package com.hbcx.user.ui.user

import android.app.Activity
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.hidePhone
import com.hbcx.user.R
import com.hbcx.user.dialogs.ChangePhoneDialog
import com.hbcx.user.interfaces.OnDialogListener
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import kotlinx.android.synthetic.main.activity_change_phone.*

class ChangePhoneActivity:TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.activity_change_phone

    override fun initClick() {
        tv_action.setOnClickListener {
            val changePhoneDialog = ChangePhoneDialog()
            changePhoneDialog.setCallback(object :OnDialogListener{
                override fun onClick(position: Int, data: String?) {
                    tv_phone.text = getString(R.string.phone_hide,data.hidePhone())
                    setResult(Activity.RESULT_OK)
                }
            })
            changePhoneDialog.show(supportFragmentManager,"changePhone")
        }
    }

    override fun initView() {
        title = "修改手机号"
        tv_phone.text = getString(R.string.phone_hide,SPUtils.instance().getString(Const.User.USER_PHONE).hidePhone())
    }
}