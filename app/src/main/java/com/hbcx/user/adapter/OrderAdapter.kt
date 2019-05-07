package com.hbcx.user.adapter

import android.support.v4.content.ContextCompat
import android.widget.TextView
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.utils.toTime
import cn.sinata.xldutils.utils.toWeek
import com.hbcx.user.R

/**
 *
 */
class FastOrderAdapter(mData:ArrayList<com.hbcx.user.beans.Order>):HFRecyclerAdapter<com.hbcx.user.beans.Order>(mData, R.layout.item_list_fast_order) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.Order) {
        holder.setText(R.id.tv_time,data.createTime!!.toTime("yyyy-MM-dd HH:mm"))
        holder.setText(R.id.tv_status,data.getStateStr())
        holder.setText(R.id.tv_start,data.startAddress)
        holder.setText(R.id.tv_end,data.endAddress)
        holder.setText(R.id.tv_start_time,data.departureTime!!.toTime("MM月dd日 ")+data.departureTime.toWeek()
        +data.departureTime.toTime(" HH:mm上车")+when(data.type){
            2->" 经济型"
            3->" 舒适型"
            4->" 商务型"
            else->""
        })

        val stateView = holder.bind<TextView>(R.id.tv_status)
        when(data.status){
            8,9->
                stateView.setTextColor(ContextCompat.getColor(context,R.color.light_gry_text))
            else->
                stateView.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary))
        }
    }

}