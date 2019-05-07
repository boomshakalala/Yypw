package com.hbcx.user.adapter

import android.graphics.Color
import android.util.TypedValue
import android.widget.TextView
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.utils.SpanBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.hbcx.user.R
import org.jetbrains.anko.dip

class RentCarCompanyAdapter(data:ArrayList<com.hbcx.user.beans.CarData>):HFRecyclerAdapter<com.hbcx.user.beans.CarData>(data, R.layout.item_rent_car) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.CarData) {
        holder.bind<SimpleDraweeView>(R.id.iv_car).setImageURI(data.company_icon)
        holder.setText(R.id.tv_title,data.name)
        holder.setText(R.id.tv_distance,String.format("%.2fkm", data.distance.toDouble() / 1000))
        val s = "￥${data.day_money}/天"
        holder.setText(R.id.tv_price, SpanBuilder(s).color(context,s.length-2,s.length,R.color.black_text)
                .size(s.length-2,s.length,12).build())
        val flowLayout = holder.bind<com.hbcx.user.views.FlowLayout>(R.id.fl_container)
        data.rentingCarLabelList.map {
            val textView = TextView(context)
            textView.text = it.name
            textView.setTextColor(Color.parseColor("#FFA200"))
            textView.setBackgroundResource(R.drawable.bg_label_orange)
            textView.setPadding(context.dip(4),context.dip(2),context.dip(4),context.dip(2))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12F)
            flowLayout.addView(textView)
        }
    }
}