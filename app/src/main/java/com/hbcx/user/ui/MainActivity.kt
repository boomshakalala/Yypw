package com.hbcx.user.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import cn.sinata.rxnetty.NettyClient
import cn.sinata.xldutils.activity.BaseActivity
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.optBoolean
import cn.sinata.xldutils.utils.optInt
import cn.sinata.xldutils.utils.optString
import cn.sinata.xldutils.visible
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationListener
import com.flyco.tablayout.listener.CustomTabEntity
import com.hbcx.user.R
import com.hbcx.user.dialogs.BannerDialog
import com.hbcx.user.dialogs.LoginDialog
import com.hbcx.user.network.Api
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.trip.FastFragment
import com.hbcx.user.ui.grouprent.GroupRentFragment
import com.hbcx.user.ui.order.MyTripActivity
import com.hbcx.user.ui.rent.RentFragment
import com.hbcx.user.ui.ticket.TicketFragment
import com.hbcx.user.ui.user.*
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.StatusBarUtil
import com.hbcx.user.utils.request
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.navigation_header.view.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast

class MainActivity : BaseActivity(), AMapLocationListener {
    private var ticketFragment = TicketFragment()
    private var rentFragment = RentFragment()
    private var groupRentFragment = GroupRentFragment()
    private var fastFragment = FastFragment.instance(1)
    private var specialFragment = FastFragment.instance(2)

    private val app by lazy {
        application as com.hbcx.user.YyApplication
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StatusBarUtil.initStatus(window)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        drawerLayout.addDrawerListener(listener)
        initDrawer()
        tab_top.setTabData(initTab(), this, R.id.fl_container, arrayListOf(ticketFragment, rentFragment, groupRentFragment, fastFragment, specialFragment))
        initClick()
        getBanner()
        getServicePhone()
        getEnableDayCount()
        if (SPUtils.instance().getInt(Const.User.USER_ID) != -1) {
            NettyClient.getInstance().startService()
        }
        RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.CALL_PHONE
                , Manifest.permission.CAMERA).subscribe {
            if (it) {
                Handler(Handler.Callback {
                    requestLocation()
                    true
                }).sendEmptyMessageDelayed(1000, 500)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (com.hbcx.user.YyApplication.selectCityName.isNotEmpty()) {
            tv_city.text = com.hbcx.user.YyApplication.selectCityName
        }
        hasNewMsg()
    }


    private val leftView by lazy {
        LayoutInflater.from(this).inflate(R.layout.navigation_header, null, false)
    }

    private fun initDrawer() {
        leftView.iv_head.setImageURI(SPUtils.instance().getString(Const.User.USER_HEAD))
        leftView.tv_name.text = SPUtils.instance().getString(Const.User.USER_NAME)

        leftView.tv_more.setOnClickListener {
            startActivityForResult<MoreActivity>(10)
        }
        leftView.tv_my_trip.setOnClickListener {
            startActivity<MyTripActivity>()
        }
        leftView.tv_rule.setOnClickListener {
            startActivity<H5Activity>("title" to "计费规则", "url" to Api.PRICE_RULE)
        }
        leftView.iv_head.onClick {
            startActivityForResult<EditActivity>(5)
        }
        leftView.tv_my_coupon.onClick {
            startActivity<MyCouponActivity>()
        }
        leftView.tv_my_score.onClick {
            startActivity<MyScoreActivity>()
        }
        leftView.tv_invite.onClick {
            startActivity<InviteActivity>()
        }
        nv_main_navigation.addHeaderView(leftView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 10) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            leftView.iv_head.setImageURI(SPUtils.instance().getString(Const.User.USER_HEAD))
            leftView.tv_name.text = SPUtils.instance().getString(Const.User.USER_NAME)
        } else if (requestCode == 1 || requestCode == 2) {
            fastFragment.onActivityResult(requestCode, resultCode, data)
        } else if (requestCode == 3 || requestCode == 4) {
            specialFragment.onActivityResult(requestCode - 2, resultCode, data)
        } else if (requestCode == 20 || requestCode == 102)
            rentFragment.onActivityResult(requestCode, resultCode, data)
        else if (requestCode == 30 || requestCode == 40 || requestCode == 101)
            groupRentFragment.onActivityResult(requestCode, resultCode, data)
        else if (requestCode == 11 || requestCode == 12 || requestCode == 13||requestCode == 14)
            ticketFragment.onActivityResult(requestCode, resultCode, data)
        else if (requestCode == 8 && resultCode == Activity.RESULT_OK)
            ticketFragment.refresh()
    }

