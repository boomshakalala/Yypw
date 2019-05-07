package cn.sinata.xldutils.adapter

import android.support.annotation.LayoutRes
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import cn.sinata.xldutils.adapter.util.ViewHolder


/**
 * 可添加头部视图和尾部视图的RecyclerView的adapter
 */
abstract class HFRecyclerAdapter<T>(data:ArrayList<T>, @LayoutRes layoutId:Int) : BaseRecyclerAdapter<T>(data,layoutId) {
    protected val TYPE_HEADER = -3//头部
    protected val TYPE_NORMAL = 0//普通
    protected val TYPE_FOOTER = -1//尾部
    var headerViewHolder: ViewHolder? = null
    var footerViewHolder: ViewHolder? = null

    override fun getItemViewType(position: Int): Int {
        if (isHeader(position)) {
            return TYPE_HEADER
        }
        if (isFooter(position)) {
            return TYPE_FOOTER
        }
        return TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == TYPE_HEADER) {
            return headerViewHolder!!
        } else if (viewType == TYPE_FOOTER) {
            return footerViewHolder!!
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (!isHeader(position) && !isFooter(position)) {
            super.onBindViewHolder(holder, position - if (hasHeader()) 1 else 0)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val layoutManager = recyclerView!!.layoutManager
        if (layoutManager is GridLayoutManager) {
            val gridSpanSizeLookup = GridSpanSizeLookup(layoutManager)
            layoutManager.spanSizeLookup = gridSpanSizeLookup
        }
    }

    override fun getItemCount(): Int {
        var count = super.getItemCount()
        if (hasHeader()) {
            count ++
        }
        if (hasFooter()) {
            count ++
        }
        return count
    }

    fun getDataItemCount(): Int {
        return super.getItemCount()
    }

    /**
     * 设置headerView
     * @param view    headerView
     */
    fun setHeaderView(view: View) {
        if (headerViewHolder == null || view != headerViewHolder?.itemView) {
            headerViewHolder = ViewHolder(view)
            notifyDataSetChanged()
        }
    }

    /**
     * 设置footerView
     * @param view footerView
     */
    fun setFooterView(view: View) {

        if (footerViewHolder == null || view != footerViewHolder?.itemView) {
            footerViewHolder = ViewHolder(view)
            notifyDataSetChanged()
        }
    }

    fun removeFooter(){
        footerViewHolder = null
        notifyDataSetChanged()
    }

    /**
     * 是否有headerView
     * @return 是否有headerView
     */
    protected fun hasHeader(): Boolean {
        return headerViewHolder != null
    }

    /**
     * 是否有footerView
     * @return 是否有footerView
     */
    protected fun hasFooter(): Boolean {
        return footerViewHolder != null
    }

    /**
     * 是否是头view
     * @param position 当前位置
     * *
     * @return 是否头view
     */
    private fun isHeader(position: Int): Boolean {
        return hasHeader() && position == 0
    }

    /**
     * 当前位置是否是footerView
     * @param position 当前位置
     * *
     * @return 是否footerView
     */
    private fun isFooter(position: Int): Boolean {
        return hasFooter() && position == getDataItemCount() + if (hasHeader()) 1 else 0
    }

    private inner class GridSpanSizeLookup internal constructor(private val layoutManager: GridLayoutManager) : GridLayoutManager.SpanSizeLookup() {

        override fun getSpanSize(position: Int): Int {
            if (isFooter(position) || isHeader(position)) {
                return layoutManager.spanCount
            }
            return 1
        }
    }
}