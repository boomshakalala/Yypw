package com.hbcx.user.adapter

import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import com.hbcx.user.R
import com.hbcx.user.beans.Station

class StationAdapter(data:ArrayList<Station>):HFRecyclerAdapter<Station>(data, R.layout.item_station) {
    override fun onBind(holder: ViewHolder, position: Int, data: Station) {
        holder.setText(R.id.tv_name,data.stationName)
    }
}