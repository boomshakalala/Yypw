package com.hbcx.user.ui.ticket

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import cn.sinata.xldutils.visible
import com.hbcx.user.R
import com.hbcx.user.adapter.SearchStationAdapter
import com.hbcx.user.beans.Station
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_search_station.*

class SearchStationActivity : TranslateStatusBarActivity(), TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        if (s.isNullOrEmpty()){
            iv_del.gone()
        }else{
            iv_del.visible()
            page = 1
            search(s.toString())
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun setContentView() = R.layout.activity_search_station

    override fun initClick() {
        adapter.setOnItemClickListener { _, position ->
            setResult(Activity.RESULT_OK,intent.putExtra("stationId", stations[position].stationId)
                    .putExtra("lineType", stations[position].lineType)
                    .putExtra("name", stations[position].stationName))
            finish()
        }
        iv_del.setOnClickListener {
            et_search.setText("")
            root.requestFocus()
        }
        tv_cancel.setOnClickListener {
            finish()
        }
    }

    private val isStart by lazy {
        intent.getBooleanExtra("isStart",false)
    }
    private val lineType by lazy { //线路类型，结束站点使用，由开始站点获得
        intent.getIntExtra("lineType",0)
    }
    private val stationId by lazy { //开始站点id，结束站点使用，由开始站点获得
        intent.getIntExtra("stationId",0)
    }
    private val cityCode by lazy {
        intent.getStringExtra("cityCode")
    }
    private val stations = arrayListOf<Station>()
    private val adapter by lazy {
        SearchStationAdapter(stations)
    }
    override fun initView() {
        title = if (isStart) "选择上车站" else "选择下车站"
        swipeRefreshLayout.setMode(SwipeRefreshRecyclerLayout.Mode.Bottom)
        swipeRefreshLayout.setLayoutManager(LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false))
        swipeRefreshLayout.setAdapter(adapter)
        swipeRefreshLayout.setOnRefreshListener(object :SwipeRefreshRecyclerLayout.OnRefreshListener{
            override fun onRefresh() {
                page = 1
                search(et_search.text.toString())
            }

            override fun onLoadMore() {
                page++
                search(et_search.text.toString())
            }
        })
        et_search.addTextChangedListener(this)
        swipeRefreshLayout.setLoadMoreText("")
    }

    private var page = 1
    private fun search(key:String){
        if (isStart)
            HttpManager.searchStartStation(cityCode,key,page).request(this){_,data->
                swipeRefreshLayout.isRefreshing = false
                data?.let {
                    if (page == 1)
                        stations.clear()
                    stations.addAll(it)
                    adapter.notifyDataSetChanged()
                }
            }
        else
            HttpManager.searchEndStation(cityCode,key,page,lineType,stationId).request(this){_,data->
                swipeRefreshLayout.isRefreshing = false
                data?.let {
                    if (page == 1)
                        stations.clear()
                    stations.addAll(it)
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
