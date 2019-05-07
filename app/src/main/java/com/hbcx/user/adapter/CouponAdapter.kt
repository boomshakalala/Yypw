package com.hbcx.user.adapter

import android.widget.ImageView
import android.widget.RelativeLayout
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.utils.toTime
import com.hbcx.user.R
import org.jetbrains.anko.backgroundResource

class CouponAdapter(data:ArrayList<com.hbcx.user.beans.Coupon>, private val money:Double = 0.0):HFRecyclerAdapter<com.hbcx.user.beans.Coupon>(data, R.layout.item_coupon) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.Coupon) {
        holder.setText(R.id.tv_title,data.getTitle())
        holder.setText(R.id.tv_deadline,"有效期至 ${data.expiryTime.toTime("yyyy-MM-dd")}")
        holder.setText(R.id.tv_describe,data.getDescribe())
        holder.setText(R.id.tv_money,data.money.toString())
        holder.bind<ImageView>(R.id.iv_button).setImageResource(if (data.isChecked) R.mipmap.ic_checked else R.mipmap.ic_unchecked)
        if (money<data.money)
            holder.bind<RelativeLayout>(R.id.rl_coupon).backgroundResource = R.mipmap.coupon_disable
        else
            holder.bind<RelativeLayout>(R.id.rl_coupon).backgroundResource = R.mipmap.coupon
    }
}