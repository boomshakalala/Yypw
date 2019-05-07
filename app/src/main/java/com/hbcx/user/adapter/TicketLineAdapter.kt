package com.hbcx.user.adapter

import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.hbcx.user.R

class TicketLineAdapter(data: ArrayList<com.hbcx.user.beans.TicketLine>) : HFRecyclerAdapter<com.hbcx.user.beans.TicketLine>(data, R.layout.item_ticket_line) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.TicketLine) {
        holder.bind<SimpleDraweeView>(R.id.iv_img) .setImageURI(data.imgUrl)
        holder.setText(R.id.tv_title,data.name)
        holder.setText(R.id.tv_content,data.remark)
    }
}