package com.hbcx.user.ui.ticket

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.sinata.xldutils.fragment.RecyclerFragment
import cn.sinata.xldutils.utils.*
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import cn.sinata.xldutils.visible
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationListener
import com.hbcx.user.R
import com.hbcx.user.db.DBHelper
import com.hbcx.user.db.HistoryDBManager
import com.hbcx.user.dialogs.TipDialog
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.MainActivity
import com.hbcx.user.ui.PayActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.isBaseActivity
import com.hbcx.user.utils.requestByF
import kotlinx.android.synthetic.main.layout_ticket_head.view.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColorResource

class TicketMainFragment : RecyclerFragment(), AMapLocationListener {
    private var startCity = ""
    private var startId = 0L
    private var startStationId = 0
    private var endStationId = 0
    private var startCode = "" //出发城市编号
    private var endCode = "" //到达城市编号
    private var endCity = ""
    private var date = System.currentTimeMillis()
    private var isViewCreated = false;
    private val enableDays by lazy {
        val mainActivity = activity as MainActivity
        mainActivity.enableDays
    }
    private var page = 1

    override fun onLocationChanged(p0: AMapLocation?) {
        p0?.let {
            HttpManager.isCityOpen(it.adCode).requestByF(this) { _, data ->
                data?.let {
                    if (it.optBoolean("isOpen")) {
                        startCode = p0.adCode.replaceRange(4, 6, "00")
//                        startCity = p0.city
                        startId = it.optLong("id")
//                        headView.tv_start.text = startCity
                    }
                }
            }
        }
    }

    private val orders = arrayListOf<com.hbcx.user.beans.TicketOrder>()

    private val dbManager by lazy {
        HistoryDBManager()
    }

    private val adapter by lazy {
        com.hbcx.user.adapter.TicketOrderAdapter(orders, object : com.hbcx.user.adapter.TicketOrderAdapter.OnClickCallBack {
            override fun onRefund(id: Int) {
                activity!!.isBaseActivity {
                    it.startActivity<CancelTicketActivity>("id" to id)
                }
            }

            override fun onPay(order: com.hbcx.user.beans.TicketOrder) {
                activity!!.isBaseActivity {
                    it.startActivityForResult<PayActivity>(14, "money" to order.payMoney, "id" to order.id,
                            "orderNum" to order.orderNum, "time" to order.createTime, "type" to 2)
                }
            }

            override fun onEvaluate(id: Int) {
                startActivity<EvaluateActivity>("id" to id)
            }

            override fun onCancel(id: Int) {
                val tipDialog = TipDialog()
                tipDialog.arguments = bundleOf("msg" to "是否取消该订单", "cancel" to "取消", "ok" to "确定")
                tipDialog.setDialogListener { p, s ->
                    HttpManager.cancelTicketOrder(id).requestByF(this@TicketMainFragment) { _, _ ->
                        toast("取消成功")
                        page = 1
                        showDialog()
                        getData()
                    }
                }
                tipDialog.show(fragmentManager, "cancel")
            }

            override fun onDelete(id: Int) {
                val tipDialog = TipDialog()
                tipDialog.arguments = bundleOf("msg" to "是否删除该订单", "cancel" to "取消", "ok" to "确定")
                tipDialog.setDialogListener { p, s ->
                    HttpManager.deleteTicketOrder(id).requestByF(this@TicketMainFragment) { _, _ ->
                        toast("删除成功")
                        page = 1
                        showDialog()
                        getData()
                    }
                }
                tipDialog.show(fragmentManager, "cancel")
            }

            override fun onItemClick(id: Int) {
                startActivity<TicketOrderDetailActivity>("id" to id)
            }
        })
    }

