package com.hbcx.user.dialogs

import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.DialogFragment
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.jpush.android.api.JPushInterface
import cn.sinata.rxnetty.NettyClient
import cn.sinata.xldutils.utils.*
import com.hbcx.user.R
import com.hbcx.user.R.id.et_phone
import com.hbcx.user.R.id.tv_get_code
import com.hbcx.user.interfaces.OnDialogListener
import com.hbcx.user.network.Api
import com.hbcx.user.network.HttpManager
import com.hbcx.user.network.HttpManager.sendSms
import com.hbcx.user.ui.H5Activity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.isBaseActivity
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.dialog_login_layout.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.wrapContent

/**
 * 登录弹窗
 */
class LoginDialog : DialogFragment() {
    private var loginType: Int = 1  //1:verify 2:pwd
    private var smsType: Int = 1  //1:login 3:forget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.FadeDialog)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog.window.setLayout((screenWidth() * 0.75).toInt(), wrapContent)
        dialog.window.setGravity(Gravity.CENTER)
        dialog.setCanceledOnTouchOutside(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.dialog_login_layout, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_verify_code.isSelected = true

        tv_close.setOnClickListener {
            dismissAllowingStateLoss()
        }

        tv_login.setOnClickListener {
            login()
        }

        tv_get_code.setOnClickListener {
            sendSms()
        }

        tv_verify_code.setOnClickListener {
            if (!tv_verify_code.isSelected) {
                loginType = 1
                changeTab()
            }
        }

        tv_pwd.setOnClickListener {
            if (!tv_pwd.isSelected) {
                loginType = 2
                changeTab()
            }
        }

        tv_forget.setOnClickListener {
            smsType = 3
            tv_back.visibility = View.VISIBLE
            ll_tab.visibility = View.GONE
            tv_title_reset.visibility = View.VISIBLE
            et_phone.setText("")
            et_code.setText("")
            et_pwd_reset.setText("")
            et_pwd_reset.visibility = View.VISIBLE
            tv_login.visibility = View.GONE
            tv_reset_login.visibility = View.VISIBLE
            tv_hide.visibility = View.VISIBLE
            if (loginType == 1) {
                tv_info.visibility = View.GONE
                tv_rule.visibility = View.GONE
            } else if (loginType == 2) {
                et_pwd.visibility = View.GONE
                tv_forget.visibility = View.GONE
                et_code.visibility = View.VISIBLE
                tv_get_code.visibility = View.VISIBLE
            }
        }

        tv_back.setOnClickListener {
            tv_back.visibility = View.GONE
            ll_tab.visibility = View.VISIBLE
            tv_title_reset.visibility = View.GONE
            et_phone.setText("")
            et_code.setText("")
            tv_login.visibility = View.VISIBLE
            tv_reset_login.visibility = View.GONE
            tv_hide.visibility = View.GONE
            et_pwd_reset.visibility = View.GONE
            changeTab()
            smsType = 1
        }
        tv_hide.setOnClickListener {
            if (et_pwd_reset.tag == "1") { //show
                et_pwd_reset.tag = "0"
                tv_hide.setCompoundDrawablesWithIntrinsicBounds(0,0,R.mipmap.ic_hide,0)
                et_pwd_reset.transformationMethod = PasswordTransformationMethod.getInstance()
                et_pwd_reset.setSelection(et_pwd_reset.text.length)
            } else if (et_pwd_reset.tag == "0") { //hide
                et_pwd_reset.tag = "1"
                tv_hide.setCompoundDrawablesWithIntrinsicBounds(0,0,R.mipmap.ic_show,0)
                et_pwd_reset.transformationMethod = HideReturnsTransformationMethod.getInstance()
                et_pwd_reset.setSelection(et_pwd_reset.text.length)
            }
        }

        tv_reset_login.setOnClickListener {
            forgetPwd()
        }
        tv_rule.setOnClickListener {
            startActivity<H5Activity>("title" to "用户协议","url" to Api.RULE)
        }
    }

    private fun changeTab() {
        if (loginType == 1) {
            tv_verify_code.isSelected = true
            tv_pwd.isSelected = false
            et_code.visibility = View.VISIBLE
            tv_get_code.visibility = View.VISIBLE
            et_pwd.visibility = View.GONE
            tv_info.visibility = View.VISIBLE
            tv_rule.visibility = View.VISIBLE
            tv_forget.visibility = View.GONE
        } else if (loginType == 2) {
            tv_pwd.isSelected = true
            tv_verify_code.isSelected = false
            et_code.visibility = View.GONE
            tv_get_code.visibility = View.GONE
            et_pwd.visibility = View.VISIBLE
            tv_info.visibility = View.GONE
            tv_rule.visibility = View.GONE
            tv_forget.visibility = View.VISIBLE
        }
    }

