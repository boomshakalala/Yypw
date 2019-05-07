package com.hbcx.user.ui.trip

import android.widget.RadioButton
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_complain.*
import org.jetbrains.anko.find
import org.jetbrains.anko.toast

/**
 * 投诉订单
 */
class ComplainActivity : TranslateStatusBarActivity() {
    override fun setContentView(): Int = R.layout.activity_complain

    override fun initClick() {
        tv_action.setOnClickListener {
            submit()
        }
    }

    override fun initView() {
        title = "我要投诉"
        rg.setOnCheckedChangeListener { group, checkedId ->
            reason = find<RadioButton>(checkedId).text.toString()
            if (checkedId == R.id.rb_8) {//其他理由
                reason = "其他"
            }
        }
    }

    private val id by lazy {
        intent.getIntExtra("id",0)
    }

    private var reason = ""


    private fun submit() {
        if (reason.isEmpty()) {
            toast("请选择投诉原因")
            return
        }
        if (rg.checkedRadioButtonId == R.id.rb_8) {
            val content = et_content.text.toString()
            if (content.isEmpty()) {
                toast("请输入描述内容")
                return
            }
        }
        showDialog()
        val content = et_content.text.toString()
        HttpManager.complaints(id,reason,content).request(this){ _, data->
            toast("投诉成功，我们会尽快核实处理！")
            finish()
        }
    }
}
