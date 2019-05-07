package com.hbcx.user.adapter

import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import com.hbcx.user.R
import com.hbcx.user.beans.Station

class SearchStationAdapter(data:ArrayList<Station>):HFRecyclerAdapter<Station>(data, R.layout.item_search_station) {
    override fun onBind(holder: ViewHolder, position: Int, data: Station) {
        holder.setText(R.id.tv_name,data.stationName)
        holder.setText(R.id.tv_address,data.stationAddress)
    }
}