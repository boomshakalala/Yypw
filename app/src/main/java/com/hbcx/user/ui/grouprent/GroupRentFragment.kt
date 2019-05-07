package com.hbcx.user.ui.grouprent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import cn.sinata.xldutils.callPhone
import cn.sinata.xldutils.fragment.RecyclerFragment
import cn.sinata.xldutils.ioScheduler
import cn.sinata.xldutils.rxutils.ResultException
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.toTime
import cn.sinata.xldutils.utils.toWeek
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationListener
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.amap.api.services.help.Tip
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.hbcx.user.R
import com.hbcx.user.dialogs.SelectDayCountDialog
import com.hbcx.user.dialogs.SelectRentTimeDialog
import com.hbcx.user.dialogs.TipDialog
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.MainActivity
import com.hbcx.user.ui.PayActivity
import com.hbcx.user.ui.PaySuccessActivity
import com.hbcx.user.ui.rent.SelectAddressActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.isBaseActivity
import com.hbcx.user.utils.requestByF
import io.reactivex.Flowable
import io.reactivex.subscribers.DisposableSubscriber
import kotlinx.android.synthetic.main.layout_group_rent_head.*
import kotlinx.android.synthetic.main.layout_group_rent_head.view.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * 包车
 */
class GroupRentFragment : RecyclerFragment(), AMapLocationListener {
    private var startTip: Tip? = null
    private var endTip: Tip? = null
    private var rentTime = 2 //包车时间
    private var startTime = System.currentTimeMillis() //出发时间
    private var startCity = ""
    private var endCity = ""

    override fun onLocationChanged(p0: AMapLocation?) {
        p0?.let {
            getPoi(p0.latitude, p0.longitude)
        }
    }

    private var groupRentMain: com.hbcx.user.beans.GroupRentMain? = null
    private val data = arrayListOf<com.hbcx.user.beans.GroupRentOrder>()
    private val adapter = com.hbcx.user.adapter.GroupRentOrderAdapter(data, object : com.hbcx.user.adapter.GroupRentOrderAdapter.OnClickCallBack {
        override fun onSign(id: Int) {
            startActivity<SignActivity>("id" to id)
        }

        override fun onDelete(id: Int) {
            val tipDialog = TipDialog()
            tipDialog.arguments = bundleOf("msg" to "是否删除该订单", "cancel" to "取消", "ok" to "确定")
            tipDialog.setDialogListener { p, s ->
                HttpManager.deleteGroupOrder(id).requestByF(this@GroupRentFragment) { _, _ ->
                    toast("删除成功")
                    getData()
                }
            }
            tipDialog.show(fragmentManager, "cancel")
        }

        override fun onPay(order: com.hbcx.user.beans.GroupRentOrder) {
            activity!!.isBaseActivity {
                it.startActivityForResult<PayActivity>(101, "money" to order.deposit, "id" to order.id, "type" to 1)
            }
        }

        override fun onCall(phone: String) {
            callPhone(phone)
        }

        override fun onCancel(id: Int, status: Int) {
            val tipDialog = TipDialog()
            tipDialog.arguments = bundleOf("msg" to if (status in 3..4) "取消订单后订金可退金额由平台跟您与商家共同商议后确定！" else "是否取消该订单", "cancel" to "我再想想", "ok" to "确定取消")
            tipDialog.setDialogListener { p, s ->
                HttpManager.cancelGroupOrder(id).requestByF(this@GroupRentFragment) { _, _ ->
                    toast("取消成功")
                    getData()
                }
            }
            tipDialog.show(fragmentManager, "cancel")
        }

        override fun onItemClick(id: Int) {
            startActivity<GroupRentOrderDetailActivity>("id" to id)
        }

    })

