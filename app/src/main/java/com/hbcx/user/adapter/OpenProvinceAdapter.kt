package com.hbcx.user.adapter

import android.widget.CheckedTextView
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import com.hbcx.user.R
import com.hbcx.user.beans.OpenCity
import com.hbcx.user.beans.OpenProvince
import com.hbcx.user.beans.Station

class OpenProvinceAdapter(data:ArrayList<OpenProvince>):HFRecyclerAdapter<OpenProvince>(data, R.layout.item_station) {
    override fun onBind(holder: ViewHolder, position: Int, data: OpenProvince) {
        holder.setText(R.id.tv_name,data.province)
        holder.bind<CheckedTextView>(R.id.tv_name).isChecked = data.isChecked
    }
}