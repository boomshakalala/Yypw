package com.hbcx.user.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.sinata.xldutils.utils.SpanBuilder

import cn.sinata.xldutils.utils.screenWidth
import com.hbcx.user.R
import kotlinx.android.synthetic.main.dialog_price_ticket.*
import org.jetbrains.anko.wrapContent

/**
 * 提示弹窗
 */
class TicketPriceDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_FRAME, R.style.FadeDialog)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog.window.setLayout((screenWidth()*0.75).toInt(), wrapContent)
        dialog.window.setGravity(Gravity.CENTER)
        dialog.setCanceledOnTouchOutside(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.dialog_price_ticket, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val order = arguments!!.getSerializable("order") as com.hbcx.user.beans.TicketOrder
        val s = "${order.passengerList.size}人"
        tv_all_price.text = String.format("￥%.2f",order.payMoney)
        tv_coupon_price.text = String.format("￥%.2f",order.couponsMoney)
        tv_score_price.text = String.format("￥%.2f",order.integralMoney)
        tv_person_count.text = s
        val s1 = String.format("￥%.2f×%s",order.serverMoney,s)
        tv_service_price.text = SpanBuilder(s1)
                .color(context!!,s1.length-s.length-1,s1.length,R.color.black_text).build()
        val s2 = String.format("￥%.2f×%s",order.insuranceMoney,s)
        tv_safe_price.text = SpanBuilder(s2)
                .color(context!!,s2.length-s.length-1,s2.length,R.color.black_text).build()
        val s3 = String.format("￥%.2f×%s",order.ticketMoney,s)
        tv_ticket_price.text = SpanBuilder(s3)
                .color(context!!,s3.length-s.length-1,s3.length,R.color.black_text).build()
        iv_close.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }
}