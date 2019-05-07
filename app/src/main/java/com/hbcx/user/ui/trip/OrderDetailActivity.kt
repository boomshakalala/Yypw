package com.hbcx.user.ui.trip

import android.app.Activity
import android.content.Intent
import android.view.View
import cn.sinata.xldutils.callPhone
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.SpanBuilder
import cn.sinata.xldutils.utils.timeDay
import cn.sinata.xldutils.visible
import com.hbcx.user.R
import com.hbcx.user.interfaces.PayListener
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.PayUtil
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.layout_pay_check.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

/**
 * 订单详情
 */
class OrderDetailActivity : TranslateStatusBarActivity(), PayListener {

    private var isUpdateStatus = false
    private var payWay:Int = 0 //0农行 1:支付宝 2:wx
    private val orderId by lazy {
        order!!.id
    }
    override fun setContentView(): Int = R.layout.activity_order_detail

    override fun initClick() {
        headView.setOnClickListener {
            val id = order?.id ?: 0
            startActivity<DriverInfoActivity>("id" to id)
        }

        tv_panel.setOnClickListener {
            ll_trip.visibility = if (ll_trip.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        tv_call_phone.setOnClickListener {
            callPhone(order?.phone)
        }

        rl_bank.setOnClickListener{
            payWay = 0
            iv_check.isSelected = true
            iv_check_wx.isSelected = false
            iv_check_ali.isSelected = false
        }
        rl_wx.setOnClickListener{
            payWay = 2
            iv_check.isSelected = false
            iv_check_wx.isSelected = true
            iv_check_ali.isSelected = false
        }

        rl_ali.setOnClickListener{
            payWay = 1
            iv_check.isSelected = false
            iv_check_wx.isSelected = false
            iv_check_ali.isSelected = true
        }
        tv_see_detail.setOnClickListener {
            startActivity<PriceDetailActivity>("order" to order)
        }
        tv_pay.setOnClickListener {
            pay()
        }

        tv_evaluate.setOnClickListener {
            val score = rb_score.rating.toInt()
            val content = et_content.text.toString().trim()
            showDialog()
            HttpManager.doEvaluate(order!!.id!!, score, content).request(this) { _, _ ->
                toast("评价成功！")
                order!!.status = 8
                order!!.evaluateContent = content
                order!!.evaluateScore = score
                setUI()
                isUpdateStatus = true

            }
        }
    }

    override fun initView() {

        title = "订单详情"

        order = intent.getSerializableExtra("order") as com.hbcx.user.beans.Order

        PayUtil.addPayListener(this)
        iv_check.isSelected = true

        titleBar.addRightButton("投诉", onClickListener = View.OnClickListener {
            if (order?.nickName.isNullOrEmpty()) {//没有司机信息
                return@OnClickListener
            }
            val id = order?.id ?: 0
            startActivity<ComplainActivity>("id" to id)

        })

        if (type == 1) {//需要请求详情
            getOrderDetail()
        } else {
            setUI()
        }
    }


    private var order: com.hbcx.user.beans.Order? = null


    private val type by lazy {
        intent.getIntExtra("type", 0)
    }


    private fun setUI() {
        tv_see_detail.gone()
        tv_tip.gone()
        ll_pay.gone()
        rl_evaluate.gone()
        rb_score.gone()
        et_content.gone()
        tv_evaluate.gone()
        tv_pay.gone()
        headView.setImageURI(order?.imgUrl)
        tv_name.text = order?.nickName
        val car = order?.brandName + order?.modelName
        tv_car_info.text = car
        tv_license.text = order?.licensePlate
        tv_score.text = String.format("%.1f", (order?.score ?: 0.0).toFloat())
        tv_count.text = String.format("%s单", order?.driverOrderNums)
        tv_start.text = order!!.startAddress
        tv_end.text = order!!.endAddress
        tv_time_top.text = order!!.departureTime!!.timeDay()+when(order!!.type){
            2-> "  经济型"
            3->"  舒适型"
            4->"  商务型"
            else->""
        }

        when (order?.status) { //5=待支付，6=取消待支付，7=待评价，8=已完成，9=已取消
            5 -> {
                val s = "请支付：${order?.payMoney}元"
                tv_money.text = SpanBuilder(s).color(this, 4, s.length - 1, R.color.color_money_text)
                        .size(4,s.length-1,24).build()
                tv_see_detail.visible()
                ll_pay.visible()
                tv_pay.visible()
            }
            6 -> {
                val s = "请支付：${order?.payMoney}元"
                tv_money.text = SpanBuilder(s).color(this, 4, s.length - 1, R.color.color_money_text)
                        .size(4,s.length-1,24).build()
                tv_tip.visible()
                ll_pay.visible()
                tv_pay.visible()
            }
            7 -> {
                val s = "${order?.payMoney}元"
                tv_money.text = SpanBuilder(s).color(this, 0, s.length - 1, R.color.color_money_text)
                        .size(0,s.length-1,24).build()
                tv_see_detail.visible()
                rl_evaluate.visible()
                tv_evaluate.visible()
                rb_score.visible()
                et_content.visible()
            }
            8 -> {
                val s = "${order?.payMoney}元"
                tv_money.text = SpanBuilder(s).color(this, 0, s.length - 1, R.color.color_money_text)
                        .size(0,s.length-1,24).build()
                tv_see_detail.visible()
                rl_evaluate.visible()
                rb_score.visible()
                et_content.visibility = if (order?.evaluateContent.isNullOrEmpty()) {
                    View.GONE
                } else {
                    et_content.setText(order?.evaluateContent)
                    et_content.isEnabled = false
                    View.VISIBLE
                }
                rb_score.rating = order?.evaluateScore!!.toFloat()
                rb_score.setIsIndicator(true)
            }
            9 -> {
                finish()
            }
        }
    }


    private fun getOrderDetail() {
        showDialog("加载中...", false)
        HttpManager.getOrderDetail(orderId!!).request(this) { _, data ->
            data?.let {
                this.order = it
                setUI()
            }
        }
    }


    override fun finish() {
        if (isUpdateStatus) {
            val data = Intent()
            data.putExtra("data", order)
            setResult(Activity.RESULT_OK, data)
        }
        super.finish()
    }

    private fun pay() {
        //农行支付
        if (payWay == 0) {
            toast("功能开发中,请等待")
        }
        if (payWay == 2) {
            PayUtil.initWeChatPay(this, Const.WX_APP_ID)
            if (!PayUtil.checkSupportWeChat(this)) {
                toast("请先安装微信，再进行支付")
                return
            }
            getPayInfo()
            toast("功能开发中,请等待")
        }

        if (payWay == 1) {
            getPayInfo()
        }
    }

    override fun onPaySuccess() {
        isUpdateStatus = true
        if (order!!.status == 5){
            order!!.status = 7
            setUI()
        }else
            finish()
    }

    private fun getPayInfo() {
        showDialog()
        HttpManager.getPayInfo(order!!.id!!,payWay,1 ).request(this) { _, data ->
            data?.let {
                when (payWay) {
                //支付宝
                    1 -> PayUtil.aliPay(this, it.orderInfo)
                //微信
                    2 -> PayUtil.weChatPay(it)
                    else -> {
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        try {
            PayUtil.removePayListener(this)
        } catch (e: Exception) {
        }
        super.onDestroy()
    }
}
