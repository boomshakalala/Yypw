package com.hbcx.user.ui.user

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request

class CouponActivity : TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.base_recyclerview_layout

    override fun initClick() {
    }

    private val data = arrayListOf<com.hbcx.user.beans.Coupon>()
    private var page = 1
    private var checkIndex = -1 //选中的
    private val useType by lazy {
        intent.getIntExtra("useType",2)
    }

    private val adapter by lazy {
        com.hbcx.user.adapter.CouponAdapter(data, orderMoney)
    }

    private val userId by lazy {
        SPUtils.instance().getInt(Const.User.USER_ID)
    }

    private val orderMoney by lazy {
        intent.getDoubleExtra("orderMoney",0.0)
    }

    private val recycle by lazy {
        findViewById<SwipeRefreshRecyclerLayout>(R.id.swipeRefreshLayout)
    }

    override fun initView() {
        title = "选择优惠券"
        titleBar.addRightButton("确定", onClickListener = View.OnClickListener {
            if (checkIndex == -1)
                setResult(Activity.RESULT_CANCELED)
            else
                setResult(Activity.RESULT_OK, intent.putExtra("data", data[checkIndex]))
            finish()
        })
        (titleBar.getRightButton(0) as TextView).setTextColor(resources.getColor(R.color.black_text))

        recycle.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))
        recycle.setAdapter(adapter)
        recycle.setBackgroundColor(resources.getColor(R.color.page_bg))
        recycle.setOnRefreshListener(object : SwipeRefreshRecyclerLayout.OnRefreshListener {
            override fun onRefresh() {
                page = 1
                getData()
            }

            override fun onLoadMore() {
                page++
                getData()
            }
        })
        adapter.setOnItemClickListener { _, position ->
            if (data[position].money>orderMoney)
                return@setOnItemClickListener
            when (checkIndex) {
                position -> {
                    data[position].isChecked = false
                    checkIndex = -1
                }
                -1 -> {
                    data[position].isChecked = true
                    checkIndex = position
                }
                else -> {
                    data[checkIndex].isChecked = false
                    data[position].isChecked = true
                    checkIndex = position
                }
            }
            adapter.notifyDataSetChanged()
        }
        showDialog()
        getData()
    }

    private fun getData() {
        HttpManager.getCoupons(page, userId, 1, useType).request(this, success = { _, coupons ->
            recycle.isRefreshing = false
            if (page == 1)
                data.clear()
            coupons?.let {
                data.addAll(it)
            }
            if (data.isEmpty()) {
                if (page == 1) {
                    recycle.setLoadMoreText("暂无数据")
                } else {
                    recycle.setLoadMoreText("没有更多")
                }
            }
            adapter.notifyDataSetChanged()
        }, error = { _, _ ->
            recycle.isRefreshing = false
        })
    }
}