package com.hbcx.user.ui.order

import android.app.Activity
import android.content.Intent
import cn.sinata.xldutils.callPhone
import cn.sinata.xldutils.fragment.RecyclerFragment
import cn.sinata.xldutils.utils.SPUtils
import com.hbcx.user.dialogs.TipDialog
import com.hbcx.user.interfaces.TripMessageListener
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.PayActivity
import com.hbcx.user.ui.PaySuccessActivity
import com.hbcx.user.ui.grouprent.GroupRentOrderDetailActivity
import com.hbcx.user.ui.grouprent.SignActivity
import com.hbcx.user.ui.rent.RentOrderDetailActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.isBaseActivity
import com.hbcx.user.utils.requestByF
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.startActivityForResult
import org.jetbrains.anko.support.v4.toast

class GroupRentOrderFragment : RecyclerFragment() {
    private val orders = ArrayList<com.hbcx.user.beans.GroupRentOrder>()
    private val ordersAdapter by lazy {
        com.hbcx.user.adapter.GroupRentOrderAdapter(orders, object : com.hbcx.user.adapter.GroupRentOrderAdapter.OnClickCallBack {
            override fun onSign(id: Int) {
                activity?.isBaseActivity {
                    it.startActivityForResult<SignActivity>(50, "id" to id)
                }
            }

            override fun onDelete(id: Int) {
                val tipDialog = TipDialog()
                tipDialog.arguments = bundleOf("msg" to "是否删除该订单", "cancel" to "取消", "ok" to "确定")
                tipDialog.setDialogListener { p, s ->
                    HttpManager.deleteGroupOrder(id).requestByF(this@GroupRentOrderFragment) { _, _ ->
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
                    it.startActivityForResult<GroupRentOrderDetailActivity>(4, "id" to id)
                }
            }

            override fun onPay(order: com.hbcx.user.beans.GroupRentOrder) {
                activity!!.isBaseActivity {
                    it.startActivityForResult<PayActivity>(40, "money" to order.deposit, "id" to order.id, "type" to 1)
                }
            }

            override fun onCall(phone: String) {
                callPhone(phone)
            }

            override fun onCancel(id: Int, status: Int) {
                val tipDialog = TipDialog()
                tipDialog.arguments = bundleOf("msg" to if (status in 3..4) "取消订单后订金可退金额由平台跟您与商家共同商议后确定！" else "是否取消该订单", "cancel" to "我再想想", "ok" to "确定取消")
                tipDialog.setDialogListener { p, s ->
                    HttpManager.cancelRentOrder(id).requestByF(this@GroupRentOrderFragment) { _, _ ->
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
        HttpManager.getGroupRentOrderList(userId,page).requestByF(this,success = { _, data->
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
        if (requestCode == 4&&resultCode == Activity.RESULT_OK){
            showDialog()
            pullDownRefresh()
        }
        if (requestCode == 40 && resultCode == Activity.RESULT_OK && data!==null){
            page = 1
            getData()
            startActivity<PaySuccessActivity>("type" to 2,"id" to data.getIntExtra("id",0))
        }
        if (requestCode == 50&&resultCode == Activity.RESULT_OK){
            page = 1
            getData()
        }
    }
}