package com.hbcx.user.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import cn.sinata.xldutils.utils.screenWidth
import com.hbcx.user.R
import kotlinx.android.synthetic.main.dialog_price_layout.*
import org.jetbrains.anko.wrapContent

/**
 * 提示弹窗
 */
class PriceDialog : DialogFragment() {

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
            inflater.inflate(R.layout.dialog_price_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val order = arguments!!.getSerializable("order") as com.hbcx.user.beans.RentOrder
        tv_all_price.text = "￥${order.payMoney}"
        tv_coupon_price.text = "￥${order.couponsMoney}"
        tv_service_price.text = "￥${order.carLineMoney}"
        tv_rent_price.text = "￥${order.rentingMoney}"
        tv_safe_price.text = "￥${order.serverMoney}"
//        if (order.hourOrDay==1){ //小时
//            val s = "￥${order.hour_money}"
//            tv_rent_price.text = SpanBuilder("${s}X$duration").color(this,0,s.length,R.color.color_money_text).build()
//        }else{

//        }
        iv_close.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }
}