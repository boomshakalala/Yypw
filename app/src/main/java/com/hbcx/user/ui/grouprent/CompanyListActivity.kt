package com.hbcx.user.ui.grouprent

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import cn.sinata.xldutils.activity.BaseActivity
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.toTime
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import cn.sinata.xldutils.visible
import com.amap.api.services.help.Tip
import com.hbcx.user.R
import com.hbcx.user.adapter.GroupRentCarListAdapter
import com.hbcx.user.beans.CarBrand
import com.hbcx.user.beans.GroupCarList
import com.hbcx.user.dialogs.LoginDialog
import com.hbcx.user.dialogs.RentFilterDialog
import com.hbcx.user.network.HttpManager
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.StatusBarUtil
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_company_list.*
import kotlinx.android.synthetic.main.layout_search_title.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.startActivity

class CompanyListActivity :BaseActivity(), TextWatcher {
    override fun afterTextChanged(p0: Editable?) {
        val s = p0.toString()
        data.clear()
        if (s.isEmpty()){
            data.addAll(result)
            lv_car.visible()
            tv_empty.gone()
        }else{
            result.map {
                val copy = it.copy()
                copy.carList = arrayListOf()
                it.carList.map {
                   if ((it.brandName+it.modelName).contains(s,true)){
                       copy.carList.add(it)
                   }
                }
                if (copy.carList.isNotEmpty())
                    data.add(it)
            }
            if (data.isEmpty()){
                lv_car.gone()
                tv_empty.visible()
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_list)
        StatusBarUtil.initStatus(window)
        initView()
        initViewClick()
    }

    private val duration by lazy {
        intent.getIntExtra("duration",0)
    }
    //请求参数
    private var page = 1
    private val startTime by lazy {
        intent.getLongExtra("startTime",0)
    }
    private val type by lazy {
        intent.getSerializableExtra("type") as com.hbcx.user.beans.GroupRentType
    }
    private val startTip by lazy {
        intent.getParcelableExtra("startTip") as Tip
    }
    private val endTip by lazy {
        intent.getParcelableExtra("endTip") as Tip
    }
    private val startCity by lazy {
        intent.getStringExtra("startCity")
    }
    private val endCity by lazy {
        intent.getStringExtra("endCity")
    }
    private var brandIndex = -1
    private var startMoney = 0.0
    private var endMoney = 0.0
    private var priceIndex = -1

    //请求结果
    private val data: ArrayList<GroupCarList> = arrayListOf()  //适配器数据
    private val result: ArrayList<GroupCarList> = arrayListOf()  //结果数据集
    private val brands = arrayListOf<CarBrand>()
    private val adapter by lazy {
        GroupRentCarListAdapter(data, type.name)
    }

    private fun initViewClick() {
        adapter.setOnMoreClickListener(object : GroupRentCarListAdapter.OnMoreClickListener{
            override fun onCarClick(id:Int) {
                if (SPUtils.instance().getInt(Const.User.USER_ID) == -1){
                    LoginDialog().show(supportFragmentManager, "login")
                    return
                }
                startActivity<GroupOrderConfirmActivity>("id" to id,"startTip" to startTip,"startTime" to startTime,"endTip" to endTip,"startCity" to startCity,"endCity" to endCity,"duration" to duration,"type" to type.name)
            }

            override fun onClick(data: ArrayList<com.hbcx.user.beans.GroupRentCarData>) {
                startActivity<MoreCarListActivity>("data" to data,"startTip" to startTip,"startTime" to startTime,"endTip" to endTip,"startCity" to startCity,"endCity" to endCity,"duration" to duration,"type" to type.name)
            }
        })
        iv_back.setOnClickListener {
            finish()
        }
        iv_filter.setOnClickListener {
            val rentFilterDialog = RentFilterDialog()
            rentFilterDialog.arguments = bundleOf("price" to priceIndex,"brands" to brands,"brand" to brandIndex,"type" to 1)
            rentFilterDialog.setDialogListener { label, price, distance, brand ->
                priceIndex = price
                startMoney = when(price){
                    0-> 0.0
                    1->500.0
                    2->1000.0
                    else -> 0.0
                }
                endMoney = when(price){
                    0-> 500.0
                    1->1000.0
                    2->0.00
                    else -> 0.0
                }
                brandIndex = brand
                page = 1
                showDialog()
                getData()
            }
            rentFilterDialog.show(supportFragmentManager,"filter")
        }
    }

    private fun initView() {
        getBrands()
        showDialog()
        getData()
        lv_car.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false))
        lv_car.setAdapter(adapter)
        et_search.addTextChangedListener(this)
        lv_car.setOnRefreshListener(object : SwipeRefreshRecyclerLayout.OnRefreshListener {
            override fun onRefresh() {
                page = 1
                getData()
            }

            override fun onLoadMore() {
                page++
                getData()
            }
        })
    }

    private fun getData() {
        HttpManager.getGroupCarList(page, startTime.toTime("HH:mm"), startTip.point.longitude,startTip.point.latitude,
                 type.id, if (brandIndex==-1) 0 else brands[brandIndex].id, startMoney, endMoney)
                .request(this,success = { _, carlist ->
                    lv_car.isRefreshing = false
                    if (page == 1){
                        data.clear()
                        result.clear()
                    }
                    carlist?.let {
                        data.addAll(it)
                        result.addAll(it)
                        if (it.isEmpty()) {
                            if (page == 1) {
                                lv_car.setLoadMoreText("暂无数据")
                            } else {
                                lv_car.setLoadMoreText("没有更多车型了")
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                },error = {_,_->
                    lv_car.isRefreshing = false
                })
    }

    private fun getBrands(){
        HttpManager.getCarBrands().request(this){ _, data->
            data?.let {
                brands.addAll(it)
            }
        }
    }
}