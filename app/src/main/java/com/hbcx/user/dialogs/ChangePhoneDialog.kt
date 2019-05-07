package com.hbcx.user.dialogs

import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.sinata.xldutils.activity.BaseActivity
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.isValidPhone
import com.hbcx.user.R
import com.hbcx.user.interfaces.OnDialogListener
import com.hbcx.user.network.HttpManager
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.isBaseActivity
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.dialog_change_phone.*
import org.jetbrains.anko.support.v4.toast

class ChangePhoneDialog: DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = LayoutInflater.from(context).inflate(R.layout.dialog_change_phone,container)!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_get_code.setOnClickListener {
            sendSms()
        }
        tv_action.setOnClickListener {
            modify()
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
            HttpManager.sendSms(phone, 2).request(it) { msg, _ ->
                if (isDestroy) {
                    return@request
                }
                toast(msg.toString())
                tv_get_code.isEnabled = false
                countDownTimer.start()
            }
        }
    }

    private fun modify(){
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
        if (code.isEmpty()) {
            toast("验证码不能为空")
            return
        }
        HttpManager.modifyPhone(SPUtils.instance().getInt(Const.User.USER_ID),phone,code).request(activity as BaseActivity){_,_->
            toast("修改成功")
            dialogListener?.onClick(0,phone)
            SPUtils.instance().put(Const.User.USER_PHONE,phone).apply()
            dismissAllowingStateLoss()
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
    private var dialogListener: OnDialogListener? = null

    fun setCallback(dialogListener: OnDialogListener){
        this.dialogListener = dialogListener
    }
}