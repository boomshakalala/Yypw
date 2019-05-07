package com.hbcx.user.ui.order

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import cn.sinata.xldutils.fragment.RecyclerFragment
import cn.sinata.xldutils.utils.SPUtils
import com.hbcx.user.interfaces.TripMessageListener
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.trip.OrderDetailActivity
import com.hbcx.user.ui.trip.TripActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.isBaseActivity
import com.hbcx.user.utils.requestByF
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.startActivityForResult
import org.json.JSONObject

class FastOrderFragment : RecyclerFragment(), TripMessageListener {
    override fun onCancel(obj: JSONObject) {
        showDialog()
        page = 1
        getData()
    }

    companion object {
        fun instance(type: Int): Fragment {
            val fragment = FastOrderFragment()
            fragment.arguments = bundleOf("type" to type)
            return fragment
        }
    }

    private val orders = ArrayList<com.hbcx.user.beans.Order>()
    private val ordersAdapter by lazy {
        com.hbcx.user.adapter.FastOrderAdapter(orders)
    }
    private val type by lazy {
        arguments!!.getInt("type",1)
    }

    private val app by lazy {
        activity!!.application as com.hbcx.user.YyApplication
    }

    private val userId by lazy {
        SPUtils.instance().getInt(Const.User.USER_ID)
    }

    private var page = 1

    override fun setAdapter() = ordersAdapter

    override fun onFirstVisibleToUser() {
        ordersAdapter.setOnItemClickListener { _, position ->
            if (orders[position].status == 9)
                return@setOnItemClickListener
            if (orders[position].status in 1..4){
                activity!!.isBaseActivity {
                    it.startActivityForResult<TripActivity>(type,"order" to orders[position])
                }
            }else
                activity!!.isBaseActivity {
                    it.startActivityForResult<OrderDetailActivity>(type,"order" to orders[position],"type" to 1)
                }
        }
        showDialog()
        getData()
        app.addTripMessageListener(this)
    }

    private fun getData() {

        HttpManager.getFastOrderList(userId,page,type).requestByF(this,success = { _, data->
            mSwipeRefreshLayout.isRefreshing = false
            if (page == 1) {
                orders.clear()
            }
            data?.let {
                orders.addAll(it)
                if (it.isEmpty()) {
                    if (page == 1) {
                        mSwipeRefreshLayout.setLoadMoreText("暂无数据")
                    } else {
                        mSwipeRefreshLayout.setLoadMoreText("没有更多")
                    }
                }
            }
            ordersAdapter.notifyDataSetChanged()
        },error = {_,_->
            mSwipeRefreshLayout.isRefreshing = false
        })
    }

    override fun pullDownRefresh() {
        super.pullDownRefresh()
        page = 1
        getData()
    }

    override fun loadMore() {
        super.loadMore()
        page ++
        getData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == type&&resultCode == Activity.RESULT_OK){
            showDialog()
            pullDownRefresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        app.removeTripMessageListener(this)
    }
}