package com.hbcx.user

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.support.multidex.MultiDex
import android.util.Log
import cn.jpush.android.api.JPushInterface
import cn.sinata.rxnetty.NettyClient
import com.hbcx.user.interfaces.TripMessageListener
import cn.sinata.xldutils.application.BaseApplication
import cn.sinata.xldutils.getUUID
import cn.sinata.xldutils.utils.SPUtils
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hbcx.user.interfaces.TripStateChangedListener
import com.hbcx.user.network.Api
import com.hbcx.user.ui.MainActivity
import com.hbcx.user.utils.Const
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig
import com.uuzuche.lib_zxing.activity.ZXingLibrary
import io.reactivex.Flowable
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class YyApplication : BaseApplication(), Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity?) {

    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
        activities.remove(activity)
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        activities.add(activity)
    }

    private val activities = java.util.ArrayList<Activity?>()
    companion object {
        var lat = 0.0
        var lng = 0.0
        var city = "" //定位城市
        var selectCityName = "" //选择的城市
        var selectCityId = 0 //选择的城市ID
    }

    override fun getSPName(): String {
        return "yypw"
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        JPushInterface.init(this)
        UMConfigure.init(this, Const.UMENG_KEY, "", UMConfigure.DEVICE_TYPE_PHONE, "")
        PlatformConfig.setWeixin(Const.WX_APP_ID, Const.WX_SECRET)
        PlatformConfig.setQQZone(Const.QQ_APP_ID, Const.QQ_SECRET)
        PlatformConfig.setSinaWeibo(Const.SINA_APP_ID, Const.SINA_SECRET, "")
        initLocationOption()
        ZXingLibrary.initDisplayOpinion(this)
        NettyClient.getInstance().init(this, Api.SOCKET_SERVER, Api.SOCKET_PORT, false)
        NettyClient.getInstance().addOnMessageListener { message ->
            Log.e("socket_receive",message)
            try {
                val json = JSONObject(message)
                val method = json.optString("method")
                val code = json.optInt("code", -1)
                if (code == 0) {
                    when (method) {
                        Const.Method.PING_RECEIVE -> {//心跳
                            if (SPUtils.instance().getInt(Const.User.USER_ID) == -1) {
                                return@addOnMessageListener
                            }
                            //延时发送心跳
                            Flowable.just("").delay(5000, TimeUnit.MILLISECONDS).subscribe {
                                sendHeart()
                            }
                        }
                        Const.Method.USER_LOGIN -> {
                            NettyClient.getInstance().stopService()
                            SPUtils.instance().put(Const.User.USER_ID,-1).apply()
                            if (activities.isNotEmpty()) {
                                //最顶部页面
                                val act = activities[activities.size - 1]
                                act?.toast(json.optJSONObject("con").optString("msg"))
                                activities.forEach {
                                    it?.finish()
                                }
                                JPushInterface.deleteAlias(this,0)
                                act?.startActivity<MainActivity>()
                            }
                        }

                        Const.Method.ORDER -> {
                            val con = json.optJSONObject("con")
                            val order = Gson().fromJson<com.hbcx.user.beans.Order>(con.toString(), object : TypeToken<com.hbcx.user.beans.Order>() {}.type)
                            when (order.status) {
                                2 -> runOnUiThread {
                                    listeners.forEach {
                                        if (order.setOutIsNot!!)
                                            it.onWaitingDriver(order)
                                        else
                                            it.onResponse(order)
                                    }
                                }
                                3 -> runOnUiThread {
                                    listeners.forEach {
                                        it.onDriverArrived(order)
                                    }
                                }
                                4 -> runOnUiThread {
                                    listeners.forEach {
                                        it.onTripping(order)
                                    }
                                }
                            }
                        }

                        Const.Method.ORDER_FINISH -> {
                            val con = json.optJSONObject("con")
                            val order = Gson().fromJson<com.hbcx.user.beans.Order>(con.toString(), object : TypeToken<com.hbcx.user.beans.Order>() {}.type)
                            listeners.forEach {
                                it.onTripFinished(order)
                            }
                        }

                        Const.Method.USER_ORDER_NOYINGDA -> {//暂无司机接单
                            listeners.forEach {
                                it.onNoResponse()
                            }
                        }

                        Const.Method.PLAT_CANCEL -> {//平台取消
                            msglisteners.forEach {
                                it.onCancel(json.optJSONObject("con"))
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        NettyClient.getInstance().setOnConnectListener {
            //连接成功。发送一次心跳
            sendHeart()
        }

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private val listeners = ArrayList<TripStateChangedListener>()
    private val msglisteners = ArrayList<TripMessageListener>()

    fun addTripStateListener(changedListener: TripStateChangedListener) {
        listeners.add(changedListener)
    }

    fun removeTripStateListener(changedListener: TripStateChangedListener) {
        listeners.remove(changedListener)
    }

    fun addTripMessageListener(changedListener: TripMessageListener) {
        msglisteners.add(changedListener)
    }

    fun removeTripMessageListener(changedListener: TripMessageListener) {
        msglisteners.remove(changedListener)
    }

    private val aMapLocationClient by lazy {
        AMapLocationClient(this)
    }

    /**
     * 高德定位设置
     */
    private fun initLocationOption() {
        val option = AMapLocationClientOption()
        option.interval = 5 * 1000
        option.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        option.isMockEnable = false
        aMapLocationClient.setLocationOption(option)
    }

    /**
     * 设置定位监听回调
     */
    fun setLocationListener(listener: AMapLocationListener) {
        aMapLocationClient.setLocationListener(listener)
    }

    /**
     * 开始定位
     */
    fun startLocation() {
        if (aMapLocationClient.isStarted) {
            aMapLocationClient.stopLocation()
        }
        aMapLocationClient.startLocation()
    }

    /**
     * 停止定位
     */
    fun stopLocation() {
        if (aMapLocationClient.isStarted) {
            aMapLocationClient.stopLocation()
        }
    }

    private fun sendHeart() {
        val userId = SPUtils.instance().getInt(Const.User.USER_ID)
        NettyClient.getInstance().sendMessage("{\"con\":{\"userId\":$userId,\"type\":1,\"token\":\"${this.getUUID()}\"},\"method\":\"OK\",\"code\":\"0\",\"msg\":\"SUCCESS\"}")
    }
}