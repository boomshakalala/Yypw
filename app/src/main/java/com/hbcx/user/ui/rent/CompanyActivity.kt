package com.hbcx.user.ui.rent

import android.os.Bundle
import android.view.LayoutInflater
import cn.sinata.xldutils.adapter.ImagePagerAdapter
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.services.help.Tip
import com.hbcx.user.R
import com.hbcx.user.network.Api
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.request
import android.graphics.Bitmap
import android.os.Handler
import android.view.View
import cn.sinata.xldutils.callPhone
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.visible
import kotlinx.android.synthetic.main.activity_company.*
import kotlinx.android.synthetic.main.layout_marker_company.view.*
import org.jetbrains.anko.startActivity


class CompanyActivity : TranslateStatusBarActivity() {
    private val HANDLER_TIME = 2000L //自动轮播时间
    private val mHandler = Handler()
    private val id by lazy {
        intent.getIntExtra("id", 0)
    }
    private val tip by lazy {
        intent.getParcelableExtra("tip") as Tip
    }
    private val type by lazy {
        intent.getIntExtra("type", 0)
    }

    private val aMap by lazy {
        mMapView.map
    }

    override fun setContentView() = R.layout.activity_company

    override fun initClick() {
        tv_point_nearby.setOnClickListener {
            startActivity<CompanyPointActivity>("tip" to tip, "id" to id)
        }
    }

    override fun initView() {
        title = "公司信息"
        webView.loadUrl(Api.COMPANY_DESCRIBE + id)
        if (type == 1) {
            tv_point_nearby.gone()
            divider.gone()
            mMapView.visible()
            aMap.uiSettings.isZoomControlsEnabled = false
            aMap.uiSettings.isZoomGesturesEnabled = false
            aMap.uiSettings.isRotateGesturesEnabled = false
            aMap.uiSettings.isScrollGesturesEnabled = false
            aMap.moveCamera(CameraUpdateFactory.zoomTo(14f))
        }
        getData()
    }

    private fun getData() {
        HttpManager.getCompanyInfo(id, tip!!.point.longitude, tip!!.point.latitude).request(this) { _, data ->
            data?.let {
                view_pager.adapter = ImagePagerAdapter(supportFragmentManager, it.companyPictures.map {
                    it.imgUrl
                } as ArrayList<String>)
                com.hbcx.user.views.ViewPagerIndicator(this, ll_indicator, R.mipmap.indicator_gray, R.mipmap.indicator_white, it.companyPictures.size).setupWithBanner(view_pager)
                tv_name.text = it.name
                tv_car_num.text = "共${it.rentingNum}款车"
                tv_rent_num.text = "预订${it.reserveNum}次"
                tv_phone.text = it.contactNumber
                tv_open_time.text = "${it.startTime}-${it.endTime}"
                if (type == 0)
                    tv_point_nearby.text = "最近取还车点距${tip!!.name}${String.format("%.2fkm", it.distance / 1000)}"
                else{
                    val latLng = LatLng(it.lat, it.lon)
                    val markerOptions = MarkerOptions()
                    val view = LayoutInflater.from(this).inflate(R.layout.layout_marker_company,null)
                    view.tv.text = it.address
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(convertViewToBitmap(view)))
                    markerOptions.position(latLng)
                    markerOptions.anchor(0f,0.5f)
                    aMap.addMarker(markerOptions)
                    aMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                }
                tv_phone.setOnClickListener {
                    callPhone(data.contactNumber)
                }
                mCount = it.companyPictures.size
                mHandler.postDelayed(runnableForViewPager, HANDLER_TIME)
            }
        }
    }
    var itemPosition = 0
    var mCount = 0
    /**
     * ViewPager的定时器
     */
    private var runnableForViewPager: Runnable = object : Runnable {
        override fun run() {
            try {
                itemPosition++
                mHandler.postDelayed(this, HANDLER_TIME)
                view_pager.currentItem = itemPosition % mCount
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMapView.onCreate(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

    //view 转bitmap
    private fun convertViewToBitmap(view: View): Bitmap {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.buildDrawingCache()
        return view.drawingCache
    }
}