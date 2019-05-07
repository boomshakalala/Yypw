package com.hbcx.user.adapter

import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.utils.SpanBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.hbcx.user.R

class MoreGroupRentCarAdapter(data:ArrayList<com.hbcx.user.beans.GroupRentCarData>):HFRecyclerAdapter<com.hbcx.user.beans.GroupRentCarData>(data, R.layout.item_group_rent_car) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.GroupRentCarData) {
        holder.bind<SimpleDraweeView>(R.id.iv_car).setImageURI(data.imgUrl)
        holder.setText(R.id.tv_title,data.brandName+data.modelName)
        holder.setText(R.id.tv_person_count,"可乘坐${data.pedestal}人")
        val s = "￥${data.money}/天"
        holder.setText(R.id.tv_price, SpanBuilder(s).color(context,s.length-2,s.length,R.color.black_text)
                .size(s.length-2,s.length,12).build())
    }
}