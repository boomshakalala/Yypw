package com.hbcx.user.ui.rent

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import com.github.promeg.pinyinhelper.Pinyin.toPinyin
import com.hbcx.user.R
import com.hbcx.user.adapter.PersonAdapter
import com.hbcx.user.beans.Driver
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.ui.ticket.AddPassengerActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_select_driver.*
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast

class SelectDriverActivity : TranslateStatusBarActivity(), TextWatcher {
    private val data = arrayListOf<Driver>()
    private var result: ArrayList<Driver>? = null
    private val driver = arrayListOf<Driver>() //选择的

    private val isPassenger by lazy {
        //true 仅为驾驶员
        intent.getBooleanExtra("isPassenger", false)
    }

    private val canAdd = false //是否可以继续添加
    private var isFirstIn = true //是否第一次进入页面 true需要加载选中人

    private val checkable by lazy {
        intent.getBooleanExtra("checkable", true)
    }

    private val ticketNum by lazy {
        //最大购票数
        intent.getIntExtra("ticketNum", 3)
    }
    private val checkedData by lazy {
        intent.getIntegerArrayListExtra("datas")
    }

    override fun afterTextChanged(p0: Editable?) {
        data.clear()
        val s = p0.toString()
        if (s.isEmpty()) {
            data.addAll(result!!)
        } else {
            result!!.map {
                if (it.name.contains(s, true) || toPinyin(it.name, "").contains(s, true)) {
                    data.add(it)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    private val id by lazy {
        SPUtils.instance().getInt(Const.User.USER_ID)
    }

    override fun setContentView() = R.layout.activity_select_driver

    override fun initClick() {
        tv_add.setOnClickListener {
            if (isPassenger)
                startActivityForResult<AddPassengerActivity>(1)
            else
                startActivityForResult<AddDriverActivity>(1)
        }
        adapter.setOnClickCallback(object : com.hbcx.user.adapter.PersonAdapter.OnClickCallback {
            override fun onItemClick(data: com.hbcx.user.beans.Driver) {
                if (driver.contains(data)) {
                    driver.remove(data)
                    data.isChecked = false
                } else {
                    if (!isPassenger && driver.isNotEmpty()) { //单选
                        driver[0].isChecked = false
                        driver.clear()
                    }
                    if (driver.size == ticketNum) { //限购3张
                        toast("最多可购买${ticketNum}张")
                    } else {
                        data.isChecked = true
                        driver.add(data)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onEditClick(data: com.hbcx.user.beans.Driver) {
                if (isPassenger)
                    startActivityForResult<AddPassengerActivity>(1, "data" to data)
                else
                    startActivityForResult<AddDriverActivity>(1, "data" to data)
            }
        })
    }

    private val adapter by lazy {
        PersonAdapter(data, isPassenger, checkable)
    }

    override fun initView() {
        title = if (isPassenger)
            "常用乘车人"
        else
            "选择驾驶员"
        if (!isPassenger)
            tv_add.text = "新增驾驶员"
        if (checkable) {
            titleBar.addRightButton("确定", onClickListener = View.OnClickListener {
                setResult(Activity.RESULT_OK, intent.putExtra("driver", driver))
                finish()
            })
            (titleBar.getRightButton(0) as TextView).setTextColor(resources.getColor(R.color.black_text))
        }
        lv_driver.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))
        lv_driver.setMode(SwipeRefreshRecyclerLayout.Mode.None)
        lv_driver.setAdapter(adapter)
        showDialog()
        getData()
        et_search.addTextChangedListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            et_search.setText("")
            getData()
        }
    }

    private fun getData() {
        HttpManager.getPerson(id, if (isPassenger) 0 else 1).request(this) { _, data ->
            data?.let {
                if (isPassenger && checkable) {
                    it.forEach { data ->
                        if (isFirstIn)
                            checkedData.forEach {
                                if (data.id == it) {
                                    data.isChecked = true
                                    driver.add(data)
                                }
                            }
                        else
                            driver.forEach {
                                if (data.id == it.id) {
                                    data.isChecked = true
                                }
                            }
                    }
                }
                result = it
                this.data.clear()
                this.data.addAll(it)
                adapter.notifyDataSetChanged()
                isFirstIn = false
            }
        }
    }
}
