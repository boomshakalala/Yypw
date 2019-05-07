package com.hbcx.user.adapter

import android.widget.CheckedTextView
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import com.hbcx.user.R
import com.hbcx.user.beans.OpenCity
import com.hbcx.user.beans.Station

class OpenCityAdapter(data:ArrayList<OpenCity>):HFRecyclerAdapter<OpenCity>(data, R.layout.item_station) {
    override fun onBind(holder: ViewHolder, position: Int, data: OpenCity) {
        holder.setText(R.id.tv_name,data.cityName)
        holder.bind<CheckedTextView>(R.id.tv_name).isChecked = data.isChecked
    }
}