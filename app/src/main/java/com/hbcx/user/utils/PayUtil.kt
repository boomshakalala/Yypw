package com.hbcx.user.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.text.TextUtils
import cn.sinata.xldutils.activity.BaseActivity
import cn.sinata.xldutils.defaultScheduler
import cn.sinata.xldutils.ioScheduler
import cn.sinata.xldutils.rxutils.ResultSubscriber
import com.alipay.sdk.app.PayTask
import com.hbcx.user.beans.PayInfo
import com.hbcx.user.interfaces.PayListener
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.reactivex.Flowable
import org.jetbrains.anko.toast

/**
 * 支付相关工具
 */
object PayUtil {
    var iwxapi: IWXAPI? = null
    val SDK_PAY_FLAG = 1
    val payListeners = ArrayList<PayListener?>()

    fun initWeChatPay(context: Context, appId: String): IWXAPI {
//        注册微信支付
        iwxapi = WXAPIFactory.createWXAPI(context, null)
        // 将该app注册到微信
        iwxapi!!.registerApp(appId)
        return iwxapi!!
    }

//    释放微信劫持的activity
    fun unregisterApp() {
        if (iwxapi != null) {
            iwxapi!!.unregisterApp()
            iwxapi!!.detach()
            iwxapi = null
        }
    }

    fun checkSupportWeChat(context: Context): Boolean {
        if (iwxapi == null) {
            try {
                throw Exception("please init first")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return false
        }
        return iwxapi!!.wxAppSupportAPI >= Build.PAY_SUPPORTED_SDK_INT
    }

    fun weChatPay(payInfo: PayInfo) {
        if (iwxapi == null) {
            try {
                throw Exception("please init first")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return
        }

        val req = PayReq()
        //req.appId = "wxf8b4f85f3a794e77";  // 测试用appId
        req.appId = payInfo.appid
        req.partnerId = payInfo.partnerId
        req.prepayId = payInfo.prepayId
        req.nonceStr = payInfo.nonceStr
        req.timeStamp = payInfo.timeStamp
        req.packageValue = payInfo.packageString
        req.sign = payInfo.sign
        iwxapi!!.sendReq(req)
    }

    fun checkAliPayState(context: Context): Boolean {
        val packageName = "com.eg.android.AlipayGphone"
        val pi: PackageInfo
        try {
            pi = context.packageManager.getPackageInfo(packageName, 0)
            val resolveIntent = Intent(Intent.ACTION_MAIN, null)
            resolveIntent.`package` = pi.packageName
            val pManager = context.packageManager
            val apps = pManager.queryIntentActivities(
                    resolveIntent, 0)

            val ri = apps.iterator().next() as ResolveInfo
            return ri != null
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return false
    }

    fun aliPay(context: Context, payInfo: String) {
        if (context !is BaseActivity) {
            return
        }

        Flowable.just(payInfo).ioScheduler().flatMap {
            // 构造PayTask 对象
            val alipay = PayTask(context as Activity)
            // 调用支付接口，获取支付结果
            val result = alipay.pay(payInfo, true)
            Flowable.just(result)
        }.defaultScheduler().subscribe(object : ResultSubscriber<String>(context){
            override fun onNext(t: String) {
                val payResult = com.hbcx.user.beans.PayResult(t)
                val resultStatus = payResult.resultStatus
                // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                if (TextUtils.equals(resultStatus, "9000")) {
                    context.toast("支付成功")
                    //使用广播模式通知支付页面
                    payListeners.forEach {
                        it?.onPaySuccess()
                    }
                } else {
                    // 判断resultStatus 为非"9000"则代表可能支付失败
                    // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                    when {
                        TextUtils.equals(resultStatus, "8000") -> context.toast("支付结果确认中")
                        TextUtils.equals(resultStatus, "6001") -> context.toast("支付取消")
                    // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        else -> context.toast("支付失败")
                    }
                }
            }
        })
    }

    fun addPayListener(listener: PayListener?) {
        payListeners.add(listener)
    }

    fun removePayListener(listener: PayListener?) {
        payListeners.remove(listener)
    }

}