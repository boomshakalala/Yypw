package com.hbcx.user.ui.trip

import android.app.Activity
import android.widget.RadioButton

import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_cancel_order.*
import org.jetbrains.anko.find
import org.jetbrains.anko.toast

/**
 * 取消订单
 */
class CancelOrderActivity : TranslateStatusBarActivity() {
    override fun setContentView(): Int = R.layout.activity_cancel_order

    override fun initClick() {
        rg.setOnCheckedChangeListener { group, checkedId ->
            reason = find<RadioButton>(checkedId).text.toString()
            if (checkedId == R.id.rb_8) {//其他理由
                reason = "其他"
            }
        }

        tv_action.setOnClickListener {
            if (reason.isEmpty()) {
                toast("请选择取消原因")
                return@setOnClickListener
            }
            if (rg.checkedRadioButtonId == R.id.rb_8) {
                val content = et_content.text.toString()
                if (content.isEmpty()) {
                    toast("请输入详细描述")
                    return@setOnClickListener
                }
            }
            submit()
        }
    }

    override fun initView() {
        title = "取消原因"
    }

    private val id by lazy {
        intent.getIntExtra("orderId",0)
    }

    private var reason = ""


    private fun submit() {
        showDialog()
        val content = et_content.text.toString()
        HttpManager.cancelOrder(id,reason,content).request(this){ _, data->
            toast("订单已取消成功！")
            setResult(Activity.RESULT_OK,intent.putExtra("order",data))
            finish()
        }
    }


}