    private fun sendSms() {
        activity!!.isBaseActivity {

            val phone = et_phone.text.toString().trim()
            if (phone.isEmpty()) {
                toast("手机号不能为空")
                return
            }
            if (!phone.isValidPhone()) {
                toast("请输入正确手机号")
                return
            }
            it.showDialog()
            HttpManager.sendSms(phone, smsType).request(it) { msg, _ ->
                if (isDestroy) {
                    return@request
                }
                toast(msg.toString())
                tv_get_code.isEnabled = false
                countDownTimer.start()
            }
        }

    }

    private fun login() {
        val phone = et_phone.text.toString().trim()
        if (phone.isEmpty()) {
            toast("手机号不能为空")
            return
        }
        if (!phone.isValidPhone()) {
            toast("请输入正确手机号")
            return
        }
        val code = et_code.text.toString().trim()
        val pwd = et_pwd.text.toString().trim()
        if (loginType == 1 && code.isEmpty()) {
            toast("验证码不能为空")
            return
        }
        if (loginType == 2) {
            if (pwd.isEmpty()) {
                toast("密码不能为空")
                return
            }
            if (pwd.length < 6) {
                toast("密码不能低于六位数")
                return
            }
        }
        activity!!.isBaseActivity {
            it.showDialog()
            HttpManager.login(phone, if (loginType == 1) {
                code
            } else {
                pwd.md5()
            }, loginType, com.hbcx.user.YyApplication.city).request(it) { _, data ->
                toast("登录成功")
                NettyClient.getInstance().startService()
                SPUtils.instance().put(Const.User.USER_ID, data!!.get("id").asInt).apply()
                JPushInterface.setAlias(activity,0,data.get("phone").asString)
                SPUtils.instance().put(Const.User.USER_PHONE, data.get("phone").asString).apply()
                SPUtils.instance().put(Const.User.USER_HEAD, data.get("imgUrl").asString).apply()
                SPUtils.instance().put(Const.User.USER_BIRTH, data.get("birthDay").asString).apply()
                SPUtils.instance().put(Const.User.USER_NAME, data.get("nickName").asString).apply()
                SPUtils.instance().put(Const.User.USER_SEX, data.get("sex").asInt).apply()
                dialogListener?.onClick(0,"success")
                dismissAllowingStateLoss()
            }
        }
    }

    private fun forgetPwd() {
        val phone = et_phone.text.toString().trim()
        if (phone.isEmpty()) {
            toast("手机号不能为空")
            return
        }
        if (!phone.isValidPhone()) {
            toast("请输入正确手机号")
            return
        }
        val code = et_code.text.toString().trim()
        val pwd = et_pwd_reset.text.toString().trim()
        if (code.isEmpty()) {
            toast("验证码不能为空")
            return
        }
        if (pwd.isEmpty()) {
            toast("密码不能为空")
            return
        }
        if (pwd.length < 6) {
            toast("密码不能低于六位数")
            return
        }

        activity!!.isBaseActivity {
            HttpManager.forgetPwd(phone, pwd.md5(), code).request(it) { _, data ->
                if (data?.optInt("state",0) == 1){ //账号状态正常
                    SPUtils.instance().put(Const.User.USER_ID, data!!.get("id").asInt).apply()
                    toast("新密码已生效")
                }else{
                    toast("新密码已生效，账号状态异常")
                }
                dismissAllowingStateLoss()
            }
        }
    }


    private val countDownTimer = object : CountDownTimer(60 * 1000, 1000) {
        override fun onFinish() {
            tv_get_code.text = "重新获取"
            tv_get_code.isEnabled = true
        }

        override fun onTick(millisUntilFinished: Long) {
            tv_get_code.text = String.format("%s", millisUntilFinished / 1000)
        }
    }

    private var dialogListener: OnDialogListener? = null

    fun setDialogListener(l: (p: Int, s: String?) -> Unit) { //登录成功
        dialogListener = object : OnDialogListener {
            override fun onClick(position: Int, data: String?) {
                l(position, data)
            }
        }
    }

    private var isDestroy = false
    override fun onDestroyView() {
        super.onDestroyView()
        isDestroy = true
    }

    override fun onDestroy() {
        try {
            countDownTimer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}