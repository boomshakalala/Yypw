package com.hbcx.user.adapter

import android.view.View
import android.widget.ImageView
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.gone
import com.hbcx.user.R

class PersonAdapter(data: ArrayList<com.hbcx.user.beans.Driver>, private val isPassenger: Boolean, private val checkable:Boolean) : HFRecyclerAdapter<com.hbcx.user.beans.Driver>(data, R.layout.item_driver) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.Driver) {
        holder.setText(R.id.tv_name, "${data.name} ${data.getPhoneStr()}")
        holder.setText(R.id.tv_id_card, "身份证号 ${data.idCards}")
        val imageView = holder.bind<ImageView>(R.id.iv_button)
        imageView.setImageResource(if (data.isChecked) R.mipmap.ic_checked else R.mipmap.ic_unchecked)
        if (!checkable){
            imageView.gone()
        }
        val edit = holder.bind<ImageView>(R.id.iv_edit)
        edit.setOnClickListener {
            onItemClickListener?.onEditClick(data)
        }
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(data)
        }
        if (isPassenger)
            holder.bind<View>(R.id.tag_driver).visibility = if (data.licenseOrNot == 1) View.VISIBLE else View.GONE
    }

    interface OnClickCallback {
        fun onItemClick(data: com.hbcx.user.beans.Driver)
        fun onEditClick(data: com.hbcx.user.beans.Driver)
    }

    private var onItemClickListener: OnClickCallback? = null
    fun setOnClickCallback(callback: OnClickCallback) {
        this.onItemClickListener = callback
    }
}