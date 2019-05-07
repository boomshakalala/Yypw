package com.hbcx.user.ui.rent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import cn.sinata.xldutils.activity.BaseActivity
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import cn.sinata.xldutils.visible
import com.amap.api.services.help.Tip
import com.hbcx.user.R
import com.hbcx.user.dialogs.LoginDialog
import com.hbcx.user.dialogs.RentFilterDialog
import com.hbcx.user.dialogs.SelectRentTimeDialog
import com.hbcx.user.dialogs.SelectTypeDialog
import com.hbcx.user.network.HttpManager
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.StatusBarUtil
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_car_list.*
import kotlinx.android.synthetic.main.layout_search_title.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

class CarListActivity : BaseActivity(), TextWatcher {
    override fun afterTextChanged(p0: Editable?) {
        val s = p0.toString()
        data.clear()
        if (s.isEmpty()){
            data.addAll(result)
            lv_car.visible()
            tv_empty.gone()
        }else{
            result.map {
                if ((it.brandName+it.modelName).contains(s,true)){
                    data.add(it)
                }
            }
            if (data.isEmpty()){
                lv_car.gone()
                tv_empty.visible()
            }else{
                lv_car.visible()
                tv_empty.gone()
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    private val data: ArrayList<com.hbcx.user.beans.CarList> = arrayListOf()  //适配器数据
    private val result: ArrayList<com.hbcx.user.beans.CarList> = arrayListOf()  //结果数据集
    private val adapter by lazy {
        com.hbcx.user.adapter.CarListAdapter(data)
    }
    private val labels = arrayListOf<com.hbcx.user.beans.CarLabel>()
    private val brands = arrayListOf<com.hbcx.user.beans.CarBrand>()

    private var startTime = 0L
    private var endTime = 0L


    private var type: com.hbcx.user.beans.CarLevel? = null
    private val typeList by lazy {
        intent.getSerializableExtra("typeList") as ArrayList<com.hbcx.user.beans.CarLevel>
    }
    private var rentTip: Tip? = null
    private val maxDay by lazy {
        intent.getIntExtra("maxDay", 0)
    }
    private val rentHour by lazy {
        intent.getIntExtra("rentHour", 0)
    }
    private var duration:String = ""
    private var page = 1
    private var distance = 0
    private var labelIndex = -1
    private var priceIndex = -1
    private var startMoney = 0.0
    private var endMoney = 0.0
    private var brandIndex = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_list)
        StatusBarUtil.initStatus(window)
        initView()
        initViewClick()
    }

    private fun initView() {
        getBrands()
        getLabels()
        tv_rent_time.text = intent.getStringExtra("rentTime")
        tv_return_time.text = intent.getStringExtra("returnTime")
        duration = intent.getStringExtra("duration")
        type = intent.getSerializableExtra("type") as com.hbcx.user.beans.CarLevel
        rentTip = intent.getParcelableExtra("rentPoint") as Tip
        startTime = intent.getLongExtra("startTime",0)
        endTime = intent.getLongExtra("endTime",0)
        tv_type.text = type!!.name
        tv_rent_point.text = rentTip?.name
        showDialog()
        getData()
        lv_car.setLayoutManager(LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false))
        lv_car.setAdapter(adapter)
        adapter.setOnMoreClickListener(object : com.hbcx.user.adapter.CarListAdapter.OnMoreClickListener{
            override fun onCarClick(id:Int) {
                if (SPUtils.instance().getInt(Const.User.USER_ID) == -1){
                    LoginDialog().show(supportFragmentManager, "login")
                    return
                }
                startActivity<OrderConfirmActivity>("id" to id,"tip" to rentTip,"startTime" to startTime,"endTime" to endTime,"duration" to duration)
            }

            override fun onClick(data: ArrayList<com.hbcx.user.beans.CarData>) {
                startActivity<CarCompanyListActivity>("data" to data,"tip" to rentTip,"startTime" to startTime,"endTime" to endTime,"duration" to duration)
            }
        })
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
        et_search.addTextChangedListener(this)
    }

    private fun initViewClick() {
        iv_back.setOnClickListener {
            finish()
        }
        iv_filter.setOnClickListener {
            val rentFilterDialog = RentFilterDialog()
            rentFilterDialog.arguments = bundleOf("labels" to labels,"label" to labelIndex,"price" to priceIndex,"distance" to distance,"brands" to brands,"brand" to brandIndex)
            rentFilterDialog.setDialogListener { label, price, distance, brand ->
                labelIndex = label
                priceIndex = price
                startMoney = when(price){
                    0-> 0.0
                    1->200.0
                    2->500.0
                    else -> 0.0
                }
                endMoney = when(price){
                    0-> 200.0
                    1->500.0
                    2->0.00
                    else -> 0.0
                }
                this.distance = distance
                brandIndex = brand
                page = 1
                showDialog()
                getData()
            }
            rentFilterDialog.show(supportFragmentManager,"filter")
        }
        rl_time.setOnClickListener {
            val selectRentTimeDialog = SelectRentTimeDialog()
            selectRentTimeDialog.arguments = bundleOf("type" to 0, "maxDay" to maxDay, "rentHour" to rentHour)
            selectRentTimeDialog.setDialogListener { p, s, p1, s1, s2 ->
                tv_rent_time.text = s!!.substring(0, 6) + s.substring(9, s.length)
                tv_return_time.text = s1!!.substring(0, 6) + s1.substring(9, s1.length)
                page = 1
                showDialog()
                getData()
                startTime = p
                endTime = p1
                duration = s2
            }
            selectRentTimeDialog.show(supportFragmentManager, "time")
        }
        rl_point.setOnClickListener {
            startActivityForResult<SelectAddressActivity>(20)
        }
        rl_type.setOnClickListener {
            val selectTypeDialog = SelectTypeDialog()
            selectTypeDialog.arguments = bundleOf("typeList" to typeList)
            selectTypeDialog.setDialogListener { p, s ->
                tv_type.text = s
                type = typeList[p]
                page=1
                showDialog()
                getData()
            }
            selectTypeDialog.show(supportFragmentManager, "type")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 20 && resultCode == Activity.RESULT_OK && data != null) {
            rentTip = data.getParcelableExtra("data")
            tv_rent_point.text = rentTip!!.name
            page=1
            showDialog()
            getData()
        }
    }

    private fun getData() {
        HttpManager.getCarList(page, tv_rent_time.text.toString().substring(6, 11), tv_return_time.text.toString().substring(6, 11),
                rentTip!!.point.longitude, rentTip!!.point.latitude, type!!.id, if (brandIndex==-1) 0 else brands[brandIndex].id, distance, if (labelIndex==-1) 0 else labels[labelIndex].id, startMoney, endMoney)
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
    private fun getLabels(){
        HttpManager.getCarLabels().request(this){_,data->
            data?.let {
                labels.addAll(it)
            }
        }
    }
    private fun getBrands(){
        HttpManager.getCarBrands().request(this){_,data->
            data?.let {
                brands.addAll(it)
            }
        }
    }
}