    private val headView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.layout_ticket_head, mSwipeRefreshLayout.mRecyclerView, false)
    }

    override fun setAdapter() = adapter

    override fun getMode() = SwipeRefreshRecyclerLayout.Mode.Both

    override fun pullDownRefresh() {
        super.pullDownRefresh()
        page = 1
        getData()
    }

    override fun loadMore() {
        super.loadMore()
        getData()
    }

    override fun onFirstVisibleToUser() {
        showEmpty()
        initHeadView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewCreated = true;
    }

    private fun getData() {
        if(!isViewCreated)
            return
        HttpManager.getTicketOrderList(SPUtils.instance().getInt(Const.User.USER_ID), page, 1).requestByF(this, success = { _, data ->
            mSwipeRefreshLayout.isRefreshing = false
            data?.let {
                if (page == 1 && it.isEmpty())
                    showEmpty()
                else
                    adapter.removeFooter()
                if (page == 1)
                    orders.clear()
                if (it.isNotEmpty()) {
                    orders.addAll(it)
                    page++
                }
                adapter.notifyDataSetChanged()
            }
        }, error = { _, _ ->
            mSwipeRefreshLayout.isRefreshing = false
        })
    }

    private fun initHeadView() {
        val mainActivity = activity as MainActivity
        mainActivity.addLocationListener(this)
        headView.tv_time.text = System.currentTimeMillis().toTime("MM月dd日 今天")
        getSearchHistory()
        headView.btn_search.onClick {
            if (endCity == "") {
                toast("请选择到达站点")
                return@onClick
            }
            if (startCity == "") {
                toast("请选择出发站点")
                return@onClick
            }
//            if (startCity == endCity) {   todo test
//                toast("出发城市不能和到达城市相同")
//                return@onClick
//            }
            dbManager.saveHistory(mainActivity, startCity, endCity, startCode, endCode, startId, startStationId, endStationId,lineType, DBHelper.HISTORY_TICKET_TABLE_NAME)
            getSearchHistory()
            startActivity<TicketListActivity>("start" to startCity, "end" to endCity, "date" to date, "startCode" to startCode, "endCode" to endCode, "enableDays" to enableDays
                    , "startStationId" to startStationId,"endStationId" to endStationId,"lineType" to lineType)
        }
        headView.tv_start.onClick {
            activity!!.isBaseActivity {
                it.startActivityForResult<ChooseStationActivity>(11, "cityId" to startId)
            }
        }
        headView.tv_end.onClick {
            if (startStationId == 0) {
                toast("请先选择出发站点")
                return@onClick
            }
            activity!!.isBaseActivity {
                it.startActivityForResult<ChooseStationActivity>(12, "isStart" to false, "stationId" to startStationId, "cityId" to startId, "lineType" to lineType)
            }
        }
        headView.tv_time.onClick {
            activity!!.isBaseActivity {
                it.startActivityForResult<CalendarActivity>(13, "enableDays" to enableDays)
            }
        }
        adapter.setHeaderView(headView)
    }


    /**
     * 更新搜索历史记录
     */
    private fun getSearchHistory() {
        headView.ll_address.removeAllViews()
        val historyList = dbManager.getHistoryList(activity!!, 10, DBHelper.HISTORY_TICKET_TABLE_NAME)
        if (historyList.isNotEmpty()) {
            headView.ll_history.visible()
            historyList.forEach {
                val textView = TextView(activity)
                textView.text = "${it.start}-${it.end}"
                textView.textColorResource = R.color.grey_text
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11F)
                textView.setPadding(dip(4), dip(2), dip(4), dip(2))
                textView.setOnClickListener { _ ->
                    startCity = it.start
                    endCity = it.end
                    headView.tv_start.text = startCity
                    headView.tv_end.text = endCity
                    startCode = it.startCode
                    endCode = it.endCode
                    startId = it.startId
                    startStationId = it.startStationId.toInt()
                    endStationId = it.endStationId.toInt()
                }
                headView.ll_address.addView(textView)
            }
        }
    }

    private fun showEmpty() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_rent_no_data, mSwipeRefreshLayout.mRecyclerView, false) as TextView
        view.text = "您近期暂未预订车票"
        view.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ticket_no_data, 0, 0)
        adapter.setFooterView(view)
    }

    private var lineType = 0
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null)
            when (requestCode) {
                11 -> {
                    startCity = data.getStringExtra("name")
                    startCode = data.getStringExtra("cityCode")
                    startId = data.getIntExtra("cityId", 0).toLong()
                    lineType = data.getIntExtra("lineType", 0)
                    startStationId = data.getIntExtra("stationId", 0)
                    headView.tv_start.text = startCity
                }
                12 -> {
                    endCity = data.getStringExtra("name")
                    endCode = data.getStringExtra("cityCode")
                    endStationId = data.getIntExtra("stationId", 0)
                    headView.tv_end.text = endCity
                }
                13 -> {
                    date = data.getLongExtra("date", 0)
                    headView.tv_time.text = date.toTime("MM-dd ") + date.toWeek()
                }
                14 -> {
                    startActivity<TicketPaySuccessActivity>("id" to data.getIntExtra("id", 0),
                            "orderNum" to data.getStringExtra("orderNum"))
                }
            }
    }

    fun refreshData() {
        page = 1
        getData()
    }
}