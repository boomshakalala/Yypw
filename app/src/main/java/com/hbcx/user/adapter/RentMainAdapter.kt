package com.hbcx.user.adapter

import android.view.View
import android.widget.TextView
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.utils.toTime
import com.hbcx.user.R
import org.jetbrains.anko.textColor

class RentMainAdapter(data: ArrayList<com.hbcx.user.beans.RentOrder>, private val callback: OnClickCallBack) : HFRecyclerAdapter<com.hbcx.user.beans.RentOrder>(data, R.layout.item_rent_order) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.RentOrder) {
        holder.setText(R.id.tv_time, data.createTime.toTime("yyyy-MM-dd HH:mm"))
        holder.setText(R.id.tv_state, data.getStateStr())
        holder.bind<TextView>(R.id.tv_state).textColor = context.resources.getColor(data.getStateColor())
        holder.setText(R.id.tv_brand, data.brandName + data.modelName)
        holder.setText(R.id.tv_car_info, "${data.gears}·${data.pedestal}座·${data.displacement} ${data.levelName}")
        holder.setText(R.id.tv_rent_time_range, "${data.startTime.toTime("MM-dd HH:mm")} 至 ${data.endTime.toTime("MM-dd HH:mm")}")
        holder.setText(R.id.tv_rent_time, "${data.times}${if (data.hourOrDay == 1) "小时" else "天"}")
        holder.setText(R.id.tv_price, "￥${data.payMoney}")
        holder.setText(R.id.tv_company, data.companyName)
        holder.setText(R.id.tv_action, data.getActionStr())
        val tvCancel = holder.bind<TextView>(R.id.tv_cancel)
        tvCancel.visibility = if (data.status == 4) View.GONE else View.VISIBLE
        holder.bind<TextView>(R.id.tv_action).visibility = if (data.status in 5..7) View.GONE else View.VISIBLE
        holder.setText(R.id.tv_cancel, data.getCancelStr())
        holder.bind<TextView>(R.id.tv_action).setOnClickListener {
            it as TextView
            if (it.text == "联系商家") {
                callback.onCall(data.contactNumber)
            } else if (it.text == "立即支付")
                callback.onPay(data)
        }
        tvCancel.setOnClickListener {
            it as TextView
            if (it.text == "取消订单")
                callback.onCancel(data.id)
            else
                callback.onDelete(data.id)
        }
        holder.itemView.setOnClickListener {
            callback.onItemClick(data.id)
        }
    }

    interface OnClickCallBack {
        fun onPay(order: com.hbcx.user.beans.RentOrder)
        fun onCall(phone: String)
        fun onCancel(id: Int)
        fun onDelete(id: Int)
        fun onItemClick(id: Int)
    }
}