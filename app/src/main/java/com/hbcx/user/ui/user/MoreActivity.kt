package com.hbcx.user.ui.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import cn.jpush.android.api.JPushInterface
import cn.sinata.xldutils.callPhone
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.optString
import com.hbcx.user.R
import com.hbcx.user.dialogs.TipDialog
import com.hbcx.user.network.Api
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.H5Activity
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.ui.rent.SelectDriverActivity
import com.hbcx.user.ui.ticket.AddPassengerActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_more.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity

class MoreActivity :TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.activity_more

    override fun initClick() {
        tv_logout.setOnClickListener {
            val tipDialog = TipDialog()
            tipDialog.arguments = bundleOf("msg" to "是否退出登录?","ok" to "确定","cancel" to "取消")
            tipDialog.setDialogListener { p, s ->
                SPUtils.instance().remove(Const.User.USER_ID).apply()
                JPushInterface.deleteAlias(this,0)
                setResult(Activity.RESULT_OK)
                finish()
            }
            tipDialog.show(supportFragmentManager,"logout")
        }
        tv_about.setOnClickListener {
            startActivity<H5Activity>("title" to "关于我们", "url" to Api.ABOUT)
        }
        tv_rule.setOnClickListener {
            startActivity<H5Activity>("title" to "平台协议", "url" to Api.PLATFORM_RULE)
        }
        ll_contact.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + getString(R.string.service_phone))
            this.startActivity(intent)
        }
        tv_safety.setOnClickListener {
            startActivity<SafetyActivity>()
        }
        tv_feedback.onClick {
            startActivity<FeedbackActivity>()
        }
        tv_passengers.onClick {
            startActivity<SelectDriverActivity>("isPassenger" to true,"checkable" to false)
        }
        ll_contact.onClick {
            callPhone(phone)
        }
    }
    private val phone by lazy {
        SPUtils.instance().getString(Const.SERVICE_PHONE)
    }
    override fun initView() {
        title = "更多"
        tv_phone.text = phone
    }
}