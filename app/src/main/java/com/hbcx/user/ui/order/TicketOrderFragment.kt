package com.hbcx.user.ui.order

import android.app.Activity
import android.content.Intent
import cn.sinata.xldutils.fragment.RecyclerFragment
import cn.sinata.xldutils.utils.SPUtils
import com.hbcx.user.dialogs.TipDialog
import com.hbcx.user.interfaces.TripMessageListener
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.PayActivity
import com.hbcx.user.ui.PaySuccessActivity
import com.hbcx.user.ui.rent.RentOrderDetailActivity
import com.hbcx.user.ui.ticket.CancelTicketActivity
import com.hbcx.user.ui.ticket.EvaluateActivity
import com.hbcx.user.ui.ticket.TicketOrderDetailActivity
import com.hbcx.user.ui.ticket.TicketPaySuccessActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.isBaseActivity
import com.hbcx.user.utils.requestByF
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

class TicketOrderFragment : RecyclerFragment() {
    private val orders = ArrayList<com.hbcx.user.beans.TicketOrder>()
    private val ordersAdapter by lazy {
        com.hbcx.user.adapter.TicketOrderAdapter(orders, object : com.hbcx.user.adapter.TicketOrderAdapter.OnClickCallBack {
            override fun onRefund(id: Int) {
                activity!!.isBaseActivity {
                    it.startActivity<CancelTicketActivity>("id" to id)
                }
            }

            override fun onDelete(id: Int) {
                val tipDialog = TipDialog()
                tipDialog.arguments = bundleOf("msg" to "是否删除该订单", "cancel" to "取消", "ok" to "确定")
                tipDialog.setDialogListener { p, s ->
                    HttpManager.deleteTicketOrder(id).requestByF(this@TicketOrderFragment) { _, _ ->
                        toast("删除成功")
                        page = 1
                        showDialog()
                        getData()
                    }
                }
                tipDialog.show(fragmentManager, "cancel")
            }

            override fun onItemClick(id: Int) {
                activity!!.isBaseActivity {
                    it.startActivityForResult<TicketOrderDetailActivity>(5, "id" to id)
                }
            }

            override fun onPay(order: com.hbcx.user.beans.TicketOrder) {
                activity!!.isBaseActivity {
                    it.startActivityForResult<PayActivity>(60, "money" to order.payMoney,
                            "id" to order.id, "time" to order.createTime, "type" to 2,"orderNum" to order.orderNum)
                }
            }

            override fun onEvaluate(id: Int) {
                activity!!.isBaseActivity {
                    it.startActivityForResult<EvaluateActivity>(5, "id" to id)
                }
            }

            override fun onCancel(id: Int) {
                val tipDialog = TipDialog()
                tipDialog.arguments = bundleOf("msg" to "是否取消该订单", "cancel" to "取消", "ok" to "确定")
                tipDialog.setDialogListener { p, s ->
                    HttpManager.cancelTicketOrder(id).requestByF(this@TicketOrderFragment) { _, _ ->
                        toast("取消成功")
                        page = 1
                        showDialog()
                        getData()
                    }
                }
                tipDialog.show(fragmentManager, "cancel")
            }
        })
    }

    private val userId by lazy {
        SPUtils.instance().getInt(Const.User.USER_ID)
    }

    private var page = 1

    override fun setAdapter() = ordersAdapter

    override fun onFirstVisibleToUser() {
        showDialog()
        getData()
    }

    private fun getData() {
        HttpManager.getTicketOrderList(userId,page,0).requestByF(this,success = { _, data->
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

        ordersAdapter.notifyDataSetChanged()
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
        if (requestCode == 5&&resultCode == Activity.RESULT_OK){
            showDialog()
            pullDownRefresh()
        }
        if (requestCode == 60&&resultCode == Activity.RESULT_OK&&data!=null){
            page = 1
            getData()
            startActivity<TicketPaySuccessActivity>("id" to data.getIntExtra("id",0),
                    "orderNum" to data.getStringExtra("orderNum"))
        }
    }
}