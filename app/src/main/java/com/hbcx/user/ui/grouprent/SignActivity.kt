package com.hbcx.user.ui.grouprent

import android.app.Activity
import com.hbcx.user.R
import com.hbcx.user.network.Api
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.H5Activity
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_sign.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class SignActivity:TranslateStatusBarActivity() {
    private val id by lazy {
        intent.getIntExtra("id",0)
    }

    override fun setContentView() = R.layout.activity_sign

    override fun initClick() {
        cb_rule.setOnCheckedChangeListener { _, isChecked ->
            btn_action.isEnabled = isChecked
        }
        btn_action.onClick {
            HttpManager.makeSign(id).request(this@SignActivity){_,data->
                data?.let {
                    toast(it)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
        tv_rule.onClick {
            startActivity<H5Activity>("title" to "包车协议","url" to Api.GROUP_RENT_RULE)
        }
    }

    override fun initView() {
        title = "包车签约"
        webView.loadUrl(Api.SIGN+id)
    }
}