package com.hbcx.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.SpanBuilder
import cn.sinata.xldutils.visible
import com.facebook.drawee.view.SimpleDraweeView
import com.hbcx.user.R
import kotlinx.android.synthetic.main.item_group_rent_car.view.*

class GroupRentCarListAdapter(data: ArrayList<com.hbcx.user.beans.GroupCarList>, private val type:String) : HFRecyclerAdapter<com.hbcx.user.beans.GroupCarList>(data, R.layout.item_group_rent_car_list) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.GroupCarList) {
        holder.bind<SimpleDraweeView>(R.id.iv_car).setImageURI(data.companyIcon)
        holder.setText(R.id.tv_brand, data.name)
        holder.setText(R.id.tv_car_num, "共${data.num}款$type")
        holder.setText(R.id.tv_order_num, "预订${data.reserveNum}次")
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

    private fun getCarLayout(car: com.hbcx.user.beans.GroupRentCarData):View{
        val view = LayoutInflater.from(context).inflate(R.layout.item_group_rent_car, null)
        view.iv_car.setImageURI(car.imgUrl)
        view.tv_title.text = "${car.brandName}${car.modelName}"
        view.tv_person_count.text = "可乘坐${car.pedestal}人"
        val s = "￥${car.money}/天"
        view.tv_price.text = SpanBuilder(s).color(context,s.length-2,s.length,R.color.black_text)
                .size(s.length-2,s.length,12).build()
        view.setOnClickListener {
            onMoreClickListener?.onCarClick(car.id)
        }
        return view
    }
    interface OnMoreClickListener{
        fun onClick(data:ArrayList<com.hbcx.user.beans.GroupRentCarData>)
        fun onCarClick(id:Int)
    }
    private var onMoreClickListener:OnMoreClickListener?=null
    fun setOnMoreClickListener(listener:OnMoreClickListener){
        onMoreClickListener = listener
    }
}