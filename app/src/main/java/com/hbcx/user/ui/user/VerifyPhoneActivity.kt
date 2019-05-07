package com.hbcx.user.ui.user

import android.os.CountDownTimer
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.*
import cn.sinata.xldutils.visible
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_verify_phone.*
import org.jetbrains.anko.toast

class VerifyPhoneActivity : TranslateStatusBarActivity() {
    private var step = 1
    override fun setContentView() = R.layout.activity_verify_phone

    override fun initClick() {
        tv_get_code.setOnClickListener {
            sendSms()
        }

        tv_action.setOnClickListener {
            if (step == 1)
                checkCode()
            else
                setPwd()
        }

        iv_hide.setOnClickListener {
            if (et_pwd.tag == "1") { //show
                et_pwd.tag = "0"
                iv_hide.setImageResource(R.mipmap.ic_hide)
                et_pwd.transformationMethod = PasswordTransformationMethod.getInstance()
                et_pwd.setSelection(et_pwd.text.length)
            } else if (et_pwd.tag == "0") { //hide
                et_pwd.tag = "1"
                iv_hide.setImageResource(R.mipmap.ic_show)
                et_pwd.transformationMethod = HideReturnsTransformationMethod.getInstance()
                et_pwd.setSelection(et_pwd.text.length)
            }
        }
    }

    private fun setPwd() {
        val pwd = et_pwd.text.toString().trim()
        if (pwd.isEmpty()) {
            toast("密码不能为空")
            return
        }
        if (pwd.length < 6) {
            toast("密码不能低于六位数")
            return
        }
        HttpManager.modifyPwd(SPUtils.instance().getInt(Const.User.USER_ID),pwd.md5()).request(this){_,_->
            toast("修改成功")
            finish()
        }
    }

    private fun checkCode() {
        val code = et_code.text.toString().trim()
        if (code.length != 6) {
            toast("请输入六位数验证码")
            return
        }
        HttpManager.checkCode(phone, code, 3).request(this) { _, _ ->
            toast("验证成功")
            stepTwo()
        }
    }

    private fun stepTwo() {
        step = 2
        tv_hint.text = "密码由6-18位英文字母、数字或字符组成"
        ll_code.gone()
        ll_pwd.visible()
        tv_action.text = "确认"
    }

    private val phone by lazy {
        SPUtils.instance().getString(Const.User.USER_PHONE)
    }

    override fun initView() {
        title = "安全验证"
        tv_hint.text = getString(R.string.phone_hint_tip, phone.hidePhone())
    }

    private fun sendSms() {
        if (phone.isEmpty()) {
            toast("手机号不能为空")
            return
        }
        if (!phone.isValidPhone()) {
            toast("请输入正确手机号")
            return
        }
        showDialog()
        HttpManager.sendSms(phone, 3).request(this) { msg, _ ->
            if (isDestroy) {
                return@request
            }
            toast(msg.toString())
            tv_get_code.isEnabled = false
            countDownTimer.start()
        }
    }

    private val countDownTimer = object : CountDownTimer(60 * 1000, 1000) {
        override fun onFinish() {
            tv_get_code.text = "重新获取"
            tv_get_code.isEnabled = true
        }

        override fun onTick(millisUntilFinished: Long) {
            tv_get_code.text = String.format("%ss", millisUntilFinished / 1000)
        }
    }
}