package com.hbcx.user.ui.grouprent

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.TextView
import cn.sinata.xldutils.callPhone
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.toTime
import cn.sinata.xldutils.utils.toWeek
import cn.sinata.xldutils.visible
import com.hbcx.user.R
import com.hbcx.user.dialogs.TipDialog
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.MainActivity
import com.hbcx.user.ui.PayActivity
import com.hbcx.user.ui.PaySuccessActivity
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.ui.rent.CompanyActivity
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_group_rent_order.*
import org.jetbrains.anko.*

class GroupRentOrderDetailActivity : TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.activity_group_rent_order

    private val id by lazy {
        intent.getIntExtra("id", 0)
    }
    private val isCreate by lazy {
        intent.getBooleanExtra("isCreate", false)
    }

    private var order: com.hbcx.user.beans.GroupRentOrder? = null

    override fun initClick() {
    }

    override fun initView() {
        title = "订单详情"
        showDialog()
        getData()
    }

    private fun getData() {
        HttpManager.getGroupRentOrderDetail(id).request(this) { _, data ->
            data?.let {
                order = it
                if (it.status == 1) {
                    tv_money.visible()
                    tv_money_title.visible()
                    tv_all_money.gone()
                    tv_all_money_title.gone()
                    tv_money_1.gone()
                    tv_money_1_title.gone()
                    tv_money_2.gone()
                    tv_money_2_title.gone()
                } else {
                    tv_money.gone()
                    tv_money_title.gone()
                    tv_all_money.visible()
                    tv_all_money_title.visible()
                    tv_money_1.visible()
                    tv_money_1_title.visible()
                    tv_money_2.visible()
                    tv_money_2_title.visible()
                    if (it.status == 2)
                        tv_money_1.textColorResource = R.color.color_money_text
                    if (it.status in 4..5) {
                        ll_car_info.visible()
                        tv_car_num.text = "包车牌编号：${it.charteredNum}"
                    }

                }
                tv_state.text = it.getStateStr()
                tv_state.textColorResource = it.getStateColor()
                tv_start_city.text = it.startCity
                tv_start_address.text = it.startAddress
                tv_end_city.text = it.endCity
                tv_end_address.text = it.endAddress
                tv_start_time_and_duration.text = "${it.startTime.toTime("MM-dd")} ${it.startTime.toWeek()} ${it.startTime.toTime("HH:mm")}出发 ${it.times}天"
                tv_type.text = it.levelName
                tv_title.text = "${it.brandName}${it.modelName}"
                tv_count.text = "${it.carNum}辆"
                tv_person_count.text = "可乘坐${it.pedestal}人"
                iv_car.setImageURI(it.imgUrl)
                tv_company.text = it.companyName
                tv_rent_point.text = "公司地址：${it.address}"
                rl_company.setOnClickListener {
                    //                    startActivity<CompanyActivity>("id" to data.c,"type" to 1)
                }
                tv_open_time.text = "营业时间：${it.businessStartTime}-${it.businessEndTime}"
                iv_call.setOnClickListener {
                    callPhone(data.contactNumber)
                }

                tv_order_num.text = it.orderNum
                tv_order_time.text = it.createTime.toTime("yyyy-MM-dd HH:mm")
                tv_phone.text = it.phone
                tv_phone.setOnClickListener {
                    callPhone(data.phone)
                }
                tv_money.text = "￥${it.startMoney}～￥${it.endMoney}"
                tv_all_money.text = "￥${it.balance + it.deposit}"
                tv_money_1.text = "￥${it.deposit}"
                tv_money_2.text = "￥${it.balance}"

                tv_negative.visibility = if (data.status == 5) View.GONE else View.VISIBLE
                tv_negative.text = data.getCancelStr()
                tv_positive.visibility = if (data.status in 6..8) View.GONE else View.VISIBLE
                tv_positive.text = data.getActionStr()
                tv_negative.setOnClickListener {
                    it as TextView
                    if (it.text == "删除订单")
                        deleteOrder()
                    else
                        cancelOrder()
                }
                tv_positive.setOnClickListener {
                    it as TextView
                    if (it.text == "联系商家") {
                        callPhone(data.contactNumber)
                    } else if (it.text == "立即签约")
                        startActivityForResult<SignActivity>(2, "id" to data.id)
                    else if (it.text == "支付订金")
                        startActivityForResult<PayActivity>(1, "money" to data.deposit, "id" to data.id, "type" to 1)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 2){
                showDialog()
                getData()
            }
            else
                startActivity<PaySuccessActivity>("type" to 2, "id" to id)
        }
    }

    private fun cancelOrder() {
        val tipDialog = TipDialog()
        tipDialog.arguments = bundleOf("msg" to if (order!!.status in 3..4) "取消订单后订金可退金额由平台跟您与商家共同商议后确定！" else "是否取消该订单", "cancel" to "我再想想", "ok" to "确定取消")
        tipDialog.setDialogListener { p, s ->
            HttpManager.cancelGroupOrder(id).request(this) { _, _ ->
                toast("取消成功")
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        tipDialog.show(supportFragmentManager, "cancel")
    }

    private fun deleteOrder() {
        val tipDialog = TipDialog()
        tipDialog.arguments = bundleOf("msg" to "是否删除该订单", "cancel" to "取消", "ok" to "确定")
        tipDialog.setDialogListener { p, s ->
            HttpManager.deleteGroupOrder(id).request(this) { _, _ ->
                toast("删除成功")
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        tipDialog.show(supportFragmentManager, "cancel")
    }

    override fun onBackPressed() {
        if (isCreate)
            startActivity<MainActivity>()
        else
            super.onBackPressed()
    }
}



