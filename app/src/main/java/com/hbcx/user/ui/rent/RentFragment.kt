package com.hbcx.user.ui.rent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import cn.sinata.xldutils.callPhone
import cn.sinata.xldutils.fragment.RecyclerFragment
import cn.sinata.xldutils.utils.*
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import com.amap.api.services.help.Tip
import com.hbcx.user.R
import com.hbcx.user.dialogs.SelectRentTimeDialog
import com.hbcx.user.dialogs.SelectTypeDialog
import com.hbcx.user.dialogs.TipDialog
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.PayActivity
import com.hbcx.user.ui.PaySuccessActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.isBaseActivity
import com.hbcx.user.utils.requestByF
import kotlinx.android.synthetic.main.layout_rent_head.*
import kotlinx.android.synthetic.main.layout_rent_head.view.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.startActivityForResult
import org.jetbrains.anko.support.v4.toast

class RentFragment : RecyclerFragment() {
    private var rentMain: com.hbcx.user.beans.RentMain? = null
    private var orderList: ArrayList<com.hbcx.user.beans.RentOrder> = arrayListOf()
    private val adapter = com.hbcx.user.adapter.RentMainAdapter(orderList, object : com.hbcx.user.adapter.RentMainAdapter.OnClickCallBack {
        override fun onDelete(id: Int) {
            val tipDialog = TipDialog()
            tipDialog.arguments = bundleOf("msg" to "是否删除该订单", "cancel" to "取消", "ok" to "确定")
            tipDialog.setDialogListener { p, s ->
                HttpManager.deleteRentOrder(id).requestByF(this@RentFragment) { _, _ ->
                    toast("删除成功")
                    getData()
                }
            }
            tipDialog.show(fragmentManager, "cancel")
        }

        override fun onItemClick(id: Int) {
            startActivity<RentOrderDetailActivity>("id" to id)
        }

        override fun onPay(order: com.hbcx.user.beans.RentOrder) {
            activity!!.isBaseActivity {
                startActivityForResult<PayActivity>(102, "money" to order.payMoney, "id" to order.id, "time" to order.createTime)
            }
        }

        override fun onCall(phone: String) {
            callPhone(phone)
        }

        override fun onCancel(id: Int) {
            val tipDialog = TipDialog()
            tipDialog.arguments = bundleOf("msg" to "是否取消该订单", "cancel" to "取消", "ok" to "确定")
            tipDialog.setDialogListener { p, s ->
                HttpManager.cancelRentOrder(id).requestByF(this@RentFragment) { _, _ ->
                    toast("取消成功")
                    getData()
                }
            }
            tipDialog.show(fragmentManager, "cancel")
        }

    })
    override fun setAdapter() = adapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userVisibleHint = true
    }

    //租车查询参数
    private var type = com.hbcx.user.beans.CarLevel("全部车型", 0)
    private var rentTip: Tip? = null
    private var startTime = 0L
    private var endTime = 0L

    override fun onFirstVisibleToUser() {
        mSwipeRefreshLayout.setLayoutManager(LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false))
        initHead()
        setEmptyFooter()
        showDialog(canCancel = false)
