package com.hbcx.user.ui

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.widget.TextView
import cn.sinata.xldutils.activity.BaseActivity
import cn.sinata.xldutils.utils.SpanBuilder
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.utils.StatusBarUtil
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_select_city.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.textColorResource

class SelectCityActivity : BaseActivity(), TextWatcher {
    override fun afterTextChanged(p0: Editable?) {
        data.clear()
        if (p0!!.isEmpty()) {
            data.addAll(sortData)
        } else {
            sortData.forEach {
                if (it.cityName.contains(p0.toString())) {
                    data.add(it)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private val startId by lazy {
        intent.getLongExtra("startId", 0L)
    }

    private val changeRootCity by lazy {
        intent.getBooleanExtra("changeRootCity", true)
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    private val index = arrayListOf("A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_city)
        StatusBarUtil.initStatus(window)
        showDialog()
        getData()
        rv_city.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_city.adapter = adapter
        adapter.setOnCityClick(object : com.hbcx.user.adapter.SelectCityAdapter.OnCityClick {
            override fun onCityClick(city: com.hbcx.user.beans.OpenCity) {
                if (changeRootCity){
                    com.hbcx.user.YyApplication.selectCityName = city.cityName
                    com.hbcx.user.YyApplication.selectCityId = city.id
                }
                setResult(Activity.RESULT_OK, intent.putExtra("city", city.cityName).putExtra("cityId", city.id).putExtra("cityCode",city.cityCode))
                finish()
            }
        })
        tv_cancel.setOnClickListener {
            finish()
        }
        addHeaderView()
        side_bar.setOnSelectIndexItemListener { index ->
            (0 until data.size).forEach {
                if (index == data[it].getInitial()) {
                    val manager = rv_city.layoutManager as LinearLayoutManager
                    manager.scrollToPositionWithOffset(it, 0)
                    return@setOnSelectIndexItemListener
                }
            }
        }
        et_search_content.addTextChangedListener(this)
    }

    /**
     * 添加当前选择城市
     */
    private fun addHeaderView() {
        val textView = TextView(this)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.textColorResource = R.color.colorPrimary
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.text = SpanBuilder("当前选择：${if (com.hbcx.user.YyApplication.selectCityName.isEmpty()) "未选择" else com.hbcx.user.YyApplication.selectCityName} ")
                .color(this, 0, 5, R.color.black_text).build()
        textView.setPadding(dip(10), dip(10), 0, dip(10))
        adapter.setHeaderView(textView)
    }

    private val sortData = arrayListOf<com.hbcx.user.beans.OpenCity>()
    private val data = arrayListOf<com.hbcx.user.beans.OpenCity>()

    private val adapter by lazy {
        com.hbcx.user.adapter.SelectCityAdapter(data)
    }

    private fun getData() {
        if (startId != 0L) //获取到达城市
            HttpManager.getEndCity(startId).request(this) { _, data ->
                data?.let {
                    if (it.size > 0) {
//                        index.forEach { index ->
//                            data.forEach {
//                                if (it.getInitial() == index)
//                                    sortData.add(it)
//                            }
//                        }
//                        this.data.addAll(sortData)
//                        adapter.notifyDataSetChanged()
                    }
                }
            }
        else
            HttpManager.getOpenCity().request(this) { _, data ->
                data?.let {
                    if (it.size > 0) {
                        index.forEach { index ->
                            data.forEach {
                                if (it.getInitial() == index)
                                    sortData.add(it)
                            }
                        }
                        this.data.addAll(sortData)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
    }
}