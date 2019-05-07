package com.hbcx.user.ui.rent

import android.support.v7.widget.LinearLayoutManager
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import com.amap.api.services.help.Tip
import com.hbcx.user.R
import com.hbcx.user.dialogs.LoginDialog
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import org.jetbrains.anko.dip
import org.jetbrains.anko.startActivity

class CarCompanyListActivity:TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.base_recyclerview_layout
    var adapter: com.hbcx.user.adapter.RentCarCompanyAdapter? = null
    val startTime by lazy {
        intent.getLongExtra("startTime",0)
    }
    val endTime by lazy {
        intent.getLongExtra("endTime",0)
    }
    val data by lazy {
        intent.getSerializableExtra("data") as ArrayList<com.hbcx.user.beans.CarData>
    }

    private val duration by lazy {
        intent.getStringExtra("duration")
    }

    val tip by lazy {
        intent.getParcelableExtra("tip") as Tip
    }
    override fun initClick() {
        adapter!!.setOnItemClickListener { view, position ->
            if (SPUtils.instance().getInt(Const.User.USER_ID) == -1){
                LoginDialog().show(supportFragmentManager, "login")
                return@setOnItemClickListener
            }
            startActivity<OrderConfirmActivity>("id" to data[position].id,"tip" to tip,"startTime" to startTime,"endTime" to endTime,"duration" to duration)
        }
    }

    override fun initView() {
        title = "车辆详情"
        val swipeRefreshLayout:SwipeRefreshRecyclerLayout  = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setMode(SwipeRefreshRecyclerLayout.Mode.None)
        swipeRefreshLayout.setPadding(dip(16),0,dip(16),0)
        swipeRefreshLayout.setLayoutManager(LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false))
        adapter = com.hbcx.user.adapter.RentCarCompanyAdapter(data)
        swipeRefreshLayout.setAdapter(adapter)
    }
}