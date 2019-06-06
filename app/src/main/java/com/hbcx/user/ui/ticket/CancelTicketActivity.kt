package com.hbcx.user.ui.ticket

import android.app.Activity
import android.view.LayoutInflater
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.SpanBuilder
import cn.sinata.xldutils.utils.hideIdCard
import cn.sinata.xldutils.utils.optDouble
import com.hbcx.user.R
import com.hbcx.user.beans.PassengerTicket
import com.hbcx.user.dialogs.TipDialog
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_cancel_ticket.*
import kotlinx.android.synthetic.main.item_passenger_select.view.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast

class CancelTicketActivity:TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.activity_cancel_ticket

    private val passengers = arrayListOf<PassengerTicket>() //选中的乘客

    private val id by lazy {
        intent.getIntExtra("id",0)
    }

    override fun initClick() {
        btn_action.onClick {
            if (passengers.isEmpty()){
                toast("请选择要退票的乘客")
                return@onClick
            }
            getCancelMoney()
        }
    }

    override fun initView() {
        title = "申请退票"

        tv_tip_1.text = SpanBuilder("※ 退票不支持已在车站取票的车票，请前往车站售票窗口处理退票")
                .color(this, 0, 1, R.color.color_tv_orange).build()
        tv_tip_2.text = SpanBuilder("※ 退款金额预计在7个工作日内原路返回到您的账户中")
                .color(this, 0, 1, R.color.color_tv_orange).build()
        val phone = SPUtils.instance().getString(Const.SERVICE_PHONE)
        tv_tip_3.text = SpanBuilder("※ 若有任何疑问请拨打客服热线：$phone")
                .color(this, 0, 1, R.color.color_tv_orange)
                .color(this, 16, 16+phone.length, R.color.black_text).build()

        getData()
    }

    private fun getData(){
        HttpManager.getPassengerList(id).request(this) { _, data ->
            data?.let {
                if (it.isNotEmpty()) {
                    it.filter {
                        it.status == 1
                    }.forEach {
                        val view = LayoutInflater.from(this).inflate(R.layout.item_passenger_select, null)
                        view.tv_name.text = it.name
                        view.tv_id_card.text = "身份证号"+it.idCard.hideIdCard()
                        view.setOnClickListener {_->
                            view.iv_check.isChecked = !view.iv_check.isChecked
                            if (view.iv_check.isChecked)
                                passengers.add(it)
                            else
                                passengers.remove(it)
                        }
                        ll_passenger.addView(view)
                    }
                }
            }
        }
    }

    private fun getCancelMoney(){
        showDialog()
        HttpManager.getCancelMoney(id,passengers.size).request(this){_,data->
            data?.let {
                val tipDialog = TipDialog()
                tipDialog.arguments = bundleOf("msg" to "你选择了${passengers.size}张退票，退票总金额￥${it.optDouble("totalMoney")}， 是否确认要进行退票？","ok" to "我再想想","cancel" to "确认退票")
                tipDialog.setCancelDialogListener { p, s ->
                    refundTicket()
                }
                tipDialog.show(supportFragmentManager,"cancel")
            }
        }
    }

    //退票
    private fun refundTicket(){
        showDialog()
        val sb = StringBuilder()
        passengers.forEach {
            sb.append(it.id).append(",")
        }
        sb.deleteCharAt(sb.lastIndex)
        HttpManager.refundTicket(id,sb.toString()).request(this){_,_->
            addTicket(id,passengers.size)
        }
    }

    private fun addTicket(id:Int,number:Int){
        HttpManager.addTicket(id,number).request(this){_,_->
            toast("申请退票成功")
            setResult(Activity.RESULT_OK)
            finish()
        }
    }


}