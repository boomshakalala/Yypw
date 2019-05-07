package com.hbcx.user.adapter

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.SpanBuilder
import cn.sinata.xldutils.visible
import com.facebook.drawee.view.SimpleDraweeView
import com.hbcx.user.R
import com.hbcx.user.beans.CarData
import kotlinx.android.synthetic.main.item_rent_car.view.*
import org.jetbrains.anko.dip

class CarListAdapter(data: ArrayList<com.hbcx.user.beans.CarList>) : HFRecyclerAdapter<com.hbcx.user.beans.CarList>(data, R.layout.item_rent_car_list) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.CarList) {
        holder.bind<SimpleDraweeView>(R.id.iv_car).setImageURI(data.imgUrl)
        holder.setText(R.id.tv_brand, data.brandName + data.modelName)
        holder.setText(R.id.tv_type, data.levelName)
        val more = holder.bind<View>(R.id.iv_more)
        more.setOnClickListener {
            onMoreClickListener?.onClick(data.carList)
        }
        val layout = holder.bind<LinearLayout>(R.id.ll_car)
        layout.removeAllViews()
        if (data.carList.isNotEmpty()) {
            if (data.carList.size <= 3) {
                more.gone()
                data.carList.map {
                    layout.addView(getCarLayout(it))
                }
            } else {
                more.visible()
                for (i in 0..2){
                    layout.addView(getCarLayout(data.carList[i]))
                }
            }
        } else
            more.gone()
    }

    private fun getCarLayout(car: CarData):View{
        val view = LayoutInflater.from(context).inflate(R.layout.item_rent_car, null)
        view.iv_car.setImageURI(car.company_icon)
        view.tv_title.text = car.name
        view.tv_distance.text = String.format("%.2fkm", car.distance.toDouble() / 1000)
        val s = "￥${car.day_money}/天"
        view.tv_price.text = SpanBuilder(s).color(context,s.length-2,s.length,R.color.black_text)
                .size(s.length-2,s.length,12).build()
        car.rentingCarLabelList.map {
            val textView = TextView(context)
            textView.text = it.name
            textView.setTextColor(Color.parseColor("#FFA200"))
            textView.setBackgroundResource(R.drawable.bg_label_orange)
            textView.setPadding(context.dip(4),context.dip(2),context.dip(4),context.dip(2))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12F)
            view.fl_container.addView(textView)
        }
        view.setOnClickListener {
            onMoreClickListener?.onCarClick(car.id)
        }
        return view
    }
    interface OnMoreClickListener{
        fun onClick(data:ArrayList<com.hbcx.user.beans.CarData>)
        fun onCarClick(id:Int)
    }
    private var onMoreClickListener:OnMoreClickListener?=null
    fun setOnMoreClickListener(listener:OnMoreClickListener){
        onMoreClickListener = listener
    }
}