package cn.sinata.xldutils.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import cn.sinata.xldutils.R
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.sysErr
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.textColor
import org.jetbrains.anko.wrapContent
import kotlin.properties.Delegates

/**
 * RecyclerView的fragment
 */
abstract class RecyclerFragment : BaseFragment() {
    /**
     *
     * 如果使用在viewpager等只回收view不回收数据的控件内，这里不能是用by lazy和val ，会导致该声明的既不能重新赋值，也指向错误的view地址，从而出现未知bug。
     * 如：RecyclerView: No adapter attached; skipping layout Error
     */
    var mSwipeRefreshLayout by Delegates.notNull<SwipeRefreshRecyclerLayout>()

    protected open var rootFl by Delegates.notNull<FrameLayout>()

    override fun contentViewId(): Int = R.layout.base_recyclerview_layout

    private var emptyView: TextView? = null

    private var adapter :RecyclerView.Adapter<*>? = null
    protected abstract fun setAdapter(): RecyclerView.Adapter<*>
    /**
     * 下拉刷新
     */
    protected open fun pullDownRefresh() {}

    /**
     * 加载更多
     */
    protected open fun loadMore() {}

    /**
     * @return 默认上拉下拉同时存在
     */
    open fun getMode(): SwipeRefreshRecyclerLayout.Mode {
        return SwipeRefreshRecyclerLayout.Mode.Both
    }

    /**
     * @return  默认垂直布局。
     */
    open fun getLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootFl = find(R.id.rootFL)
        mSwipeRefreshLayout = find(R.id.swipeRefreshLayout)
        sysErr("---getLayoutManager()--->"+getLayoutManager())
        mSwipeRefreshLayout.setLayoutManager(getLayoutManager())
        mSwipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshRecyclerLayout.OnRefreshListener {
            override fun onRefresh() {
                pullDownRefresh()
            }

            override fun onLoadMore() {
                loadMore()
            }
        })
        mSwipeRefreshLayout.setMode(getMode())
        adapter = setAdapter()
        mSwipeRefreshLayout.setAdapter(adapter)
        mSwipeRefreshLayout.isRefreshing = false

    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            adapter!!.unregisterAdapterDataObserver(observer)
        } catch (e: Exception) {
        }
        //同步置空view
        emptyView = null
    }

    protected fun addEmptyView(text:String,leftRes:Int=0,topRes:Int=0,rightRes:Int=0,bottomRes:Int=0) {
        if (emptyView == null) {
            emptyView = TextView(context)
            val lp = FrameLayout.LayoutParams(wrapContent, wrapContent)
            lp.gravity = Gravity.CENTER
            emptyView!!.layoutParams = lp
            emptyView!!.gravity = Gravity.CENTER
            emptyView!!.textColor = ContextCompat.getColor(context!!,R.color.textColor99)
            emptyView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP,16f)
            emptyView!!.setCompoundDrawablesWithIntrinsicBounds(leftRes,topRes,rightRes,bottomRes)
            rootFl.addView(emptyView,0)
            if (adapter != null) {
                adapter!!.registerAdapterDataObserver(observer)
                toggleEmptyView()
            }
        }
        emptyView!!.text = text
    }

    private val observer:RecyclerView.AdapterDataObserver = object :RecyclerView.AdapterDataObserver(){
        override fun onChanged() {
            super.onChanged()
            toggleEmptyView()
        }
    }

    private fun toggleEmptyView() {
        var count = adapter!!.itemCount
        if (adapter is HFRecyclerAdapter<*>) {
            count = (adapter as HFRecyclerAdapter<*>).getDataItemCount()
        }
        if (emptyView != null) {
            if (count > 0) {
                emptyView!!.visibility = View.GONE
            } else {
                emptyView!!.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 设置刷新状态
     * @param refreshing 刷新状态
     */
    fun setRefreshing(refreshing: Boolean) {
        mSwipeRefreshLayout.isRefreshing = refreshing
    }
}