package com.hbcx.user.adapter

import android.widget.TextView
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.utils.toTime
import cn.sinata.xldutils.utils.toWeek
import com.hbcx.user.R
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textColorResource

class TicketOrderAdapter(data: ArrayList<com.hbcx.user.beans.TicketOrder>, private val callback: OnClickCallBack) : HFRecyclerAdapter<com.hbcx.user.beans.TicketOrder>(data, R.layout.item_ticket_order) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.TicketOrder) {
        holder.setText(R.id.tv_time,data.createTime.toTime("yyyy-MM-dd HH:mm"))
        holder.setText(R.id.tv_status,data.getStatusStr())
        holder.setText(R.id.tv_start_time,data.rideDate.toTime("MM月dd日 ")+data.rideDate.toWeek() + data.rideDate.toTime(" HH:mm")+"上车")
        holder.setText(R.id.tv_start,data.pointUpName)
        holder.setText(R.id.tv_end,data.pointDownName)
        holder.setText(R.id.tv_num,String.format("共%d张票",data.peopleNum))
        holder.setText(R.id.tv_price,String.format("￥%.2f",data.payMoney))
        holder.bind<TextView>(R.id.tv_status).textColorResource = data.getStatusColorRes()
        holder.bind<TextView>(R.id.tv_action).visibility = data.getPositiveVisibility()
        holder.setText(R.id.tv_action,data.getPositiveBtnStr())
        holder.setText(R.id.tv_cancel,data.getNegativeBtnStr())

        holder.itemView.onClick {
            callback.onItemClick(data.id)
        }
        holder.bind<TextView>(R.id.tv_cancel).onClick {
            val tv = it as TextView
            when (tv.text){
                "申请退票" -> callback.onRefund(data.id)
                "取消订单" ->callback.onCancel(data.id)
                "删除订单" ->callback.onDelete(data.id)
            }
        }
        holder.bind<TextView>(R.id.tv_action).onClick {
            val tv = it as TextView
            when (tv.text){
                "立即支付" -> callback.onPay(data)
                " 验票码 " ->callback.onItemClick(data.id)
                "立即评价" ->callback.onEvaluate(data.id)
            }
        }
    }

    interface OnClickCallBack {
        fun onPay(order: com.hbcx.user.beans.TicketOrder)
        fun onCancel(id: Int)
        fun onDelete(id: Int)
        fun onEvaluate(id: Int)
        fun onRefund(id: Int)
        fun onItemClick(id: Int)
    }
}