    private fun initTab(): ArrayList<CustomTabEntity> {
        val titles = arrayOf("票务", "租车", "包车", "快车", "专车")
        val arrayList = ArrayList<CustomTabEntity>()
        for (i in 0..4) {
            arrayList.add(object : CustomTabEntity {
                override fun getTabUnselectedIcon(): Int {
                    return 0
                }

                override fun getTabSelectedIcon(): Int {
                    return 0
                }

                override fun getTabTitle(): String {
                    return titles[i]
                }
            })
        }
        return arrayList
    }

    private fun requestLocation() {
        app.setLocationListener(this)
        app.startLocation()
    }

    private val locationListeners: ArrayList<AMapLocationListener> = arrayListOf()

    fun addLocationListener(listener: AMapLocationListener) {
        locationListeners.add(listener)
    }

    override fun onLocationChanged(location: AMapLocation?) {
        location?.let {
            com.hbcx.user.YyApplication.city = it.city
            com.hbcx.user.YyApplication.lat = it.latitude
            com.hbcx.user.YyApplication.lng = it.longitude
            locationListeners.map {
                it.onLocationChanged(location)
            }
            app.stopLocation()
            HttpManager.isCityOpen(it.adCode).request(this) { _, data ->
                data?.let {
                    if (it.optBoolean("isOpen")) {
                        //如果未选择过城市
                        if (com.hbcx.user.YyApplication.selectCityName.isEmpty()) {
                            com.hbcx.user.YyApplication.selectCityName = location.city
                            com.hbcx.user.YyApplication.selectCityId = it.optInt("id")
                            tv_city.text = location.city
                        } else {

                        }

                    } else {
                        tv_city.text = "请选择"
                        toast("所在城市未开通")
                    }
                }
            }
        }
    }


    private fun checkLogin(): Boolean {
        if (SPUtils.instance().getInt(Const.User.USER_ID) == -1) {
            val loginDialog = LoginDialog()
            loginDialog.setDialogListener { p, s -> //登录成功回调
                leftView.iv_head.setImageURI(SPUtils.instance().getString(Const.User.USER_HEAD))
                leftView.tv_name.text = SPUtils.instance().getString(Const.User.USER_NAME)
                ticketFragment.refreshOrderData()
                rentFragment.getData()
                groupRentFragment.getData()
            }
            loginDialog.show(supportFragmentManager, "login")
            return false
        }
        return true
    }

    private fun initClick() {
        iv_menu.setOnClickListener {
            if (checkLogin()) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        tv_city.setOnClickListener {
            startActivityForResult<SelectCityActivity>(8)
        }
        iv_msg.onClick {
            if (checkLogin()) {
                startActivity<MessageActivity>()
            }
        }
    }

    private fun getBanner() {
        HttpManager.getBanner().request(this) { _, data ->
            data?.let {
                if (it.size > 0) {
                    val bannerDialog = BannerDialog()
                    bannerDialog.arguments = bundleOf("data" to it)
                    bannerDialog.show(supportFragmentManager, "banner")
                }
            }
        }
    }

    private fun getServicePhone(){
        HttpManager.getServicePhone().request(this){ _, data->
            data?.let {
                val phone = it.optString("phone")
                SPUtils.instance().put(Const.SERVICE_PHONE,phone).apply()
            }
        }
    }

    var enableDays = 0 //
    private fun getEnableDayCount() {
        HttpManager.getEnableDayCount().request(this) { _, data ->
            enableDays = data?.optInt("days", 0) ?: 0
        }
    }

    private fun hasNewMsg() {
        val id = SPUtils.instance().getInt(Const.User.USER_ID)
        if (id != -1) {
            HttpManager.hasNewMsg(id).request(this) { _, data ->
                if (data?.optBoolean("isMess") == true) {
                    iv_unread.visible()
                } else
                    iv_unread.gone()
            }
        }
    }

    //设置drawerLayout打开时取消禁用滑动
    private val listener = object : DrawerLayout.SimpleDrawerListener() {
        override fun onDrawerClosed(drawerView: View) {
            super.onDrawerClosed(drawerView)
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }

        override fun onDrawerOpened(drawerView: View) {
            super.onDrawerOpened(drawerView)
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }
}
