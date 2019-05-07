package com.hbcx.user.adapter

import android.util.TypedValue
import android.widget.TextView
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import com.hbcx.user.R
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.textColorResource

class TicketListAdapter(data:ArrayList<com.hbcx.user.beans.TicketList>):HFRecyclerAdapter<com.hbcx.user.beans.TicketList>(data, R.layout.item_ticket_list) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.TicketList) {
        val flowLayout = holder.bind<com.hbcx.user.views.FlowLayout>(R.id.fl_container)
        flowLayout.removeAllViews()
        holder.setText(R.id.tv_start_time,data.start_time+"上车")
        holder.setText(R.id.tv_count,String.format("余票%s张",data.pedestal))
        holder.setText(R.id.tv_money,String.format("￥%.2f",data.money))
        holder.setText(R.id.tv_distance,String.format("距你%.2fkm",data.km1/1000))
        holder.setText(R.id.tv_length,String.format("约%.2fkm",data.km2/1000))
        holder.setText(R.id.tv_start,data.startName)
        holder.setText(R.id.tv_end,data.endName)
        if (data.facilitiesName!=null&&data.facilitiesName!!.isNotEmpty())
            data.facilitiesName!!.split(",").forEach {
                val textView = TextView(context)
                textView.textColorResource = R.color.color_tv_orange
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f)
                textView.backgroundResource = R.drawable.bg_label_orange_radio
                textView.setPadding(context.dip(10),context.dip(2),context.dip(10),context.dip(2))
                textView.text = it
                flowLayout.addView(textView)
            }
    }
}