package com.hbcx.user.adapter

import android.view.View
import android.view.ViewGroup
import cn.sinata.xldutils.adapter.BaseRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import com.amap.api.services.help.Tip
import com.hbcx.user.R

/**
 * 搜索地址adapter
 */
class TipAdapter(mData: ArrayList<Tip>) : BaseRecyclerAdapter<Tip>(mData, R.layout.item_list_search_tip) {
    private val TYPE_CLEAR = 30

    private var clearViewHolder: ViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = super.onCreateViewHolder(parent, viewType)
        if (viewType == TYPE_CLEAR && clearViewHolder != null) {
            return clearViewHolder!!
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) != TYPE_CLEAR) {
            super.onBindViewHolder(holder, position)
        }
    }

    override fun onBind(holder: ViewHolder, position: Int, data: Tip) {
        holder.setText(R.id.tv_title, data.name)
        if (data.district == "null") data.district =""
        holder.setText(R.id.tv_content, "${data.district ?: ""}${data.address ?: ""}")
    }

    override fun getItemViewType(position: Int): Int {
        val type = super.getItemViewType(position)
        if (isClearView(position)) {
            return TYPE_CLEAR
        }
        return type
    }

    private fun isClearView(position: Int): Boolean {
        return clearViewHolder != null && position == super.getItemCount()
    }

    fun setClearView(view: View) {
        this.clearViewHolder = ViewHolder(view)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        if (clearViewHolder != null) {
            return super.getItemCount() + 1
        }
        return super.getItemCount()
    }
}