package com.hbcx.user.ui.ticket

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import cn.sinata.xldutils.activity.BaseActivity
import cn.sinata.xldutils.adapter.ImagePagerAdapter
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.ioScheduler
import cn.sinata.xldutils.rxutils.ResultException
import cn.sinata.xldutils.utils.toTime
import cn.sinata.xldutils.utils.toWeek
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import cn.sinata.xldutils.visible
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.help.Tip
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.hbcx.user.R
import com.hbcx.user.YyApplication
import com.hbcx.user.network.Api
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.H5Activity
import com.hbcx.user.ui.MainActivity
import com.hbcx.user.ui.rent.SelectAddressActivity
import com.hbcx.user.utils.StatusBarUtil
import com.hbcx.user.utils.request
import io.reactivex.Flowable
import io.reactivex.subscribers.DisposableSubscriber
import kotlinx.android.synthetic.main.activity_line_list.*
import kotlinx.android.synthetic.main.layout_line_list_head.view.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

/**
 * 热门线路-线路列表
 */
class LineListActivity : BaseActivity() {
    private var page = 1
    private val adapter by lazy {
        com.hbcx.user.adapter.TicketListAdapter(data)
    }
    private val data = arrayListOf<com.hbcx.user.beans.TicketList>()
    private val enableDays by lazy {
        intent.getIntExtra("enableDays",0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_list)
        StatusBarUtil.initStatus(window)
        initView()
        initClick()
        getData()
    }

    private fun initClick() {
        iv_back.onClick {
            finish()
        }
        adapter.setOnItemClickListener { view, position ->
            startActivity<LineDetailActivity>("start" to start, "end" to end,"startCode" to data[position].startCityCode,"endCode" to data[position].endCityCode,
                    "lat" to lat,"lng" to lng,"date" to date,"id" to data[position].id,"endPointId" to data[position].endPointId,
                    "startPointId" to data[position].startPointId)
        }
    }

    private val id by lazy {
        intent.getIntExtra("id",0)
    }
    private var start: String? = null
    private var end: String? = null
    private var content: String? = null  //协议路径
    private var date: Long = System.currentTimeMillis()
    private var lat = com.hbcx.user.YyApplication.lat
    private var lng = com.hbcx.user.YyApplication.lng

    private fun initView() {
        mRecyclerView.setMode(SwipeRefreshRecyclerLayout.Mode.Both)
        mRecyclerView.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))
        mRecyclerView.setAdapter(adapter)
        mRecyclerView.setOnRefreshListener(object :SwipeRefreshRecyclerLayout.OnRefreshListener{
            override fun onRefresh() {
                page = 1
                getData()
            }

            override fun onLoadMore() {
                getData()
            }
        })
        initHead()
    }

    private val headView by lazy {
        LayoutInflater.from(this).inflate(R.layout.layout_line_list_head, mRecyclerView, false)
    }

    private fun initHead() {
        getPoi(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)
        headView.tv_start_time.text = date.toTime("MM-dd ") + date.toWeek()
        headView.tv_start_point.onClick {
            startActivityForResult<SelectAddressActivity>(2,"type" to 1,"city" to start)
        }
        headView.tv_start_time.onClick {
            startActivityForResult<CalendarActivity>(1,"enableDays" to enableDays)
        }
        headView.tv_detail.onClick {
            startActivity<H5Activity>("title" to end,"url" to Api.LINE_DESCRIBE+content)
        }
        adapter.setHeaderView(headView)
    }


    private fun getData() {
        HttpManager.getSpecialLineDeatail(page, com.hbcx.user.YyApplication.selectCityId,lng,lat,date.toTime("yyyy-MM-dd"),id)
                .request(this,success = {_,data->
                    mRecyclerView.isRefreshing = false
                    data?.let {
                        if (end == null){  //第一次请求
                            start = YyApplication.selectCityName
                            end = it.name
                            content = it.content
                            tv_start.text = start
                            tv_end.text = end
                            headView.tv_title.text = it.name
                            headView.tv_content.text = it.remark
                            headView.view_pager.adapter = ImagePagerAdapter(supportFragmentManager, it.imgUrl.map { it.imgUrl } as ArrayList<String>)
                        }
                        if (page == 1 )
                            this.data.clear()
                        if (it.lineList.isNotEmpty())
                            this.data.addAll(it.lineList)
                        else{
                            if (page != 1)
                                mRecyclerView.setLoadMoreText("没有更多班次了")
                        }
                        if (this.data.isEmpty())
                            headView.tv_empty.visible()
                        else{
                            headView.tv_empty.gone()
                            page++
                            adapter.notifyDataSetChanged()
                        }
                    }
                },error = {_,_ ->
                    mRecyclerView.isRefreshing = false
                })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            date = data.getLongExtra("date", 0L)
            headView.tv_start_time.text = date.toTime("MM月dd日 ") + date.toWeek()
            page = 1
            getData()
        }
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            val tip = data.getParcelableExtra<Tip>("data")
            headView.tv_start_point.text = tip.name
            lat = tip.point.latitude
            lng = tip.point.longitude
            page = 1
            getData()
        }
    }

    private var startTip: Tip? = null
    private var poiSearchDisposable: DisposableSubscriber<PoiResult>? = null
    private fun getPoi(lat: Double?, lng: Double?) {
        if (lat == null || lng == null) {
            return
        }
        //如果上一次还没处理完，取消订阅
        if (poiSearchDisposable != null && !poiSearchDisposable!!.isDisposed) {
            poiSearchDisposable?.dispose()
        }
        poiSearchDisposable = object : DisposableSubscriber<PoiResult>() {
            override fun onError(t: Throwable?) {

            }

            override fun onComplete() {

            }

            override fun onNext(t: PoiResult?) {
                val address = if (t != null) {
                    t.pois.sortBy { it.distance }
                    if (t.pois.isNotEmpty()) {
                        startTip = Tip()
                        startTip!!.setPostion(t.pois[0].latLonPoint)
                        startTip!!.name = t.pois[0].title
                        startTip!!.adcode = t.pois[0].adCode
                        t.pois[0].title
                    } else {
                        startTip = null
                        null
                    }
                } else {
                    startTip = null
                    null
                }
                runOnUiThread {
                    headView.tv_start_point.text = address
                }
            }
        }
        val query = PoiSearch.Query("", "190000", "")
        val poiSearch = PoiSearch(this, query)
        poiSearch.bound = PoiSearch.SearchBound(LatLonPoint(lat, lng), 1000)
        Flowable.just(poiSearch).ioScheduler().flatMap {
            try {
                val result = poiSearch.searchPOI()
                if (result == null) {
                    Flowable.error(ResultException(""))
                } else
                    Flowable.just(result)
            } catch (e: Exception) {
                e.printStackTrace()
                Flowable.error<PoiResult>(e)
            }
        }.subscribe(poiSearchDisposable)
    }
}