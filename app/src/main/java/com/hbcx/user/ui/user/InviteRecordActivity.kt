package com.hbcx.user.ui.user

import android.support.v7.widget.LinearLayoutManager
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip

class InviteRecordActivity : TranslateStatusBarActivity(),SwipeRefreshRecyclerLayout.OnRefreshListener {
    override fun onRefresh() {
        page = 1
        getData()
    }

    override fun onLoadMore() {
        page++
        getData()
    }

    override fun setContentView() = R.layout.base_recyclerview_layout

    private var page = 1
    private lateinit var swipeRefreshLayout: SwipeRefreshRecyclerLayout
    private val data = arrayListOf<com.hbcx.user.beans.InviteRecord>()
    private val adapter by lazy {
        com.hbcx.user.adapter.InviteRecordAdapter(data)
    }
    private val userId by lazy {
        SPUtils.instance().getInt(Const.User.USER_ID)
    }

    override fun initClick() {
    }

    override fun initView() {
        title = "邀请记录"
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.backgroundResource = R.color.bg_grey
        swipeRefreshLayout.setPadding(0, dip(10), 0, 0)
        swipeRefreshLayout.setLayoutManager(LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false))
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setAdapter(adapter)
        showDialog()
        getData()
    }

    private fun getData(){
        HttpManager.getInviteRecord(userId,page).request(this,success ={_,data->
            swipeRefreshLayout.isRefreshing = false
            data?.let {
                if (page == 1)
                    this.data.clear()
                this.data.addAll(it)
                if (this.data.isEmpty())
                    swipeRefreshLayout.setLoadMoreText("暂无邀请记录")
                adapter.notifyDataSetChanged()
            }
        },error = {_,_->
            swipeRefreshLayout.isRefreshing = false
        } )
    }
}