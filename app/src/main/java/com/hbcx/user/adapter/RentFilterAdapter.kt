package com.hbcx.user.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckedTextView
import com.hbcx.user.R

class RentFilterAdapter(val context:Context, val data:ArrayList<com.hbcx.user.beans.CarBrand>, var index:Int): BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_filter_brand, null) as CheckedTextView
        view.text = data[p0].name
        if (index == p0){
            view.isChecked = true
        }
        return view
    }

    fun changeIndex(index: Int){
        this.index = index
        notifyDataSetChanged()
    }

    override fun getItem(p0: Int)=  data[p0]

    override fun getItemId(p0: Int) = p0.toLong()

    override fun getCount() = data.size
}