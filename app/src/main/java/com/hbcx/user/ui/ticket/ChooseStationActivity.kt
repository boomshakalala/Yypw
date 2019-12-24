package com.hbcx.user.ui.ticket

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import com.amap.api.col.sln3.it
import com.hbcx.user.R
import com.hbcx.user.adapter.OpenCityAdapter
import com.hbcx.user.adapter.OpenProvinceAdapter
import com.hbcx.user.adapter.RegionAdapter
import com.hbcx.user.adapter.StationAdapter
import com.hbcx.user.beans.OpenCity
import com.hbcx.user.beans.OpenProvince
import com.hbcx.user.beans.Region
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.layout_station_head.view.*
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

class ChooseStationActivity : TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.base_recyclerview_layout

    override fun initClick() {
        cityAdapter.setOnItemClickListener { _, position ->
            if (position != cityIndex) {
                citys[position].isChecked = true
                citys[cityIndex].isChecked = false
                cityIndex = position
                cityAdapter.notifyDataSetChanged()
                getRegion()
            }
        }

        proviceAdapter.setOnItemClickListener { _, position ->
            var index = 0;
            for (province in provinces) {
                province.isChecked = index == position
                index++
                proviceAdapter.notifyDataSetChanged()
            }
            citys.clear()
            regions.clear()
            adapter.notifyDataSetChanged()
            for (openCity in provinces[position].city) {
                openCity.isChecked = false
            }
            citys.addAll(provinces[position].city)
            if (citys.isNotEmpty()) {
                citys[0].isChecked = true
                cityIndex = 0
                getRegion()
            }
            cityAdapter.notifyDataSetChanged()
        }
    }

    private val isStart by lazy {
        intent.getBooleanExtra("isStart", true)
    }

    private val recyclerLayout by lazy {
        find<SwipeRefreshRecyclerLayout>(R.id.swipeRefreshLayout)
    }
    private val citys = arrayListOf<OpenCity>()
    private val provinces = arrayListOf<OpenProvince>()
    private val regions = arrayListOf<Region>()
    private val adapters = arrayListOf<StationAdapter>()
    private val adapter by lazy {
        RegionAdapter(regions, adapters)
    }
    private val cityAdapter by lazy {
        OpenCityAdapter(citys)
    }

    private val proviceAdapter by lazy {
        OpenProvinceAdapter(provinces)
    }
    private val headerView by lazy {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_station_head, recyclerLayout, false)
        view.rv_city.layoutManager = GridLayoutManager(this, 3)
        view.rv_city_1.layoutManager = GridLayoutManager(this, 3)
        view.rv_city_1.visibility = if(!isStart) View.VISIBLE else View.GONE
        view.tv_title_1.visibility = if(!isStart) View.VISIBLE else View.GONE
        view.rv_city.adapter = cityAdapter
        view.rv_city_1.adapter = proviceAdapter
        view.tv_title.text = if (isStart) "出发城市" else "到达城市"
        view.tv_title_1.text = if (isStart) "出发省份" else "到达省份"
        view.tv_title_2.text = if (isStart) "选择上车点" else "选择下车点"
        view.tv_search.setOnClickListener {
            if (citys.isNotEmpty())
                startActivityForResult<SearchStationActivity>(1, "isStart" to isStart, "lineType" to lineType, "stationId" to stationId, "cityCode" to citys[cityIndex].cityCode)
        }
        view
    }

    override fun initView() {
        title = if (isStart) "选择上车站" else "选择下车站"
        recyclerLayout.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))
        recyclerLayout.setMode(SwipeRefreshRecyclerLayout.Mode.None)
        recyclerLayout.backgroundColorResource = R.color.page_bg_deep
        recyclerLayout.setAdapter(adapter)
        adapter.setHeaderView(headerView)
        getCity()
    }

    private var cityIndex = 0
    private val lineType by lazy {
        //结束站点使用的线路类型
        intent.getIntExtra("lineType", 0)
    }
    private val stationId by lazy {
        //结束站点使用的开始站点id
        intent.getIntExtra("stationId", 0)
    }
    private val cityId by lazy {
        //结束站点使用的开始城市id
        intent.getLongExtra("cityId", 0)
    }

    private fun getCity() {
        if (isStart)
            HttpManager.getOpenCity().request(this) { _, data ->
                data?.let {
                    citys.addAll(it)
                    it.forEachWithIndex { i, openCity ->
                        if (openCity.id.toLong() == cityId) {
                            openCity.isChecked = true
                            cityIndex = i
                            getRegion()
                            return@forEachWithIndex
                        }
                    }
                    if (citys.isNotEmpty()) {
                        citys[0].isChecked = true
                        getRegion()
                    }
                    cityAdapter.notifyDataSetChanged()
                }
            }
        else
            HttpManager.getEndCity(cityId).request(this) { _, data ->
                data?.let {
//                    citys.addAll(it)
//                    if (citys.isNotEmpty()) {
//                        citys[0].isChecked = true
//                        getRegion()
//                    }
//                    cityAdapter.notifyDataSetChanged()
                    provinces.addAll(it)
                    if (provinces.isNotEmpty()){
                        provinces[0].isChecked = true
//                        proviceAdapter.notifyDataSetChanged()
                        citys.clear()
                        citys.addAll(provinces[0].city)
                        if (citys.isNotEmpty()) {
                            citys[0].isChecked = true
                            getRegion()
                    }
                        proviceAdapter.notifyDataSetChanged()
                        cityAdapter.notifyDataSetChanged()

                    }
                }
            }
    }

    private fun getRegion() {
        regions.clear()
        adapters.clear()
        showDialog(canCancel = false)
        if (isStart)
            HttpManager.getStartStation(citys[cityIndex].cityCode).request(this) { _, data ->
                dismissDialog()
                if (!data.isNullOrEmpty()) {
                    regions.addAll(data)
                    data[0].isOpen = true
                    data.forEach {
                        val stationAdapter = StationAdapter(it.stationList)
                        stationAdapter.setOnItemClickListener { _, position ->
                            setResult(Activity.RESULT_OK, intent.putExtra("cityId", citys[cityIndex].id)
                                    .putExtra("stationId", it.stationList[position].stationId)
                                    .putExtra("lineType", it.stationList[position].lineType)
                                    .putExtra("cityCode", citys[cityIndex].cityCode)
                                    .putExtra("city", citys[position].cityName)
                                    .putExtra("name", it.stationList[position].stationName))
                            finish()
                        }
                        adapters.add(stationAdapter)
                    }
                }
                adapter.notifyDataSetChanged()
            }
        else{
            regions.clear()
            HttpManager.getEndStation(citys[cityIndex].cityCode, lineType, stationId).request(this) { _, data ->
                dismissDialog()
                if (!data.isNullOrEmpty()) {
                    data[0].isOpen = true
                    regions.addAll(data)
                    data.forEach {
                        val stationAdapter = StationAdapter(it.stationList)
                        stationAdapter.setOnItemClickListener { _, position ->
                            setResult(Activity.RESULT_OK, intent.putExtra("cityId", citys[cityIndex].id)
                                    .putExtra("stationId", it.stationList[position].stationId)
                                    .putExtra("lineType", it.stationList[position].lineType)
                                    .putExtra("cityCode", citys[cityIndex].cityCode)
                                    .putExtra("city", citys[cityIndex].cityName)
                                    .putExtra("name", it.stationList[position].stationName))
                            finish()
                        }
                        adapters.add(stationAdapter)
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK&&data!=null){
            setResult(Activity.RESULT_OK,data.putExtra("cityId", citys[cityIndex].id)
                    .putExtra("cityCode", citys[cityIndex].cityCode))
            finish()
        }
    }
}