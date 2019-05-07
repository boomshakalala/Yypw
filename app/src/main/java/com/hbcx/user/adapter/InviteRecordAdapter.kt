package com.hbcx.user.adapter

import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.utils.hidePhone
import cn.sinata.xldutils.utils.toTime
import com.facebook.drawee.view.SimpleDraweeView
import com.hbcx.user.R

class InviteRecordAdapter(data:ArrayList<com.hbcx.user.beans.InviteRecord>):HFRecyclerAdapter<com.hbcx.user.beans.InviteRecord>(data, R.layout.item_invite) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.InviteRecord) {
        holder.bind<SimpleDraweeView>(R.id.iv_head).setImageURI(data.imgUrl)
        holder.setText(R.id.tv_name,data.nickName)
        holder.setText(R.id.tv_phone,data.phone.hidePhone())
        holder.setText(R.id.tv_time,data.createTime.toTime("yyyy-MM-dd"))
    }
}