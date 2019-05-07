package com.hbcx.user.ui.rent

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.hbcx.user.R
import com.hbcx.user.dialogs.TipDialog
import com.hbcx.user.network.Api
import com.hbcx.user.ui.H5Activity
import com.hbcx.user.ui.TranslateStatusBarActivity
import kotlinx.android.synthetic.main.activity_rent_safe.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.startActivity

class RentSafeActivity : TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.activity_rent_safe

    private val isTicket by lazy {
        intent.getBooleanExtra("isTicket",false)
    }
    private val safeMoney by lazy {
        intent.getDoubleExtra("money",0.0)
    }
    override fun initClick() {
        rl_base.setOnClickListener {
            setResult(Activity.RESULT_OK,intent.putExtra("safe",0))
            finish()
        }
        rl_all.setOnClickListener {
            if (!isTicket){
                setResult(Activity.RESULT_OK,intent.putExtra("safe",1))
                finish()
            }else{
                val tipDialog = TipDialog()
                tipDialog.arguments = bundleOf("msg" to "为了您和家人多一份安心，建议选购车意险，小小一份保险，保护大安全！","ok" to "买份安心","cancel" to "残忍拒绝")
                tipDialog.setDialogListener { p, s ->
                    setResult(Activity.RESULT_OK,intent.putExtra("safe",0))
                    finish()
                }
                tipDialog.setCancelDialogListener { p, s ->
                    setResult(Activity.RESULT_OK,intent.putExtra("safe",1))
                    finish()
                }
                tipDialog.show(supportFragmentManager,"safe")
            }
        }
    }

    override fun initView() {
        titleBar.addRightButton("保险须知",onClickListener = View.OnClickListener {
            startActivity<H5Activity>("title" to "保险须知","url" to if (isTicket) Api.TICKET_SAFE_RULE else Api.RENT_SAFE_RULE)
        })
        (titleBar.getRightButton(0) as TextView).setTextColor(resources.getColor(R.color.black_text))
        if (isTicket){
            title = "选择出行保障"
            tv_money_base.text = String.format("￥%.2f元/人",safeMoney)
            tv_title_1.text = "出行保障"
            tv_title_2.text = "不购买"
            tv_tip_1.text = "优先出票，保额30000元"
            tv_tip_2.text = "为了家人放心，建议选择出行保障"
        }else{
            title = "选择租车保障"
            val carSafe = intent.getSerializableExtra("data") as com.hbcx.user.beans.CarSafe
            tv_money_base.text = "￥${carSafe.basicsMoney}元/天"
            tv_money_all.text = "￥${carSafe.comprehensiveMoney}元/天"
        }
    }
}
