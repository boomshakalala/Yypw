package com.hbcx.user.ui.ticket

import android.support.v7.widget.LinearLayoutManager
import cn.sinata.xldutils.fragment.RecyclerFragment
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.MainActivity
import com.hbcx.user.utils.requestByF
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.support.v4.startActivity

class TicketLineFragment: RecyclerFragment() {
    private val lines = arrayListOf<com.hbcx.user.beans.TicketLine>()
    private val adapter = com.hbcx.user.adapter.TicketLineAdapter(lines)
    override fun setAdapter() = adapter
    private var page = 1
    private val enableDays by lazy {
        val mainActivity = activity as MainActivity
        mainActivity.enableDays
    }

    companion object {
        fun getInstance(lineTypeId:Int):TicketLineFragment{
            val fragment = TicketLineFragment()
            fragment.arguments = bundleOf("lineTypeId" to lineTypeId)
            return fragment
        }
    }

    private val lineTypeId by lazy {
        arguments?.getInt("lineTypeId")?:0
    }

    override fun onFirstVisibleToUser() {
        rootFl.backgroundResource = R.color.bg_grey
        mSwipeRefreshLayout.setLayoutManager(LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false))
        adapter.setOnItemClickListener { view, position ->
            startActivity<LineListActivity>("id" to lines[position].id,"enableDays" to enableDays)
        }
    }

    override fun pullDownRefresh() {
        page = 1
        getData()
    }

    override fun loadMore() {
        getData()
    }

    private fun getData(){
        HttpManager.getLineList(page,lineTypeId, com.hbcx.user.YyApplication.selectCityId).requestByF(this,success = { _, data->
            mSwipeRefreshLayout.isRefreshing = false
            data?.let {
                if (page == 1)
                    lines.clear()
                if (it.isNotEmpty()){
                    lines.addAll(it)
                    page++
                }else{
                    if (page == 1)
                        mSwipeRefreshLayout.setLoadMoreText("暂无数据")
                    else
                        mSwipeRefreshLayout.setLoadMoreText("没有更多")
                }
                adapter.notifyDataSetChanged()
            }
        },error = {_,_->
            mSwipeRefreshLayout.isRefreshing = false
        })
    }

    fun refresh(){
        mSwipeRefreshLayout.isRefreshing = true
        pullDownRefresh()
    }
}