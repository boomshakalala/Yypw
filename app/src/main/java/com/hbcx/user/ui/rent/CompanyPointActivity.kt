package com.hbcx.user.ui.rent

import android.os.Bundle
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.services.help.Tip
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_map.*

class CompanyPointActivity:TranslateStatusBarActivity() {
    private val aMap by lazy {
        mMapView.map
    }
    private val id by lazy {
        intent.getIntExtra("id",0)
    }
    private val tip by lazy {
        intent.getParcelableExtra("tip") as Tip
    }

    override fun setContentView()= R.layout.activity_map
    override fun initClick() {
    }

    override fun initView() {
        title = "取还车点"
        addUserPoint()
//        aMap.moveCamera(CameraUpdateFactory.zoomTo(48f))
        aMap.uiSettings.isZoomControlsEnabled = false
        aMap.uiSettings.isRotateGesturesEnabled = false
        getData()
    }
    private fun getData(){
        HttpManager.getCompanyPoint(id,tip.point.longitude,tip.point.latitude).request(this){_,data->
            data?.let {
                it.map {
                    val markerOptions = MarkerOptions()
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_map_center))
                    markerOptions.position(LatLng(it.lat,it.lon))
                    aMap.addMarker(markerOptions)
                }
            }
        }
    }

    private fun addUserPoint(){
        val markerOptions = MarkerOptions()
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location))
        markerOptions.position(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng))
        aMap.addMarker(markerOptions)
        aMap.animateCamera(CameraUpdateFactory.newLatLng(LatLng(com.hbcx.user.YyApplication.lat, com.hbcx.user.YyApplication.lng)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMapView.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }
}
