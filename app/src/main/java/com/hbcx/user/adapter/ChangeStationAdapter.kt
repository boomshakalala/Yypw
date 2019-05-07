package com.hbcx.user.adapter

import android.widget.CheckBox
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import com.hbcx.user.R

class ChangeStationAdapter(data: ArrayList<com.hbcx.user.beans.BusStation>) : HFRecyclerAdapter<com.hbcx.user.beans.BusStation>(data, R.layout.item_change_station) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.BusStation) {
        holder.setText(R.id.tv_station, data.name)
        holder.setText(R.id.tv_time, String.format("预计%s", data.times))
        holder.bind<CheckBox>(R.id.cb_check).isChecked = data.isChecked
    }
}