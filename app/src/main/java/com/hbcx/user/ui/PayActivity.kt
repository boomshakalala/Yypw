package com.hbcx.user.ui

import android.app.Activity
import android.os.CountDownTimer
import android.util.Log
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.SpanBuilder
import cn.sinata.xldutils.utils.toTime
import com.hbcx.user.R
import com.hbcx.user.dialogs.TipDialog
import com.hbcx.user.interfaces.PayListener
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.rent.RentOrderDetailActivity
import com.hbcx.user.ui.ticket.TicketOrderDetailActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.PayUtil
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activit_pay.*
import kotlinx.android.synthetic.main.layout_pay.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast

class PayActivity : TranslateStatusBarActivity(), PayListener {
    override fun onPaySuccess() {
        setResult(Activity.RESULT_OK, intent.putExtra("id", id))
        finish()
    }

    override fun setContentView() = R.layout.activit_pay

    private val money by lazy {
        intent.getDoubleExtra("money", 0.0)
    }
    private val time by lazy {
        intent.getLongExtra("time", 0)
    }
    private val id by lazy {
        intent.getIntExtra("id", 0)
    }
    private val type by lazy {
        //0租车 1包车 //2票务
        intent.getIntExtra("type", 0)
    }
    private val isCreate by lazy {
        //下单页面过来
        intent.getIntExtra("isCreate", 0)
    }

    override fun initClick() {
        rl_ali.setOnClickListener {
            getPayInfo(1)
        }
        rl_bank.setOnClickListener {
            toast("功能开发中")
        }
        rl_wx.setOnClickListener {
            PayUtil.initWeChatPay(this, Const.WX_APP_ID)
            if (!PayUtil.checkSupportWeChat(this)) {
                toast("请先安装微信，再进行支付")
                return@setOnClickListener
            }
            getPayInfo(2)
        }
    }

    override fun initView() {
        tv_money.text = String.format("￥%.2f",money)
        if (type == 1) {
            title = "支付订金"
            tv_deadline.gone()
        } else {
            title = if (type == 0) "支付" else "支付方式"
            Log.e("socket_receive", "time:$time")
            Log.e("socket_receive", "time:${System.currentTimeMillis()}")
            object : CountDownTimer((if (type == 2) 5 else 30) * 60 * 1000 + time - System.currentTimeMillis(), 1000) {
                override fun onFinish() {
                    if(!isDestroy){
                        toast("订单已失效")
                        startActivity<MainActivity>()
                        finish()
                    }
                }

                override fun onTick(p0: Long) {
                    tv_deadline.text = SpanBuilder("请在${p0.toTime("mm分ss秒")}内完成支付，逾期将自动取消预订！").color(this@PayActivity, 2, 8, R.color.color_money_text).build()
                }
            }.start()
        }
        val phone = SPUtils.instance().getString(Const.SERVICE_PHONE)
        tv_hot_line.text = SpanBuilder("※ 若有任何疑问请拨打客服热线：$phone")
                .color(this, 0, 1, R.color.color_tv_orange)
                .color(this, 16, 16+phone.length, R.color.black_text).build()
        tv_tip.text = SpanBuilder("※ 支付完成后请到我的行程查看并确认是否订购成功，以免影响您的行程。")
                .color(this, 0, 1, R.color.color_tv_orange).build()
        PayUtil.addPayListener(this)
    }

    //1=支付宝,2 = 微信
    private fun getPayInfo(payWay: Int) {
        showDialog()
        HttpManager.getPayInfo(id, payWay, when (type) {
            0 -> 2
            1 -> 3
            else -> 4
        }).request(this) { _, data ->
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

    override fun onBackPressed() {
        val tipDialog = TipDialog()
        tipDialog.setCancelDialogListener { _, _ ->
            if (isCreate == 1) {
                if (type == 0)
                    startActivity<RentOrderDetailActivity>("id" to id, "isCreate" to isCreate)
                else
                    startActivity<TicketOrderDetailActivity>("id" to id, "isCreate" to isCreate)
            }
            finish()
        }
        tipDialog.arguments = bundleOf("cancel" to "确认离开", "ok" to "继续支付", "msg" to "超过支付时效后订单将被取消，请尽快完成支付!")
        tipDialog.show(supportFragmentManager, "tip")
    }
}
