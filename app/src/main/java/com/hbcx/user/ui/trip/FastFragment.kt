package com.hbcx.user.ui.trip

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import cn.sinata.xldutils.activity.BaseActivity
import cn.sinata.xldutils.fragment.BaseFragment
import cn.sinata.xldutils.ioScheduler
import cn.sinata.xldutils.rxutils.ResultException
import cn.sinata.xldutils.utils.optInt
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.help.Tip
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.hbcx.user.R
import com.hbcx.user.dialogs.SelectTimeDialog
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.MainActivity
import com.hbcx.user.utils.isBaseActivity
import com.hbcx.user.utils.request
import com.hbcx.user.utils.requestByF
import io.reactivex.Flowable
import io.reactivex.subscribers.DisposableSubscriber
import kotlinx.android.synthetic.main.fragment_fast.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.support.v4.startActivity


class FastFragment : BaseFragment(), AMapLocationListener {
    private val type by lazy {
        arguments!!.getInt("type")
    }
    private var bundle: Bundle? = null
    private var myLocation: Marker? = null
    private var time = "现在"
    private var reserveTime = 0
    private var driverList: ArrayList<Marker> = arrayListOf()
    private var startTip: Tip? = null
    private var endTip: Tip? = null
    private val aMap by lazy {
        mMapView.map
    }
    private val app by lazy {
        activity!!.application as com.hbcx.user.YyApplication
    }
    private var poiSearchDisposable: DisposableSubscriber<PoiResult>? = null
    private var selectCityName = ""
    //我的定位城市
    private var mCity = ""
    private var mCityCode = ""
    private var isFirst = true
    private var isMoveBySelectAddress = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.bundle = savedInstanceState
        userVisibleHint = true
        val mainActivity = activity as MainActivity
        mainActivity.addLocationListener(this)
    }

    companion object {
        fun instance(type: Int): FastFragment {
            val fragment = FastFragment()
            fragment.arguments = bundleOf("type" to type)
            return fragment
        }
    }

    override fun contentViewId() = R.layout.fragment_fast

    override fun onFirstVisibleToUser() {
        mMapView.onCreate(bundle)

        aMap.uiSettings.isZoomControlsEnabled = false
        aMap.uiSettings.isRotateGesturesEnabled = false
        aMap.uiSettings.setZoomInByScreenCenter(true)
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15f))
        /**
         * 地图移动事件
         */
        aMap.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChangeFinish(p0: CameraPosition?) {
                if (isMoveBySelectAddress) {
                    isMoveBySelectAddress = false
                    return
                }
                if (p0 != null) {
                    getPoi(p0.target.latitude, p0.target.longitude)
                }
            }

            override fun onCameraChange(p0: CameraPosition?) {
            }
        })
//        requestLocation() 定位放到Activity中了

        //归位按钮
        tv_my_location.setOnClickListener {
            //如果定位的城市和选择的城市是同一个
            if (mCity.isNotEmpty() && selectCityName.isNotEmpty() && (selectCityName.contains(mCity) || mCity.contains(selectCityName))) {
                aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)))
            } else {
                aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)))
//                tv_title.text = mCity 设置顶部城市
            }
            getPoi(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)
        }

        tv_time.setOnClickListener {
            val selectTime = SelectTimeDialog()
            selectTime.arguments = bundleOf("reserveTime" to reserveTime)
            selectTime.setDialogListener { p, s ->
                time = s ?: ""
                tv_time.text = s
            }
            childFragmentManager.beginTransaction()
                    .add(selectTime, "time")
                    .commitNowAllowingStateLoss()
        }
        //开始地点
        tv_start_address.setOnClickListener {
            activity!!.isBaseActivity {
                it.startActivityForResult<SearchAddressActivity>(if (type == 1) 1 else 3, "type" to 0, "city" to mCity)
            }
        }
        //目的地
        tv_end_address.setOnClickListener {
            activity!!.isBaseActivity {
                it.startActivityForResult<SearchAddressActivity>(if (type == 1) 2 else 4, "type" to 1, "city" to mCity)
            }
        }
        getReserveTime()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1 && data != null) {//起点
                startTip = data.getParcelableExtra("data")
                tv_start_address.text = startTip?.name
                isMoveBySelectAddress = true

                startTip?.let {
                    val latLng = LatLng(it.point.latitude, it.point.longitude)
                    aMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    if (endTip != null) {
                        startActivity<TripActivity>("type" to type, "start" to it, "end" to endTip!!, "time" to time, "reserveTime" to reserveTime, "city" to mCity)
                        tv_time.text = ""
                        tv_end_address.text = ""
                        time = ""
                        endTip = null
                    }
                }

            } else if (requestCode == 2 && data != null) {
                endTip = data.getParcelableExtra("data")
                tv_end_address.text = endTip?.name
                if (startTip != null) {
                    startActivity<TripActivity>("type" to type, "start" to startTip!!, "end" to endTip!!, "time" to time, "reserveTime" to reserveTime, "city" to mCity)
                    tv_time.text = ""
                    tv_end_address.text = ""
                    time = ""
                    endTip = null
                }
            }
        }
    }

    private fun getReserveTime() {
        HttpManager.getReserveTime(type).requestByF(this) { _, data ->
            reserveTime = data?.optInt("times") ?: 0
        }
    }

    fun requestLocation() {
        app.setLocationListener(this)
        app.startLocation()
    }

    override fun onLocationChanged(location: AMapLocation?) {
        location?.let {
            val b = BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location)
            if (myLocation == null) {
                val options = MarkerOptions()
                options.position(LatLng(it.latitude, it.longitude))
                options.icon(b)
                myLocation = aMap.addMarker(options)
            } else {
                myLocation?.position = LatLng(it.latitude, it.longitude)
            }
            mCity = it.city
            app.stopLocation()
            aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
            if (isFirst) {
                getPoi(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)
                isFirst = false
            }
        }
    }

    /**
     *   获取poi信息
     */
    fun getPoi(lat: Double?, lng: Double?) {
        if (lat == null || lng == null) {
            return
        }
        //如果上一次还没处理完，取消订阅
        if (poiSearchDisposable != null && !poiSearchDisposable!!.isDisposed) {
            poiSearchDisposable?.dispose()
        }
        poiSearchDisposable = object : DisposableSubscriber<PoiResult>() {
            override fun onComplete() {

            }

            override fun onNext(t: PoiResult?) {
                val address = if (t != null) {
                    t.pois.sortBy { it.distance }
                    if (t.pois.isNotEmpty()) {
                        startTip = Tip()
                        startTip!!.setPostion(t.pois[0].latLonPoint)
                        startTip!!.name = t.pois[0].title
                        getNearbyDriver()
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
                    tv_start_address.text = address
                    com.hbcx.user.utils.AnimationUtil.beatingPoint(iv_location)
                }
            }

            override fun onError(t: Throwable?) {
                activity!!.runOnUiThread {
                    tv_start_address.text = "附近无可上车地点"
                }
                startTip = null
            }
        }
        val query = PoiSearch.Query("", "190000","")
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

    private fun getNearbyDriver() {
        HttpManager.getNearbyDriver(type, startTip!!.point.longitude, startTip!!.point!!.latitude)
                .requestByF(this) { _, data ->
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

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        if (mMapView != null)
            mMapView.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }
}