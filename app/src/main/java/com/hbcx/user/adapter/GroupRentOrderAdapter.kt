package com.hbcx.user.adapter

import android.view.View
import android.widget.TextView
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.SpanBuilder
import cn.sinata.xldutils.utils.toTime
import cn.sinata.xldutils.utils.toWeek
import cn.sinata.xldutils.visible
import com.facebook.drawee.view.SimpleDraweeView
import com.hbcx.user.R
import org.jetbrains.anko.textColor

class GroupRentOrderAdapter(data: ArrayList<com.hbcx.user.beans.GroupRentOrder>, private val callback: OnClickCallBack) : HFRecyclerAdapter<com.hbcx.user.beans.GroupRentOrder>(data, R.layout.item_group_rent_order) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.GroupRentOrder) {
        holder.bind<SimpleDraweeView>(R.id.iv_head).setImageURI(data.companyIcon)
        holder.setText(R.id.tv_company, data.companyName)
        val tvState = holder.bind<TextView>(R.id.tv_state)
        tvState.text = data.getStateStr()
        tvState.textColor = context.resources.getColor(data.getStateColor())
        holder.setText(R.id.tv_start_point, "${data.startAddress}  出发")
        holder.setText(R.id.tv_time, "${data.startTime.toTime("MM-dd")} ${data.startTime.toWeek()} ${data.startTime.toTime("HH:mm")}")
        holder.setText(R.id.tv_rent_time, "${data.times}天")
        holder.setText(R.id.tv_brand, "${data.brandName}${data.modelName}")
        holder.setText(R.id.tv_count, "${data.carNum}辆")
        holder.setText(R.id.tv_type, data.levelName)
        val tvBalance = holder.bind<TextView>(R.id.tv_balance_payment)
        if (data.status == 1) tvBalance.gone() else tvBalance.visible()
        tvBalance.text = SpanBuilder("尾款金额 ￥${data.balance}").color(context, 0, 4, R.color.black_text).build()
        holder.setText(R.id.tv_money_title, if (data.status == 1) "预计金额" else "订金金额")
        holder.setText(R.id.tv_price, if (data.status == 1) "￥${data.startMoney}-￥${data.endMoney}" else "￥${data.deposit}")
        val tvCancel = holder.bind<TextView>(R.id.tv_cancel)
        tvCancel.visibility = if (data.status == 5) View.GONE else View.VISIBLE
        holder.bind<TextView>(R.id.tv_action).visibility = if (data.status in 6..8) View.GONE else View.VISIBLE
        holder.setText(R.id.tv_cancel, data.getCancelStr())
        holder.setText(R.id.tv_action, data.getActionStr())
        holder.bind<TextView>(R.id.tv_action).setOnClickListener {
            it as android.widget.TextView
            when {
                it.text == "联系商家" -> callback.onCall(data.contactNumber)
                it.text == "支付订金" -> callback.onPay(data)
                it.text == "立即签约" -> callback.onSign(data.id)
            }
        }
        tvCancel.setOnClickListener {
            it as android.widget.TextView
            if (it.text == "删除订单")
                callback.onDelete(data.id)
            else
                callback.onCancel(data.id, data.status)
        }
        holder.itemView.setOnClickListener {
            callback.onItemClick(data.id)
        }
    }

    interface OnClickCallBack {
        fun onPay(order: com.hbcx.user.beans.GroupRentOrder)
        fun onSign(id: Int)
        fun onCall(phone: String)
        fun onCancel(id: Int, status: Int)
        fun onDelete(id: Int)
        fun onItemClick(id: Int)
    }
}