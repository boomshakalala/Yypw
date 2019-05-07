package com.hbcx.user.ui.user

import android.app.Activity
import android.content.Intent
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.hidePhone
import com.hbcx.user.R
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import kotlinx.android.synthetic.main.activity_safety.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

class SafetyActivity: TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.activity_safety

    override fun initClick() {
        ll_phone.setOnClickListener {
            startActivityForResult<ChangePhoneActivity>(1)
        }
        tv_pwd.setOnClickListener {
            startActivity<VerifyPhoneActivity>()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1&&resultCode == Activity.RESULT_OK)
            tv_phone.text = SPUtils.instance().getString(Const.User.USER_PHONE).hidePhone()
    }

    override fun initView() {
        title = "账户安全"
        tv_phone.text = SPUtils.instance().getString(Const.User.USER_PHONE).hidePhone()
    }
}