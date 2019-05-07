package com.hbcx.user.adapter

import android.view.View
import android.widget.TextView
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import com.hbcx.user.R

class SelectCityAdapter(data:ArrayList<com.hbcx.user.beans.OpenCity>):HFRecyclerAdapter<com.hbcx.user.beans.OpenCity>(data, R.layout.item_select_city) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.OpenCity) {
        val tvIndex = holder.bind<TextView>(R.id.tv_index)
        val tvCity = holder.bind<TextView>(R.id.tv_name)
        if (position == 0 || mData[position - 1].getInitial() != data.getInitial()) {
            tvIndex.visibility = View.VISIBLE
            holder.setText(R.id.tv_index,data.getInitial())
        } else {
            tvIndex.visibility = View.GONE
        }
        holder.setText(R.id.tv_name,data.cityName)
        tvCity.setOnClickListener {
            callback?.onCityClick(data)
        }
    }
    private var callback:OnCityClick? = null
    interface OnCityClick{
        fun onCityClick(city: com.hbcx.user.beans.OpenCity)
    }
    fun setOnCityClick(click: OnCityClick){
        callback = click
    }
}