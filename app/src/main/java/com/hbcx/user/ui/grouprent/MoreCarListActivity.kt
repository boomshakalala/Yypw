package com.hbcx.user.ui.grouprent

import android.support.v7.widget.LinearLayoutManager
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import com.amap.api.services.help.Tip
import com.hbcx.user.R
import com.hbcx.user.ui.TranslateStatusBarActivity
import org.jetbrains.anko.dip
import org.jetbrains.anko.startActivity

class MoreCarListActivity:TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.base_recyclerview_layout
    var adapter: com.hbcx.user.adapter.MoreGroupRentCarAdapter? = null
    val startTime by lazy {
        intent.getLongExtra("startTime",0)
    }
    val endTime by lazy {
        intent.getLongExtra("endTime",0)
    }
    val data by lazy {
        intent.getSerializableExtra("data") as ArrayList<com.hbcx.user.beans.GroupRentCarData>
    }

    private val duration by lazy {
        intent.getIntExtra("duration",0)
    }

    private val type by lazy {
        intent.getStringExtra("type")
    }

    private val startTip by lazy {
        intent.getParcelableExtra("startTip") as Tip
    }
    private val endTip by lazy {
        intent.getParcelableExtra("endTip") as Tip
    }
    private val startCity by lazy {
        intent.getStringExtra("startCity")
    }
    private val endCity by lazy {
        intent.getStringExtra("endCity")
    }

    override fun initClick() {
        adapter!!.setOnItemClickListener { _, position ->
            startActivity<GroupOrderConfirmActivity>("id" to data[position].id,"startTip" to startTip,"endTip" to endTip,"startTime" to startTime,"endTime" to endTime,"startCity" to startCity,"endCity" to endCity,"duration" to duration,"type" to type)
        }
    }

    override fun initView() {
        title = "车辆详情"
        val swipeRefreshLayout:SwipeRefreshRecyclerLayout  = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setMode(SwipeRefreshRecyclerLayout.Mode.None)
        swipeRefreshLayout.setPadding(dip(16),0,dip(16),0)
        swipeRefreshLayout.setLayoutManager(LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false))
        adapter = com.hbcx.user.adapter.MoreGroupRentCarAdapter(data)
        swipeRefreshLayout.setAdapter(adapter)
    }
}