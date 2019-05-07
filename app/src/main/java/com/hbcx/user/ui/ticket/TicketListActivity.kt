package com.hbcx.user.ui.ticket

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import cn.sinata.xldutils.activity.BaseActivity
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.toTime
import cn.sinata.xldutils.utils.toWeek
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import cn.sinata.xldutils.visible
import com.amap.api.services.help.Tip
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.rent.SelectAddressActivity
import com.hbcx.user.utils.StatusBarUtil
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_ticket_list.*
import kotlinx.android.synthetic.main.layout_ticket_list_head.view.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

class TicketListActivity : BaseActivity(), SwipeRefreshRecyclerLayout.OnRefreshListener {
    override fun onRefresh() {
        page = 1
        getData()
    }

    override fun onLoadMore() {
        page++
        getData()
    }

    private var page = 1

    private val adapter by lazy {
        com.hbcx.user.adapter.TicketListAdapter(list)
    }
    private val list = arrayListOf<com.hbcx.user.beans.TicketList>()
    private val startCode by lazy {
        intent.getStringExtra("startCode")
    }
    private val endCode by lazy {
        intent.getStringExtra("endCode")
    }
    private val enableDays by lazy {
        intent.getIntExtra("enableDays", 0)
    }

    //选择的上车点，默认为定位点
    private var lat = com.hbcx.user.YyApplication.lat
    private var lng = com.hbcx.user.YyApplication.lng

    private var start: String? = null
    private var end: String? = null
    private var date: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_list)
        StatusBarUtil.initStatus(window)
        initView()
        initClick()
        showDialog()
        getData()
    }

    private fun initClick() {
        iv_back.onClick {
            finish()
        }
        btn_date.onClick {
            startActivityForResult<CalendarActivity>(1, "enableDays" to enableDays)
        }
        tv_pre.onClick {
            showDialog()
            page = 1
            date -= 24 * 60 * 60 * 1000
            btn_date.text = date.toTime("MM月dd日 ") + date.toWeek()
            getData()
            updateBtnStatus()
        }
        tv_next.onClick {
            showDialog()
            page = 1
            date += 24 * 60 * 60 * 1000
            btn_date.text = date.toTime("MM月dd日 ") + date.toWeek()
            getData()
            updateBtnStatus()
        }
        adapter.setOnItemClickListener { view, position ->
            startActivity<LineDetailActivity>("start" to start, "end" to end,"startCode" to startCode,"endCode" to endCode,
                    "lat" to lat,"lng" to lng,"date" to date,"id" to list[position].id,"endPointId" to list[position].endPointId,
                    "startPointId" to list[position].startPointId)
        }
    }

    private fun initView() {
        start = intent.getStringExtra("start")
        end = intent.getStringExtra("end")
        date = intent.getLongExtra("date", 0L)
        tv_start.text = start
        tv_end.text = end
        btn_date.text = date.toTime("MM月dd日 ") + date.toWeek()
        mRecyclerView.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))
        mRecyclerView.setAdapter(adapter)
        initHead()
        mRecyclerView.setOnRefreshListener(this)
        updateBtnStatus()
    }

    /**
     * 更新前一天，后一天按钮状态
     */
    private fun updateBtnStatus() {
        tv_pre.isEnabled = date > System.currentTimeMillis()
        tv_next.isEnabled = date<System.currentTimeMillis()+(enableDays-2)*24*60*60*1000
        tv_pre.alpha = if (tv_pre.isEnabled) 1.0f else 0.3f
        tv_next.alpha = if (tv_next.isEnabled) 1.0f else 0.3f
    }

    private val headView by lazy {
        LayoutInflater.from(this).inflate(R.layout.layout_ticket_list_head, mRecyclerView, false)
    }

    private fun initHead() {
        if (start != com.hbcx.user.YyApplication.city){
            headView.tv_position.text = "请选择出发地"
        }
        headView.tv_position.onClick {
            startActivityForResult<SelectAddressActivity>(2, "type" to 1, "city" to start)
        }
        adapter.setHeaderView(headView)
    }

    private val startStationId by lazy {
        intent.getIntExtra("startStationId",0)
    }
    private val endStationId by lazy {
        intent.getIntExtra("endStationId",0)
    }
    private val lineType by lazy {
        intent.getIntExtra("lineType",0)
    }
    private fun getData() {
        HttpManager.getTicketLineList(page, startCode, endCode
                , lng, lat, date.toTime("yyyy-MM-dd"),startStationId,endStationId,lineType)
                .request(this, success = { _, data ->
                    mRecyclerView.isRefreshing = false
                    if (page == 1)
                        list.clear()
                    data?.let {
                        if (it.isNotEmpty()) {
                            list.addAll(it)
                        }
                        if (list.isEmpty()){
                            tv_empty.visible()
                        }
                        else
                            tv_empty.gone()
                        adapter.notifyDataSetChanged()
                    }
                }, error = { _, _ ->
                    list.clear()
                    tv_empty.visible()
                    mRecyclerView.isRefreshing = false
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            date = data.getLongExtra("date", 0L)
            btn_date.text = date.toTime("MM月dd日 ") + date.toWeek()
            updateBtnStatus()
            page = 1
            getData()
        }
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            val tip = data.getParcelableExtra<Tip>("data")
            lat = tip.point.latitude
            lng = tip.point.longitude
            headView.tv_position.text = tip.name
            page = 1
            getData()
        }
    }
}