    override fun setAdapter() = adapter
    private val headView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.layout_group_rent_head, mSwipeRefreshLayout.mRecyclerView, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userVisibleHint = true
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    override fun onFirstVisibleToUser() {
        mSwipeRefreshLayout.setLayoutManager(LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false))
        setEmptyFooter()
        initHeadView()
//        getData()
    }

    override fun getMode() = SwipeRefreshRecyclerLayout.Mode.Top

    private fun setEmptyFooter() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_rent_no_data, mSwipeRefreshLayout.mRecyclerView, false) as TextView
        view.text = "您近期暂未包车"
        view.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.group_no_data, 0, 0)
        adapter.setFooterView(view)
    }

    override fun pullDownRefresh() {
        getData()
    }

    private fun initHeadView() {
        val mainActivity = activity as MainActivity
        mainActivity.addLocationListener(this)
        adapter.setHeaderView(headView)
        startTime = startTime - startTime % (24 * 60 * 60 * 1000) + 25 * 60 * 60 * 1000
        headView.tv_time.text = "${startTime.toTime("MM-dd ")}${startTime.toWeek()}${startTime.toTime(" HH:mm")}"
        headView.tv_start_address.setOnClickListener {
            activity!!.isBaseActivity {
                it.startActivityForResult<SelectAddressActivity>(30)
            }
        }
        headView.tv_end_address.setOnClickListener {
            activity!!.isBaseActivity {
                it.startActivityForResult<SelectAddressActivity>(40)
            }
        }
        headView.ll_start_time.setOnClickListener {
            val dialog = SelectRentTimeDialog()
            dialog.arguments = bundleOf("type" to 2)
            dialog.setDialogListener { p0, s0, p1, s1, s2 ->
                startTime = p0
                headView.tv_time.text = s0
            }
            dialog.show(fragmentManager, "time")
        }
        headView.ll_duration.setOnClickListener {
            if (groupRentMain == null)
                return@setOnClickListener
            val selectDayCountDialog = SelectDayCountDialog()
            selectDayCountDialog.arguments = bundleOf("day" to groupRentMain?.charteredDay)
            selectDayCountDialog.setDialogListener { p, s ->
                rentTime = p + 1
                tv_duration.text = s
            }
            selectDayCountDialog.show(fragmentManager, "duration")
        }
        headView.iv_left.setOnClickListener {
            headView.view_pager.currentItem = if (headView.view_pager.currentItem == 0) 0 else headView.view_pager.currentItem - 1
        }
        headView.iv_right.setOnClickListener {
            headView.view_pager.currentItem = if (headView.view_pager.currentItem == fragments.size - 1) fragments.size - 1 else headView.view_pager.currentItem + 1
        }
        headView.btn_search.setOnClickListener {
            if (groupRentMain == null) return@setOnClickListener
            if (startTip == null) {
                toast("请选择出发地")
                return@setOnClickListener
            }
            if (endTip == null) {
                toast("请选择到达地")
                return@setOnClickListener
            }
            startActivity<CompanyListActivity>("startTime" to startTime, "startTip" to startTip!!
                    , "endTip" to endTip!!, "type" to groupRentMain!!.carLevelList[headView.view_pager.currentItem]
                    , "duration" to rentTime, "startCity" to startCity, "endCity" to endCity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                30 -> {
                    startTip = data.getParcelableExtra("data")
                    headView.tv_start_address.text = startTip!!.name
                    if (endTip != null)
                        calculate()
                    getCityName(LatLonPoint(startTip!!.point.latitude, startTip!!.point.longitude), true)
                }
                40 -> {
                    endTip = data.getParcelableExtra("data")
                    headView.tv_end_address.text = endTip!!.name
                    getCityName(LatLonPoint(endTip!!.point.latitude, endTip!!.point.longitude), false)
                    calculate()
                }
                101 -> startActivity<PaySuccessActivity>("type" to 2, "id" to data.getIntExtra("id", 0))

            }
        }
    }

    private fun calculate() {
        HttpManager.getDistance(startTip!!.point.longitude, startTip!!.point.latitude, endTip!!.point.longitude, endTip!!.point.latitude)
                .requestByF(this) { _, data ->
                    data?.let {
                        headView.tv_distance.text = "${it.get("distance").asDouble}公里"
                    }
                }
    }

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
                        startCity = t.pois[0].cityName
                        t.pois[0].title
                    } else {
                        startTip = null
                        null
                    }
                } else {
                    startTip = null
                    null
                }
                activity!!.runOnUiThread {
                    headView.tv_start_address.text = address
                }
            }
        }
        val query = PoiSearch.Query("", "190000", "")
        val poiSearch = PoiSearch(context, query)
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

    private val fragments = arrayListOf<Fragment>()

    inner class TypeFragmentAdapter(manager: FragmentManager?) : FragmentPagerAdapter(manager) {
        override fun getItem(position: Int) = fragments[position]
        override fun getCount() = fragments.size
    }

    private fun getCityName(latLonPoint: LatLonPoint, start: Boolean) {
        val geocodeSearch = GeocodeSearch(context)
        geocodeSearch.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {
            }

            override fun onRegeocodeSearched(p0: RegeocodeResult?, p1: Int) {
                if (p0 != null) {
                    if (start) {
                        startCity = p0.regeocodeAddress.city
                    } else
                        endCity = p0.regeocodeAddress.city
                }
            }
        })
        val query = RegeocodeQuery(latLonPoint, 10f, GeocodeSearch.AMAP)
        geocodeSearch.getFromLocationAsyn(query)
    }

    fun getData() {
        HttpManager.getGroupRentMain(SPUtils.instance().getInt(Const.User.USER_ID)).requestByF(this, success = { _, data ->
            mSwipeRefreshLayout.isRefreshing = false
            data?.let {
                groupRentMain = it
                if (fragments.isEmpty()){
                    it.carLevelList.forEach {
                        fragments.add(TypeViewPagerFragment.getInstance(it))
                    }
                    headView.view_pager.adapter = TypeFragmentAdapter(fragmentManager)
                }
                this.data.clear()
                if (it.orderList != null && it.orderList!!.isNotEmpty()) {
                    this.data.addAll(it.orderList!!)
                    adapter.removeFooter()
                } else {
                    setEmptyFooter()
                }
            }
        }, error = { _, _ ->
            mSwipeRefreshLayout.isRefreshing = false
        })
    }
}