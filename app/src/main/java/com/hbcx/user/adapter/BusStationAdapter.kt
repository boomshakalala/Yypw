package com.hbcx.user.adapter

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.visible
import com.hbcx.user.R
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColorResource

class BusStationAdapter(data: ArrayList<com.hbcx.user.beans.BusStation>) : HFRecyclerAdapter<com.hbcx.user.beans.BusStation>(data, R.layout.item_bus_station) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.BusStation) {
        val top = holder.bind<View>(R.id.top_line)
        val bottom = holder.bind<View>(R.id.bottom_line)
        val stamp = holder.bind<TextView>(R.id.tv_stamp)
        holder.setText(R.id.tv_station, data.name)
        when (position) {
            0 -> {//始发站
                top.visibility = View.INVISIBLE
                bottom.visibility = View.VISIBLE
                holder.setText(R.id.tv_time, data.times)
                stamp.visible()
                stamp.text = "起"
                stamp.backgroundResource = R.drawable.bg_blue_circle
            }
            itemCount - 1 -> {//终点站
                top.visibility = View.VISIBLE
                bottom.visibility = View.INVISIBLE
                if (data.times != null && data.times.isNotEmpty() && data.times != "null")
                    holder.setText(R.id.tv_time, String.format("预计%s", data.times))
                stamp.visible()
                stamp.text = "终"
                stamp.backgroundResource = R.drawable.bg_orange_circle
            }
            else -> {
                top.visibility = View.VISIBLE
                bottom.visibility = View.VISIBLE
                if (data.times != null && data.times.isNotEmpty() && data.times != "null")
                    holder.setText(R.id.tv_time, String.format("预计%s", data.times))
                stamp.gone()
            }
        }
        when (data.isUpOrDown) {
            1 -> { //上车点
                stamp.visible()
                stamp.text = "上"
                stamp.backgroundResource = R.drawable.bg_blue_circle
            }
            2 -> {//下车点
                stamp.visible()
                stamp.text = "下"
                stamp.backgroundResource = R.drawable.bg_orange_circle
            }
        }
        holder.bind<TextView>(R.id.tv_station).typeface = if (data.isUpOrDown in 1..2) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        holder.bind<TextView>(R.id.tv_time).typeface = if (data.isUpOrDown in 1..2) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        holder.bind<TextView>(R.id.tv_station).textColorResource = if (data.isUpOrDown in 1..2) R.color.black_text else R.color.textColor66
        holder.bind<TextView>(R.id.tv_time).textColorResource = if (data.isUpOrDown in 1..2) R.color.black_text else R.color.textColor66
    }
}