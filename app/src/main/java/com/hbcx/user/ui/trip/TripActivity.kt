package com.hbcx.user.ui.trip

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import cn.sinata.amaplib.overlay.DrivingRouteOverlay
import cn.sinata.xldutils.callPhone
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.rxutils.SystemUtil
import cn.sinata.xldutils.utils.*
import cn.sinata.xldutils.visible
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.help.Tip
import com.amap.api.services.route.*
import com.hbcx.user.R
import com.hbcx.user.beans.Order
import com.hbcx.user.dialogs.LoginDialog
import com.hbcx.user.dialogs.SelectTimeDialog
import com.hbcx.user.dialogs.TipDialog
import com.hbcx.user.interfaces.TripMessageListener
import com.hbcx.user.interfaces.TripStateChangedListener
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_trip.*
import org.jetbrains.anko.*
import org.json.JSONObject
import java.util.*

class TripActivity : TranslateStatusBarActivity(), TripStateChangedListener, RouteSearch.OnRouteSearchListener, Handler.Callback, SlidingUpPanelLayout.PanelSlideListener, TripMessageListener {
    override fun onCancel(obj: JSONObject) {
        val id = obj.optInt("id")
        if (id == order!!.id) {
            val tipDialog = TipDialog()
            tipDialog.arguments = bundleOf("msg" to "平台已取消订单", "notice" to true)
            tipDialog.setDialogListener { p, s ->
                finish()
            }
            tipDialog.show(supportFragmentManager, "cancel")
        }
    }

    //1=待接单 2=已接单 3=出发前往预约地点 4=到达预约地点 5=开始服务 6=待支付 7=完成服务(待评价) 8=完成服务(已评价) 9=无偿取消 10=有偿取消 11=已改派 12=异常结束
    private var state = 0
    private val reserveTime by lazy {
        intent.getIntExtra("reserveTime", 0)
    }
    private var orderId: Int = 0
    private var myLocation: Marker? = null
    private var driverMarker: Marker? = null //司机位置
    private var startMarker: Marker? = null //起始点marker
    private var endMarker: Marker? = null //终点marker
    private var drivingRouteOverlay: DrivingRouteOverlay? = null
    private var oldDrivingRouteOverlay: DrivingRouteOverlay? = null
    private var time = ""
    private var driverList: ArrayList<Marker> = arrayListOf()
    private var startTip: Tip? = null
    private var endTip: Tip? = null
    private var type: Int = 1  //1=快车，2=经济型，3=舒适型，4=商务型
    private var orderCreateTime = 0L
    private var driverArriveTime = 0L
    private val aMap by lazy {
        mMapView.map
    }
    private val app by lazy {
        application as com.hbcx.user.YyApplication
    }
    private val mCity by lazy {
        intent.getStringExtra("city")
    }
    private val handler by lazy {
        Handler(this)
    }
    private var order: Order? = null
    private var bundle: Bundle? = null

    override fun setContentView(): Int = R.layout.activity_trip
    override fun onPanelSlide(panel: View?, slideOffset: Float) {

    }

