package cn.sinata.xldutils.view

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.sinata.xldutils.R
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import org.jetbrains.anko.dip


/**

 * 简单的带上拉自动加载更多的系统SwipeRefreshLayout+RecyclerView
 */
class SwipeRefreshRecyclerLayout : SwipeRefreshLayout, SwipeRefreshLayout.OnRefreshListener {
    lateinit var mRecyclerView: RecyclerView
    var loadMoreTextColor:Int = 0
    private var loadMoreView: TextView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var isLoadMore = false
    private var mode = Mode.Both
    private var onRefreshListener: OnRefreshListener? = null
    private var lastVisibleItem = 0
    private var lastPositions: IntArray? = null
    private var loadMoreBgColor = Color.TRANSPARENT


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    internal fun init() {
        mRecyclerView = RecyclerView(context)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.isFocusableInTouchMode = false
        mRecyclerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        loadMoreTextColor = ContextCompat.getColor(context,R.color.textColor)
        addView(mRecyclerView)
        setOnRefreshListener(this)
        setMode(mode)
    }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            val adapter = recyclerView!!.adapter
            resetLastVisibleItem()
            //拖动完成。并在最底部
            if (newState == RecyclerView.SCROLL_STATE_IDLE && adapter != null
                    && lastVisibleItem + 1 == adapter.itemCount && !isRefreshing && !isLoadMore) {
                if (adapter is HFRecyclerAdapter<*>) {
                    if (loadMoreView != null) {
                        loadMoreView?.text = "正在载入..."
                        loadMoreView?.visibility = View.VISIBLE
                    }
                    isLoadMore = true
                    if (mode == Mode.Both) {
                        //设置下拉不可用。
                        isEnabled = false
                    }
                    if (onRefreshListener != null) {
                        onRefreshListener?.onLoadMore()
                    }
                }
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING && adapter != null
                    && lastVisibleItem + 1 == adapter.itemCount && !isRefreshing && !isLoadMore) {
                //拖动中，并在最底部
                if (adapter is HFRecyclerAdapter<*>) {
                    if (loadMoreView != null) {
                        loadMoreView?.text = "松开载入更多"
                        loadMoreView?.visibility = View.VISIBLE
                    }
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }
    }

    fun smoothScrollToPosition(position: Int) {
        mRecyclerView.smoothScrollToPosition(position)
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        mAdapter = adapter
        mRecyclerView.adapter = mAdapter
        if (mode == Mode.Both || mode == Mode.Bottom) {
            initLoadMore()
        }
    }

    fun setLayoutManager(layoutManager: RecyclerView.LayoutManager) {
        mRecyclerView.layoutManager = layoutManager
    }

    fun addItemDecoration(itemDecoration: RecyclerView.ItemDecoration) {
        mRecyclerView.addItemDecoration(itemDecoration)
    }

    fun removeItemDecoration(decoration: RecyclerView.ItemDecoration) {
        mRecyclerView.removeItemDecoration(decoration)
    }

    fun setOnRefreshListener(onRefreshListener: OnRefreshListener) {
        this.onRefreshListener = onRefreshListener
    }

    fun setMode(mode: Mode) {
        this.mode = mode
        when {
            mode === Mode.None ->  {
                this.isEnabled = false
                mRecyclerView.removeOnScrollListener(onScrollListener)
            }
            mode === Mode.Both -> {
                mRecyclerView.addOnScrollListener(onScrollListener)
                initLoadMore()
            }
            mode === Mode.Bottom -> {
                mRecyclerView.addOnScrollListener(onScrollListener)
                this.isEnabled = false
                initLoadMore()
            }
            else -> {
                mRecyclerView.removeOnScrollListener(onScrollListener)
                this.isEnabled = true
            }
        }
    }

    private fun initLoadMore() {
        if (mAdapter is HFRecyclerAdapter<*>) {
            if (loadMoreView != null) {
                return
            }
            loadMoreView = TextView(context)
            loadMoreView?.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            loadMoreView?.setBackgroundColor(loadMoreBgColor)
            loadMoreView?.text = "载入更多..."
            val textSize = resources.getDimensionPixelSize(R.dimen.textSize)
            loadMoreView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
            loadMoreView?.setTextColor(loadMoreTextColor)
            loadMoreView?.gravity = Gravity.CENTER
            loadMoreView?.setPadding(0, dip(16), 0,dip(16))
            (mAdapter as HFRecyclerAdapter<*>).setFooterView(loadMoreView!!)
        }
    }

    fun setLoadMoreText(text: CharSequence) {
        //如果是不带上拉更多的页面
        if (mode == Mode.Top || mode == Mode.None) {
            initLoadMore()
        }
        if (loadMoreView != null) {
            loadMoreView?.text = text
            if (loadMoreView?.visibility != View.VISIBLE) {
                loadMoreView?.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 重新获取可见item的位置
     */
    private fun resetLastVisibleItem() {
        val layoutManager = mRecyclerView.layoutManager
        if (layoutManager != null && layoutManager is LinearLayoutManager) {
            lastVisibleItem = layoutManager.findLastVisibleItemPosition()
        } else if (layoutManager != null && layoutManager is StaggeredGridLayoutManager) {
            val staggeredGridLayoutManager = layoutManager
            if (lastPositions == null) {
                lastPositions = IntArray(staggeredGridLayoutManager.spanCount)
            }
            staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions)
            lastVisibleItem = findMax(lastPositions!!)
        }
    }

    private fun findMax(lastPositions: IntArray): Int {
        val max = lastPositions.max() ?: lastPositions[0]
        return max
    }

    override fun onRefresh() {
        //如果正在上拉。不执行操作
        if (isLoadMore) {
            return
        }
        isRefreshing = true
        if (onRefreshListener != null) {
            onRefreshListener?.onRefresh()
        }
    }
    /**
     * 设置刷新状态

     * @param refreshing 刷新状态
     */
    override fun setRefreshing(refreshing: Boolean) {

        //如果当前状态是上拉更多。
        if (isLoadMore) {
            if (mode == Mode.Both) {
                isEnabled = true
            }
        }
        //重置系统下拉刷新状态。
        if (mode == Mode.Both || mode == Mode.Top) {
            if (loadMoreView != null) {
                loadMoreView?.visibility = View.GONE
            }
            super.setRefreshing(refreshing)
        }
        //重置系统上拉更多状态。
        if (mode == Mode.Bottom || mode == Mode.Both) {
            this.isLoadMore = refreshing
            if (loadMoreView != null) {
                loadMoreView?.visibility = View.GONE
            }
        }
    }

    enum class Mode {
        None, Both, Top, Bottom
    }

    interface OnRefreshListener {
        fun onRefresh()
        fun onLoadMore()
    }
}
