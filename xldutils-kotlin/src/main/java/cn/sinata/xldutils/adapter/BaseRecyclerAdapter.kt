package cn.sinata.xldutils.adapter

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.sinata.xldutils.adapter.util.ViewHolder
import kotlin.properties.Delegates

/**
 *基础recyclerView的Adapter
 */
abstract class BaseRecyclerAdapter<T>(var mData: ArrayList<T>,@LayoutRes layoutId: Int) : RecyclerView.Adapter<ViewHolder>() {
    private var mLayoutId: Int = layoutId
    var context: Context by Delegates.notNull()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent!!.context
        if (mLayoutId <= 0) {
            mLayoutId = android.R.layout.simple_list_item_1
        }
        val view = LayoutInflater.from(parent.context).inflate(mLayoutId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (!disableItemClick()) {
            initItemClickListener(holder, position)
        }
        val t = mData[position%mData.size]
        onBind(holder!!, position%mData.size, t)
    }

    override fun getItemCount(): Int = this.mData.size
    abstract fun onBind(holder: ViewHolder, position: Int, data: T)

    protected var mOnItemClickListener: OnItemClickListener? = null

    /**
     * 设置OnItemClickListener
     * @param mOnItemClickListener OnItemClickListener
     */
    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener
    }
    /**
     * 设置OnItemClickListener
     * @param mOnItemClickListener OnItemClickListener
     */
    fun setOnItemClickListener(listener: (view:View?,position:Int) -> Unit) {
        this.mOnItemClickListener = object :OnItemClickListener{
            override fun onItemClick(view: View?, position: Int) {
                listener(view,position%mData.size)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    private fun initItemClickListener(holder: ViewHolder?, position: Int) {
        //如果设置了回调，则设置点击事件
        holder?.itemView?.setOnClickListener {
            if (mOnItemClickListener != null) {
                mOnItemClickListener?.onItemClick(holder.itemView, position%mData.size)
            }
        }
    }

    open fun disableItemClick() = false
}