    override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?) {
        if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            tv_panel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_panel_down, 0)
        } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            tv_panel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_panel, 0)
        }
    }

    override fun initClick() {
        tv_time.setOnClickListener {
            val selectTime = SelectTimeDialog()
            selectTime.arguments = bundleOf("reserveTime" to reserveTime)
            selectTime.setDialogListener { p, s ->
                time = s ?: ""
                tv_time.text = s
            }
            selectTime.show(supportFragmentManager, "time")
        }
        //开始地点
        tv_start_address.setOnClickListener {
            startActivityForResult<SearchAddressActivity>(1, "type" to 0, "city" to mCity)
        }
        //目的地
        tv_end_address.setOnClickListener {
            startActivityForResult<SearchAddressActivity>(2, "type" to 1, "city" to mCity)
        }
        //归位按钮
        tv_my_location.setOnClickListener {
            aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)))
        }

        tv_call.setOnClickListener {
            order()
        }

        tv_call_phone.setOnClickListener {
            order?.let {
                callPhone(it.phone)
            }
        }
        mSlidingUpPanelLayout.addPanelSlideListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = savedInstanceState
    }

    override fun initView() {
        setResult(Activity.RESULT_OK)
        mMapView.onCreate(bundle)
        app.addTripStateListener(this)
        app.addTripMessageListener(this)
        aMap.uiSettings.isZoomControlsEnabled = false
        aMap.uiSettings.isRotateGesturesEnabled = false
        aMap.uiSettings.setZoomInByScreenCenter(true)
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15f))
        mSlidingUpPanelLayout.isClipPanel = false
        mSlidingUpPanelLayout.isOverlayed = true
        mSlidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        tv_panel.gone()
        titleBar.showLeft(false)
        titleBar.addRightButton("取消订单", onClickListener = View.OnClickListener {
            cancelOrder()
        })
        titleBar.hideAllRightButton()

        setMyMarker()

        type = intent.getIntExtra("type", 1)
        if (type != 1) {
            ll_type.visible()
            tv_call.text = "呼叫专车"
            rg_type.setOnCheckedChangeListener { _, checkedId ->
                type = when (checkedId) {
                    R.id.rb_cheap -> 2
                    R.id.rb_comfortable -> 3
                    R.id.rb_business -> 4
                    else -> 1
                }
                calculate()
            }
        }
        if (intent.getSerializableExtra("order") != null)
            order = intent.getSerializableExtra("order") as Order
        if (order == null) { //首页过来下单
            card_options.visibility = View.VISIBLE
            tv_call.visibility = View.VISIBLE
            titleBar.showLeft(true)
            startTip = intent.getParcelableExtra("start") as Tip
            endTip = intent.getParcelableExtra("end") as Tip
            time = intent.getStringExtra("time")
            tv_start_address.text = startTip?.name
            tv_end_address.text = endTip?.name
            tv_time.text = time
            tv_start.text = startTip?.name
            tv_end.text = endTip?.name
            tv_time_top.text = if (time.isNotEmpty()) time else "现在"
            calculate()
//            getNearbyDriver() 下单界面不获取附近司机了
            titleBar.setTitle("确认呼叫")
            setStartMarker()
            setEndMarker()
            setRoute()
        } else { //订单列表进来
            orderId = order!!.id!!
            startTip = Tip()
            endTip = Tip()
            getOrderDetail()
        }
    }

    private fun getOrderDetail() {
        showDialog("加载中...", false)
        HttpManager.getOrderDetail(orderId).request(this) { _, data ->
            data?.let {
                this.order = it
                tv_start.text = it.startAddress
                tv_end.text = it.endAddress
//                startTip!!.setPostion(LatLonPoint(it.startLat!!.toDouble(), it.startLon!!.toDouble()))
//                endTip!!.setPostion(LatLonPoint(it.endLat!!.toDouble(), it.endLon!!.toDouble()))
                tv_time_top.text = it.departureTime!!.timeDay() + when (it.type) {
                    2 -> "  经济型"
                    3 -> "  舒适型"
                    4 -> "  商务型"
                    else -> ""
                }
                when (it.status) {
                    1 -> {
                        onWaitingResponse(LatLng(com.hbcx.user.YyApplication.lng, com.hbcx.user.YyApplication.lng))
                    }
                    2 -> {
                        if (order!!.setOutIsNot!!)
                            onWaitingDriver(order!!)
                        else
                            onResponse(order!!)
                    }
                    3 -> {
                        onDriverArrived(order!!)
                    }
                    4 -> {
                        onTripping(order!!)
                    }
                    else->{
                        startActivity<OrderDetailActivity>("order" to order,"type" to 1)
                        finish()
                    }
                }
            }
        }
    }

    private fun setMyMarker() {
        if (myLocation != null)
            myLocation!!.remove()
        val b = BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location)
        val options = MarkerOptions()
        options.position(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng))
        options.icon(b)
        myLocation = aMap.addMarker(options)
    }

    private fun setDriverMarker() {
        val latLng = LatLng(order!!.lat!!, order!!.lon!!)
        if (driverMarker == null) {
            val b = BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)
            val options = MarkerOptions()
            options.position(latLng)
            options.icon(b)
            driverMarker = aMap.addMarker(options)
        } else {
            driverMarker?.position = latLng
        }
    }


    private fun setStartMarker() {
        //文本改变，或marker未初始化时初始化图标信息。
        if (startTip == null)
            return
        val latLng = LatLng(startTip!!.point.latitude, startTip!!.point.longitude)
        if (startMarker == null) {
            val b = BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_start)
            val options = MarkerOptions()
            options.position(latLng)
            options.icon(b)
            startMarker = aMap.addMarker(options)
        } else {
            startMarker?.position = latLng
        }
    }

    private fun setEndMarker() {
        if (endTip == null)
            return
        //文本改变，或marker未初始化时初始化图标信息。
        val latLng = LatLng(endTip!!.point.latitude, endTip!!.point.longitude)
        if (endMarker == null) {
            val b = BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_end)
            val options = MarkerOptions()
            options.position(latLng)
            options.icon(b)
            endMarker = aMap.addMarker(options)
        } else {
            endMarker?.position = latLng
        }
    }

    private fun setRoute() {
        val fromAndTo = RouteSearch.FromAndTo(if (order == null || order!!.lat!! == 0.0) {
            startTip!!.point
        } else LatLonPoint(order!!.lat!!, order!!.lon!!), endTip!!.point)
        val driveRouteQuery = RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null, null, "")
        val routeSearch = RouteSearch(this)
        routeSearch.calculateDriveRouteAsyn(driveRouteQuery)
        routeSearch.setRouteSearchListener(this)
    }

    private fun clearMarkers() {
        aMap.clear()
        startMarker = null
        endMarker = null
        driverMarker = null
        myLocation = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1 && data != null) {//起点
                startTip = data.getParcelableExtra("data")
                tv_start_address.text = startTip?.name

                startTip?.let {
                    val latLng = LatLng(it.point.latitude, it.point.longitude)
                    aMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    if (endTip != null) {
                        calculate()
//                        getNearbyDriver()  //不获取附近司机
                        setStartMarker()
                        setRoute()
                    }
                }

            } else if (requestCode == 2 && data != null) {
                endTip = data.getParcelableExtra("data")
                tv_end_address.text = endTip?.name
                if (startTip != null) {
                    calculate()
                    setEndMarker()
                    setRoute()
                }
            } else if (requestCode == 8&&data!=null) {
                order = data.getSerializableExtra("order") as Order
                Log.e("mmp",order.toString())
                if (order != null && order!!.payMoney?:0.0 > 0) {
                    order!!.status = 6
                    startActivity<OrderDetailActivity>("order" to order)
                }
                finish()
            }
        }
    }

    private fun getNearbyDriver() {
        HttpManager.getNearbyDriver(type, startTip!!.point.longitude, startTip!!.point!!.latitude)
                .request(this) { _, data ->
                    driverList.map {
                        it.remove()
                    }
                    driverList.clear()
                    data?.map {
                        val markerOptions = MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car))
                                .position(LatLng(it.lat, it.lon))
                        driverList.add(aMap.addMarker(markerOptions))
                    }
                }
    }

    /**
     * 计算价格
     */
    private fun calculate() {
        val userId = SPUtils.instance().getInt(Const.User.USER_ID)
        HttpManager.calculatePrice(type, startTip!!.point.longitude, startTip!!.point.latitude, endTip!!.point.longitude, endTip!!.point.latitude, userId).request(this) { _, data ->
            if (data == null) {
                return@request
            }
            val orderMoney = data.optDouble("orderMoney")
            val couponMoney = data.optDouble("couponMoney")
            val t = String.format("打车费约%.2f元", orderMoney)
            tv_money.text = SpanBuilder(t)
                    .color(this, 4, t.length - 1, R.color.color_text_price)
                    .size(4, t.length - 1, 20)
                    .build()
            if (userId > 0) {
                //优惠券已抵扣0元
                val c = String.format("优惠券已抵扣%.2f元", couponMoney)
                tv_coupon.text = c
            } else {
                tv_coupon.text = "登录后可查看优惠券抵扣情况"
            }
        }
    }

    /**
     * 下单
     */
    private fun order() {
        val userId = SPUtils.instance().getInt(Const.User.USER_ID)
        if (userId < 1) {
            val loginDialog = LoginDialog()
            loginDialog.setDialogListener { p, s ->
                calculate()
            }
            loginDialog.show(supportFragmentManager, "login")
            return
        }
        //如果上次请求接口返回的服务区时间比当前手机时间小，使用当前手机时间
        val t = System.currentTimeMillis()
        val now = if (SystemUtil.sysTime < t) {
            t
        } else {
            SystemUtil.sysTime
        }

        val timeStr = if (time.isEmpty() || time == "现在") {//现在
            null
        } else {
            parserTime(now)
        }
        if (startTip == null) {
            toast("请选择出发地")
            return
        }
        if (endTip == null) {
            toast("请选择目的地")
            return
        }
        showDialog(canCancel = false)
        HttpManager.callOrder(type, startTip!!.point.longitude, startTip!!.point.latitude, endTip!!.point.longitude, endTip!!.point.latitude
                , userId, startTip!!.name, endTip!!.name, timeStr).request(this) { _, data ->
            toast("呼叫成功")
            data?.let {
                orderId = it.optInt("id")
                onWaitingResponse(LatLng(startTip!!.point.latitude, startTip!!.point.longitude))
            }
        }

    }

    //取消订单
    private fun cancelOrder() {
        val tipDialog = TipDialog()
        if (state == 1) //待接单可直接取消
            tipDialog.arguments = bundleOf("msg" to "订单正在加紧派给司机，你确定要取消该订单吗？", "cancel" to "再等等")
        else if (order!!.cancleMoney == 0.0 || order?.cancleTime!! > System.currentTimeMillis()) //已接单可免责取消
            tipDialog.arguments = bundleOf("msg" to "司机正在加紧赶来，你确定要取消该订单吗？", "cancel" to "不取消了")
        else //需要支付取消金额
            tipDialog.arguments = bundleOf("msg" to "当前取消订单需要收取￥${order!!.cancleMoney}作为服务费，您确定要取消该订单吗？", "cancel" to "不取消了")

        tipDialog.setDialogListener { p, s ->
            if (state == 1) {
                HttpManager.cancelOrder(orderId,"","").request(this){ _, data->
                    toast("订单已取消成功！")
                    finish()
                }
            } else
                startActivityForResult<CancelOrderActivity>(8, "orderId" to if (order == null) orderId else order?.id)
        }
        tipDialog.show(supportFragmentManager, "cancel")
    }

    // 转换时间格式
    private fun parserTime(now: Long): String {
        val c = Calendar.getInstance()
        c.timeInMillis = now
        val d = c.get(Calendar.DAY_OF_MONTH)
        var timeStr = ""
        when {
            time.startsWith("今天") -> {//今天
                timeStr = time.replace("今天", now.toTime("yyyy-MM-dd "))
            }
            time.startsWith("明天") -> {
                c.set(Calendar.DAY_OF_MONTH, d + 1)
                val t = c.timeInMillis
                timeStr = time.replace("明天", t.toTime("yyyy-MM-dd "))
            }
            time.startsWith("后天") -> {
                c.set(Calendar.DAY_OF_MONTH, d + 2)
                val t = c.timeInMillis
                timeStr = time.replace("后天", t.toTime("yyyy-MM-dd "))
            }
        }
        return "$timeStr:00"
    }

    override fun onWaitingResponse(latLng: LatLng) {
//        if (order?.id != orderId)
//            return
//        isInTrip = true
        titleBar.setTitle("等待应答")
        orderCreateTime = if (order != null) {
            order!!.createTime!!
        } else System.currentTimeMillis()
        state = 1
//        startLatLng = latLng
        changeTripStateUI()
        handler.sendEmptyMessage(0)
        card_options.gone()
        tv_call.gone()
        rl_status.visible()
        tv_status.visible()
        clearMarkers()
        setMyMarker()
        aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)))
        tv_panel.visible()
        mSlidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        tv_waite.visible()
        rl_driver.gone()
    }

    override fun onResponse(order: Order) {
        if (order.id != orderId)
            return
        this.order = order
        orderId = order.id!!
        titleBar.setTitle("等待接驾")
        tv_panel.visible()
        state = 2
        mSlidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        setDriverInfo(order)
        rl_driver.visible()
        tv_waite.gone()
        tv_status.visible()
        rl_status.visible()
        tv_status.text = "等待司机出发前往预约地点\n${order.cancleTime?.toTime("MM月dd日 HH:mm")}前可免责取消订单"
        changeTripStateUI()
        setMyMarker()
        aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)))
    }

    override fun onWaitingDriver(order: Order) {
        if (order.id != orderId)
            return
        this.order = order
        orderId = order.id!!
        titleBar.setTitle("等待接驾")
        tv_panel.visible()
        state = 3
        rl_status.visible()
        mSlidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        setDriverInfo(order)
        rl_driver.visible()
        tv_waite.gone()
        tv_status.visible()
        tv_status.text = "司机已出发，距预约地点${order.estimateDistance}公里，预计${order.estimateTime}分钟到达\n" +
                "${order.cancleTime?.toTime("MM月dd日 HH:mm")}前可免责取消订单"
        changeTripStateUI()
        setMyMarker()
        aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)))
    }

    override fun onDriverArrived(order: Order) {
        if (order.id != orderId)
            return
        this.order = order
        orderId = order.id
        driverArriveTime = order.arrivalTime!!
        titleBar.setTitle("等待上车")
        tv_panel.visible()
        state = 4
        mSlidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        setDriverInfo(order)
        rl_driver.visible()
        rl_status.visible()
        tv_waite.gone()
        tv_status.visible()
        handler.sendEmptyMessage(1)
        changeTripStateUI()
        setMyMarker()
        setDriverMarker()
        aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(order.lat!!, order.lon!!)))
    }

    override fun onTripping(order: Order) {
        if (order.id != orderId)
            return
        this.order = order
        startTip!!.setPostion(LatLonPoint(order.startLat!!.toDouble(), order.startLon!!.toDouble()))
        endTip!!.setPostion(LatLonPoint(order.endLat!!.toDouble(), order.endLon!!.toDouble()))
        orderId = order.id!!
        titleBar.setTitle("行程中")
        tv_panel.visible()
        rl_status.visible()
        state = 5
        mSlidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        setDriverInfo(order)
        rl_driver.visible()
        tv_waite.gone()
        tv_status.visible()
        tv_status.text = "已为您服务${order.serviceTime}分钟，累计行驶${order.serviceDistance}公里\n距离目的地还有${order.estimateDistance}公里，预计行驶${order.estimateTime}分钟"
        changeTripStateUI()
        myLocation?.remove()
        setStartMarker()
        setEndMarker()
        setRoute()
        setDriverMarker()
    }

    override fun onTripFinished(order: Order) {
        if (order.id != orderId) {
            return
        }
        this.order = order
        orderId = order.id
        state = 6
        startActivity<OrderDetailActivity>("order" to order)
        finish()
    }

    override fun onNoResponse() {
        //todo 没有司机接单
    }

    private fun changeTripStateUI() {
        if (state == 1)
            titleBar.showLeft(false)
        else
            titleBar.showLeft(true)

        if (state < 5) {
            titleBar.showAllRightButton()
        } else
            titleBar.hideAllRightButton()
    }

    private fun setDriverInfo(order: Order) {
        headView.setImageURI(order.imgUrl)
        tv_name.text = order.nickName
        tv_license.text = order.licensePlate
        tv_car_info.text = "${order.brandName}${order.modelName}"
        tv_score.text = String.format("%.1f", (order.score ?: 0.0).toFloat())
        tv_count.text = String.format("%s单", order.driverOrderNums)
        tv_time_top.text = order.departureTime!!.timeDay()
        tv_start.text = order.startAddress
        tv_end.text = order.endAddress
    }

    override fun onDriveRouteSearched(result: DriveRouteResult?, errorCode: Int) {
        if (errorCode == 1000) {
            oldDrivingRouteOverlay = drivingRouteOverlay
            if (result?.paths != null) {
                if (result.paths.size > 0) {
                    val drivePath = result.paths[0]
                    drivingRouteOverlay = DrivingRouteOverlay(
                            this, aMap, drivePath,
                            result.startPos,
                            result.targetPos)
                    drivingRouteOverlay!!.setNodeIconVisibility(false)
                    drivingRouteOverlay!!.addToMap()
                    drivingRouteOverlay!!.zoomToSpan(50, if (order == null) 200 else 500, 50, if (order == null) 1200 else 250)
                    oldDrivingRouteOverlay?.removeFromMap()
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

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        try {
            handler.removeMessages(0)
            handler.removeMessages(1)
            app.removeTripStateListener(this)
            app.removeTripMessageListener(this)
            mMapView.onDestroy()
        } catch (e: Exception) {
        }
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    override fun handleMessage(msg: Message?): Boolean {
        when (msg?.what) {
            0 -> {//更新等待时间
                if (state < 2) {
                    val interval = System.currentTimeMillis() - orderCreateTime
                    val s = String.format("正为您寻找车辆, 已等待%02d:%02d", interval / 1000 / 60, interval / 1000 % 60)
                    tv_status.text = s
                }
                handler.sendEmptyMessageDelayed(0, 1000)
            }
            1 -> {//更新等待时间
                if (state == 4) {
                    val interval = System.currentTimeMillis() - driverArriveTime
                    val s = String.format("司机到达预约地点，已等您 %02d:%02d\n当前取消订单需支付一定的违约金", interval / 1000 / 60, interval / 1000 % 60)
                    tv_status.text = s
                }
                handler.sendEmptyMessageDelayed(1, 1000)
            }
        }
        return true
    }
}