//        getData()
    }

    override fun getMode() = SwipeRefreshRecyclerLayout.Mode.Top

    fun getData() {
        HttpManager.getRentMain(SPUtils.instance().getInt(Const.User.USER_ID)).requestByF(this, success = { _, data ->
            data?.let {
                mSwipeRefreshLayout.isRefreshing = false
                it.carLevelList.add(0, com.hbcx.user.beans.CarLevel("全部车型", 0))
                this.rentMain = it
                orderList.clear()
                if (it.orderList != null && it.orderList!!.isNotEmpty()) {
                    orderList.addAll(it.orderList!!)
                    adapter.removeFooter()
                } else {
                    setEmptyFooter()
                }
            }
        }, error = { _, _ ->
            mSwipeRefreshLayout.isRefreshing = false
        })
    }

    override fun pullDownRefresh() {
        getData()
    }

    private fun setEmptyFooter() {
        adapter.setFooterView(LayoutInflater.from(context).inflate(R.layout.layout_rent_no_data, mSwipeRefreshLayout.mRecyclerView, false))
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    private fun initHead() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_rent_head, mSwipeRefreshLayout.mRecyclerView, false)
        adapter.setHeaderView(view)
        val timeMillis = System.currentTimeMillis() + 24 * 60 * 60 * 1000
        startTime = timeMillis - timeMillis % (24 * 60 * 60 * 1000) + 2 * 60 * 60 * 1000
        endTime = startTime + 24 * 60 * 60 * 1000 * 2
        view.tv_start_date.text = timeMillis.toTime("MM-dd")
        view.tv_end_date.text = (timeMillis + 24 * 60 * 60 * 1000 * 2).toTime("MM-dd")
        view.tv_start_time.text = "${timeMillis.toWeek()} 10:00"
        view.tv_end_time.text = "${(timeMillis + 24 * 60 * 60 * 1000 * 2).toWeek()} 10:00"
        view.ll_start_time.setOnClickListener {
            val selectRentTimeDialog = SelectRentTimeDialog()
            selectRentTimeDialog.arguments = bundleOf("type" to 0, "maxDay" to rentMain?.rentingDay, "rentHour" to rentMain?.rentingHour)
            selectRentTimeDialog.setDialogListener { p, s, p1, s1, s2 ->
                tv_start_time.text = s!!.substring(6, s.length)
                tv_end_time.text = s1!!.substring(6, s1.length)
                tv_start_date.text = s.substring(0, 5)
                tv_end_date.text = s1.substring(0, 5)
                tv_rent_day.text = s2
                startTime = p
                endTime = p1
            }
            selectRentTimeDialog.show(fragmentManager, "time")
        }
        view.ll_end_time.setOnClickListener {
            val selectRentTimeDialog = SelectRentTimeDialog()
            selectRentTimeDialog.arguments = bundleOf("type" to 1, "maxDay" to rentMain?.rentingDay, "rentHour" to rentMain?.rentingHour)
            selectRentTimeDialog.setDialogListener { p, s, p1, s1, s2 ->
                tv_start_time.text = s!!.substring(6, s.length)
                tv_start_time.text = s!!.substring(6, s.length)
                tv_end_time.text = s1!!.substring(6, s1.length)
                tv_start_date.text = s.substring(0, 5)
                tv_end_date.text = s1.substring(0, 5)
                tv_rent_day.text = s2
                startTime = p
                endTime = p1
            }
            selectRentTimeDialog.show(fragmentManager, "time")
        }
        view.tv_type.setOnClickListener {
            val selectTypeDialog = SelectTypeDialog()
            selectTypeDialog.arguments = bundleOf("typeList" to rentMain?.carLevelList)
            selectTypeDialog.setDialogListener { p, s ->
                tv_type.text = s
                type = rentMain!!.carLevelList[p]
            }
            selectTypeDialog.show(fragmentManager, "type")
        }

        view.tv_rent_point.setOnClickListener {
            activity!!.isBaseActivity {
                it.startActivityForResult<SelectAddressActivity>(20,"type" to 2)
            }
        }

        view.btn_search.setOnClickListener {
            if (rentTip == null) {
                toast("请选择租车地点")
                return@setOnClickListener
            }
            startActivity<CarListActivity>("rentTime" to tv_start_date.text.toString() + tv_start_time.text.toString().substring(2, 8),
                    "returnTime" to tv_end_date.text.toString() + tv_end_time.text.toString().substring(2, 8),
                    "typeList" to rentMain!!.carLevelList, "type" to type, "rentPoint" to rentTip!!,
                    "maxDay" to rentMain!!.rentingDay, "rentHour" to rentMain!!.rentingHour,
                    "startTime" to startTime,"endTime" to endTime,"duration" to tv_rent_day.text.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 20 && resultCode == Activity.RESULT_OK && data != null) {
            rentTip = data.getParcelableExtra("data")
            tv_rent_point.text = rentTip!!.name
        }
        if (requestCode == 102&&resultCode == Activity.RESULT_OK && data!=null){
            startActivity<PaySuccessActivity>("id" to data.getIntExtra("id",0))
        }
    }
}