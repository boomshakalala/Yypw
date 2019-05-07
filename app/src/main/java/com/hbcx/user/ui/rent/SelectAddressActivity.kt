package com.hbcx.user.ui.rent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.support.v7.widget.LinearLayoutManager
import cn.sinata.xldutils.ioScheduler
import cn.sinata.xldutils.rxutils.ResultException
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeQuery
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.help.Tip
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.hbcx.user.R
import com.hbcx.user.db.DBHelper
import com.hbcx.user.db.HistoryDBManager
import com.hbcx.user.ui.SelectCityActivity
import com.hbcx.user.ui.TranslateStatusBarActivity
import io.reactivex.Flowable
import io.reactivex.subscribers.DisposableSubscriber
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeResult
import kotlinx.android.synthetic.main.activity_select_address.*
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast


class SelectAddressActivity : TranslateStatusBarActivity(), GeocodeSearch.OnGeocodeSearchListener {
    override fun onRegeocodeSearched(p0: RegeocodeResult?, p1: Int) {
    }

    override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {
        if (p1 == 1000) {
            p0?.let {
                if (it.geocodeAddressList != null && it.geocodeAddressList.isNotEmpty()) {
                    val geocodeAddress = it.geocodeAddressList[0]
                    aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(geocodeAddress.latLonPoint.latitude, geocodeAddress.latLonPoint.longitude)))
                }
            }
        }
    }

    override fun setContentView() = R.layout.activity_select_address

    private val aMap by lazy {
        mMapView.map
    }
    private var poiSearchDisposable: DisposableSubscriber<PoiResult>? = null
    private val mTips = ArrayList<Tip>()
    private val tipAdapter by lazy {
        com.hbcx.user.adapter.TipAdapter(mTips)
    }
    private val type by lazy { //1:票务 2:租车
        intent.getIntExtra("type", 0)
    }

    private val city by lazy { //票务出发城市
        intent.getStringExtra("city")
    }

    private var onCameraChangeListener: AMap.OnCameraChangeListener = object : AMap.OnCameraChangeListener {

        // 拖动地图
        override fun onCameraChange(cameraPosition: CameraPosition) {

        }

        /**
         * 拖动地图 结束回调
         *
         * @param cameraPosition 当地图位置发生变化，就重新查询数据（手动拖动或者代码改变地图位置都会调用）
         */
        override fun onCameraChangeFinish(cameraPosition: CameraPosition) {
            getPoi(cameraPosition.target.latitude, cameraPosition.target.longitude)
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
                mTips.clear()
                if (t != null) {
                    t.pois.sortBy { it.distance }
                    if (t.pois.isNotEmpty()) {
                        t.pois.map {
                            val tip = Tip()
                            tip.setPostion(it.latLonPoint)
                            tip.name = it.title
                            tip.adcode = it.adCode
                            tip.address = it.snippet
                            mTips.add(tip)
                        }
                    } else {
                        Looper.prepare()
                        toast("附近无可取车地点")
                        Looper.loop()
                    }
                } else {
                    toast("附近无可取车地点")
                }
                runOnUiThread {
                    com.hbcx.user.utils.AnimationUtil.beatingPoint(iv_point)
                    tipAdapter.notifyDataSetChanged()
                }
            }

            override fun onError(t: Throwable?) {
                toast("地址搜索失败")
            }
        }
        val query = PoiSearch.Query("", "190000", "")
        val poiSearch = PoiSearch(this, query)
        poiSearch.bound = PoiSearch.SearchBound(LatLonPoint(lat, lng), 10000)
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

    override fun initClick() {
        tv_search.setOnClickListener {
            startActivityForResult<SearchRentPlaceActivity>(1, "type" to type,"city" to city)
        }
        //归位按钮
        iv_location.setOnClickListener {
            aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)))
        }
        tipAdapter.setOnItemClickListener { view, position ->
            val tip = mTips[position]
            HistoryDBManager().saveHistory(this, tip, DBHelper.HISTORY_RENT_TABLE_NAME)
            val intent = Intent()
            intent.putExtra("data", tip)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        if (type!=1&&type != 2)
            titleBar.titleView.setOnClickListener {
                startActivityForResult<SelectCityActivity>(2)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMapView.onCreate(savedInstanceState)
    }

    override fun initView() {
        if (type!=1&&type!=2){
            titleBar.titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.drop_down, 0)
            titleBar.titleView.textSize = 14f
        }
        else{
            title = if (type == 2) "租车地" else "出发地"
            if (city!= com.hbcx.user.YyApplication.city&&type == 1){ //如果出发城市不等于定位城市
                getCityLatlng(city)
            }
        }
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15f))
        if (com.hbcx.user.YyApplication.city == com.hbcx.user.YyApplication.selectCityName)
            aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)))
        else
            getCityLatlng(com.hbcx.user.YyApplication.selectCityName)
        val markerOptions = MarkerOptions()
        markerOptions.position(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng))
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location))
        aMap.addMarker(markerOptions)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        lv_location.layoutManager = linearLayoutManager
        lv_location.adapter = tipAdapter
        aMap.setOnCameraChangeListener(onCameraChangeListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            getCityLatlng(data!!.getStringExtra("city"))
        }
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
        if (type!=1&&type!=2)
            title = com.hbcx.user.YyApplication.selectCityName
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        try {
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

    private fun getCityLatlng(city: String) {
        val geocodeSearch = GeocodeSearch(this)
        geocodeSearch.setOnGeocodeSearchListener(this)
        val geocodeQuery = GeocodeQuery(city, "")
        geocodeSearch.getFromLocationNameAsyn(geocodeQuery)
    }
}