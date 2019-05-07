package com.hbcx.user.ui.trip


import android.support.v7.widget.LinearLayoutManager
import cn.sinata.xldutils.utils.hideIdCard
import cn.sinata.xldutils.utils.optFloat
import cn.sinata.xldutils.utils.optJsonArray
import cn.sinata.xldutils.utils.optString

import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_driver_info.*
import kotlinx.android.synthetic.main.header_driver_info.view.*

/**
 * 司机信息
 */
class DriverInfoActivity : TranslateStatusBarActivity(),SwipeRefreshRecyclerLayout.OnRefreshListener {
    override fun setContentView(): Int = R.layout.activity_driver_info

    override fun initClick() {
    }

    override fun initView() {
        title = "司机主页"

        mSwipeRefreshLayout.setLayoutManager(LinearLayoutManager(this))
        mSwipeRefreshLayout.setAdapter(adapter)

        mSwipeRefreshLayout.setOnRefreshListener(this)

        adapter.setHeaderView(headerView)

        showDialog()
        getData()
    }

    private val id by lazy {
        intent.getIntExtra("id",0)
    }

    private var page = 1

    private val mAppraises = ArrayList<com.hbcx.user.beans.Appraise>()

    private val adapter by lazy {
        com.hbcx.user.adapter.AppraiseAdapter(mAppraises)
    }

    private val headerView by lazy {
        layoutInflater.inflate(R.layout.header_driver_info,mSwipeRefreshLayout.mRecyclerView,false)
    }


    private fun getData() {
        HttpManager.getDriverInfo(id,page).request(this,success = { _, data->
            mSwipeRefreshLayout.isRefreshing = false
            if (page == 1) {
                mAppraises.clear()
            }
            data?.let {
                if (page == 1) {
                    val name = it.optString("nickName")
                    val plate = it.optString("licensePlate")
                    val brand = it.optString("brandName")
                    val idcard = it.optString("idCards")
                    val headImg = it.optString("imgUrl")
                    val model = it.optString("modelName")
                    val score = it.optFloat("score")
                    headerView.headView.setImageURI(headImg)
                    headerView.tv_name.text = name
                    val c = "$plate $brand$model"
                    headerView.tv_content.text = c
                    headerView.tv_idCard.text = String.format("身份证:%s",idcard.hideIdCard())
                    headerView.tv_score.text = String.format("%.1f",score)
                }
                val list = it.optJsonArray("evaluateList")
                val temp = Gson().fromJson<ArrayList<com.hbcx.user.beans.Appraise>>(list,object :TypeToken<ArrayList<com.hbcx.user.beans.Appraise>>(){}.type)
                if (temp.isEmpty()) {
                    if (page == 1) {
                        mSwipeRefreshLayout.setLoadMoreText("暂无数据")
                    } else {
                        mSwipeRefreshLayout.setLoadMoreText("没有更多了")
                        page--
                    }
                } else {
                    mAppraises.addAll(temp)
                }
            }
            adapter.notifyDataSetChanged()
        }){_,_ ->
            mSwipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onRefresh() {
        page = 1
        getData()
    }

    override fun onLoadMore() {
        page ++
        getData()
    }
}
