package cn.sinata.xldutils.view

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import cn.sinata.xldutils.R
import cn.sinata.xldutils.adapter.BaseRecyclerAdapter
import cn.sinata.xldutils.sysErr
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.textColor

/**
 * RecyclerView实现的wheel,因为item的textview是写死的50dp所以，如果要展示n行，请设置view高度为n*50dp
 */
class RWheelView : RecyclerView {

    private val mItems = ArrayList<WheelItem>()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    init {
        //默认垂直
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        //滑动停止居中
        val snapHelper = WheelSnapHelper()
        snapHelper.attachToRecyclerView(this)
        adapter = WheelAdapter(mItems)

        addOnScrollListener(object : OnScrollListener() {
            var mScrolled = false
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE && mScrolled) {
                    mScrolled = false
                    postDelayed({
                        val p = getSelectPosition()
                        mItems.forEachWithIndex { i, wheelItem ->
                            wheelItem.isSelected = i == p
                        }
                        adapter?.notifyDataSetChanged()
                        getSelectItem()
                    }, 200)//延时200毫秒
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dx != 0 || dy != 0) {
                    if (!mScrolled) {
                        mItems.forEach { it.isSelected = false }
                        adapter?.notifyDataSetChanged()
                    }
                    mScrolled = true
                }
            }
        })
    }

    fun setItems(items: ArrayList<String>) {
        mItems.clear()
        //前后增加一个占位空白textView
        mItems.add(WheelItem(""))
        items.forEach {
            mItems.add(WheelItem(it))
        }
        mItems.add(WheelItem(""))
        adapter?.notifyDataSetChanged()
    }

    private fun getSelectPosition(): Int {
        var position = 0
        if (layoutManager != null && layoutManager is LinearLayoutManager) {
            position = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        }
        return position + 1
    }

    fun getSelectItem(): String {
        var position = 0
        if (layoutManager != null && layoutManager is LinearLayoutManager) {
            position = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (position < adapter!!.itemCount - 1) {
                sysErr(mItems[position + 1])
            }
        }
        return mItems[position + 1].content
    }

    inner class WheelAdapter(mData: ArrayList<WheelItem>) : BaseRecyclerAdapter<WheelItem>(mData, R.layout.item_wheel_text) {
        override fun onBind(holder: cn.sinata.xldutils.adapter.util.ViewHolder, position: Int, data: WheelItem) {
            val contentView = holder.bind<TextView>(R.id.tv_content)
            contentView.text = data.content
            if (data.isSelected) {
                contentView.textColor = Color.parseColor("#333333")
            } else {
                contentView.textColor = Color.parseColor("#999999")
            }
        }
    }

    inner class WheelSnapHelper : LinearSnapHelper() {
        override fun calculateDistanceToFinalSnap(layoutManager: LayoutManager, targetView: View): IntArray? {
            val distance = super.calculateDistanceToFinalSnap(layoutManager, targetView)
            getSelectItem()
            return distance
        }
    }

    data class WheelItem(val content: String) {
        var isSelected = false
    }
}