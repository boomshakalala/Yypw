package com.hbcx.user.ui.ticket

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import cn.sinata.amaplib.overlay.DrivingRouteOverlay
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.toTime
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.*
import com.hbcx.user.R
import com.hbcx.user.beans.BusStation
import com.hbcx.user.dialogs.ChangeStationDialog
import com.hbcx.user.dialogs.LoginDialog
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_line_detail.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.dip
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class LineDetailActivity : TranslateStatusBarActivity(), SlidingUpPanelLayout.PanelSlideListener, RouteSearch.OnRouteSearchListener {
    private val aMap by lazy {
        mMapView.map
    }

    private val stations = arrayListOf<BusStation>()

    private val adapter by lazy {
        com.hbcx.user.adapter.BusStationAdapter(stations)
    }

    private var start = ""
    private var end = ""
    private val endCode by lazy {
        intent.getStringExtra("endCode")
    }
    private val startCode by lazy {
        intent.getStringExtra("startCode")
    }
    private val lat by lazy {
        intent.getDoubleExtra("lat", 0.0)
    }
    private val lng by lazy {
        intent.getDoubleExtra("lng", 0.0)
    }
    private val date by lazy {
        intent.getLongExtra("date", 0L)
    }
    private val id by lazy {
        //班线id
        intent.getIntExtra("id", 0)
    }
    //上车和下车站点，默认为始发站和终点站
    private var startPointId = 0
    private var endPointId = 0

    private var startIndex = 0
    private var endIndex = 0

    private var money = 0.0 //单价
    private var ticketNum = 0 //余票数

    private val changeStationDialog by lazy {
        ChangeStationDialog()
    }

    private var startMarker: Marker? = null //起点标记
    private var endMarker: Marker? = null //终点标记
    private var route: DrivingRouteOverlay? = null //路径


    override fun setContentView() = R.layout.activity_line_detail

    override fun initClick() {
        iv_location.onClick {
            aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)))
        }
        tv_action.onClick {
            if (SPUtils.instance().getInt(Const.User.USER_ID) == -1) {
                toast("请先登录")
                LoginDialog().show(supportFragmentManager, "login")
                return@onClick
            }
            if (ticketNum == 0) {
                toast("该班次已售完")
                return@onClick
            }
            startActivity<TicketOrderConfirmActivity>("start" to start, "end" to end, "startPoint" to stations[startIndex].name
                    , "endPoint" to stations[endIndex].name, "date" to date, "startTime" to stations[startIndex].times, "id" to id,
                    "endTime" to stations[endIndex].times, "money" to money, "ticketNum" to if (ticketNum > 3) 3 else ticketNum,
                    "startPointId" to stations[startIndex].id, "endPointId" to stations[endIndex].id)
        }
        tv_change_start.onClick {
            val list = stations.subList(0, endIndex).filter {
                it.type in arrayOf(1, 3, 4)
            }
            changeStationDialog.arguments = bundleOf("isStart" to true, "list" to list)
            changeStationDialog.setCallback { position ->
                val busStation = list[position]
                startPointId = busStation.id
                startIndex = stations.indexOf(busStation)
                getData()
            }
            changeStationDialog.show(supportFragmentManager, "start")
        }
        tv_change_end.onClick {
            val list = stations.subList(startIndex + 1, stations.size).filter {
                it.type in arrayOf(2, 3, 5)
            }
            changeStationDialog.arguments = bundleOf("isStart" to false, "list" to list)
            changeStationDialog.setCallback { position ->
                val busStation = list[position]
                endPointId = busStation.id
                endIndex = stations.indexOf(busStation)
                getData()
            }
            changeStationDialog.show(supportFragmentManager, "end")
        }
    }

    override fun initView() {
        title = "班线详情"
        mSlidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        mSlidingUpPanelLayout.addPanelSlideListener(this)
        mSlidingUpPanelLayout.isOverlayed = true
        aMap.uiSettings.isRotateGesturesEnabled = false
        aMap.uiSettings.isZoomControlsEnabled = false
        rv_bus_station.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_bus_station.adapter = adapter
        aMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location)).position(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)))
        startPointId = intent.getIntExtra("startPointId", 0)
        endPointId = intent.getIntExtra("endPointId", 0)
        getData()
    }

    private fun getData() {
        HttpManager.getTicketLineDeatail(startCode, endCode, lng, lat, date.toTime("yyyy-MM-dd"), id, startPointId, endPointId).request(this) { _, data ->
            data?.let {
                money = it.money
                ticketNum = it.pedestal.toInt()
                tv_start_time.text = String.format("%s上车", it.start_time)
                tv_count.text = String.format("余票%s张", it.pedestal)
                tv_money.text = String.format("￥%.2f", it.money)
                tv_start.text = it.startName
                tv_end.text = it.endName
                start = it.startCityName
                end = it.endCityName
                tv_distance.text = String.format("距你%.2fkm", it.km1 / 1000)
                tv_length.text = String.format("约%.2fkm", it.km2 / 1000)
                if (stations.isEmpty() && it.stationList.size > 5) { //第一次才设置高度
                    rv_bus_station.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(250))
                }
                if (it.stationList.isNotEmpty()) {
                    stations.clear()
                    if (endIndex == 0) //当第一次请求进行初始化
                        endIndex = it.stationList.size - 1
                    stations.addAll(it.stationList)
                    adapter.notifyDataSetChanged()
                }
                throughList.clear()
                throughName.clear()
                throughList.addAll(stations.subList(startIndex+1,endIndex).map {
                    LatLonPoint(it.lat,it.lon)
                })
                throughName.addAll(stations.subList(startIndex+1,endIndex).map {
                    it.name
                })
                setStartMarker(it.startLat, it.startLon)
                if (it.endLat == 0.0 && it.endLon == 0.0)
                    aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.startLat, it.startLon)))
                else {
                    setEndMarker(it.endLat, it.endLon)
                    calculateRoute(LatLonPoint(it.startLat, it.startLon), LatLonPoint(it.endLat, it.endLon))
                }
            }
        }
    }

    private fun calculateRoute(startPoint: LatLonPoint, endPoint: LatLonPoint) {
        val fromAndTo = RouteSearch.FromAndTo(startPoint, endPoint)
        val driveRouteQuery = RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, throughList, null, "")
        val routeSearch = RouteSearch(this)
        routeSearch.calculateDriveRouteAsyn(driveRouteQuery)
        routeSearch.setRouteSearchListener(this)
    }

    //沿途点坐标
    private val throughList = mutableListOf<LatLonPoint>()
    private val throughName = arrayListOf<String>()
    override fun onDriveRouteSearched(result: DriveRouteResult?, p1: Int) {
        if (p1 == 1000) {
            route?.removeFromMap()
            if (result?.paths != null) {
                if (result.paths.size > 0) {
                    val drivePath = result.paths[0]
                    route = DrivingRouteOverlay(
                            this, aMap, drivePath,
                            result.startPos,
                            result.targetPos,throughList,throughName)
                    route!!.setNodeIconVisibility(false)
                    route!!.addToMap()
                    route!!.zoomToSpan(50, 50, 50, 50)
                }
            }
        }
    }

    override fun onBusRouteSearched(p0: BusRouteResult?, p1: Int) {
    }

    override fun onRideRouteSearched(p0: RideRouteResult?, p1: Int) {
    }

    override fun onWalkRouteSearched(p0: WalkRouteResult?, p1: Int) {
    }

    private fun setStartMarker(lat: Double, lng: Double) {
        if (startMarker == null) {
            startMarker = aMap.addMarker(MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_start))
                    .position(LatLng(lat, lng)))
        } else {
            startMarker?.position = LatLng(lat, lng)
        }
    }

    private fun setEndMarker(lat: Double, lng: Double) {
        if (endMarker == null) {
            endMarker = aMap.addMarker(MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_end))
                    .position(LatLng(lat, lng)))
        } else {
            endMarker?.position = LatLng(lat, lng)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMapView.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onPanelSlide(panel: View?, slideOffset: Float) {
    }

    override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?) {
        if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            tv_panel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_panel_down, 0)
        } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            tv_panel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_panel, 0)
        }
    }
}