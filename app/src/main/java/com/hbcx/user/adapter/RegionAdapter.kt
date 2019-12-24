package com.hbcx.user.adapter

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import com.hbcx.user.R
import com.hbcx.user.beans.Region

class RegionAdapter(data:ArrayList<Region>, private val adapters:ArrayList<StationAdapter>):HFRecyclerAdapter<Region>(data, R.layout.item_region) {
    override fun onBind(holder: ViewHolder, position: Int, data: Region) {
        val recyclerView = holder.bind<RecyclerView>(R.id.rv_station)
        holder.setText(R.id.tv_region,data.name)
//        holder.setOnClickListener(R.id.tv_region, View.OnClickListener {
//            data.isOpen = !data.isOpen
//            if (data.isOpen){
//                recyclerView.visibility = View.VISIBLE
//            }else
//                recyclerView.visibility = View.GONE
//        })
//        if (data.isOpen){
//            recyclerView.visibility = View.VISIBLE
//        }else
//            recyclerView.visibility = View.GONE
        recyclerView.layoutManager = GridLayoutManager(context,3)
        recyclerView.adapter = adapters[position]
    }
}