package com.hbcx.user.ui.ticket

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.*
import cn.sinata.xldutils.visible
import com.hbcx.user.R
import com.hbcx.user.network.Api
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.H5Activity
import com.hbcx.user.ui.PayActivity
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.ui.rent.RentSafeActivity
import com.hbcx.user.ui.rent.SelectDriverActivity
import com.hbcx.user.ui.user.CouponActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_ticket_order_confirm.*
import kotlinx.android.synthetic.main.item_passenger.view.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import java.lang.StringBuilder

class TicketOrderConfirmActivity : TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.activity_ticket_order_confirm

    private val userId by lazy {
        SPUtils.instance().getInt(Const.User.USER_ID)
    }
    private val start by lazy {
        intent.getStringExtra("start")
    }
    private val end by lazy {
        intent.getStringExtra("end")
    }
    private val startPoint by lazy {
        intent.getStringExtra("startPoint")
    }
    private val endPoint by lazy {
        intent.getStringExtra("endPoint")
    }
    private val startTime by lazy {
        intent.getStringExtra("startTime")
    }
    private val endTime by lazy {
        intent.getStringExtra("endTime")
    }
    private val date by lazy {
        intent.getLongExtra("date", 0)
    }
    private val money by lazy {
        intent.getDoubleExtra("money", 0.0)
    }
    private val ticketNum by lazy {
        //余票数
        intent.getIntExtra("ticketNum", 3)
    }
    private var payMoney = 0.0 //支付总价
    private var safeMoney = 0.0 //保险单价
    private var discountMoney = 0.0 //积分抵扣金额
    private var serverMoney = 0.0 //服务费
    private var passengerNum = 0 //乘客数量
    private var isBuySafe = true
    private var totalScore = 0 //用户总积分
    private var usefulScore = 0 //积分可用门槛

    private var passengers = arrayListOf<com.hbcx.user.beans.Driver>() //乘车人

    private var coupon: com.hbcx.user.beans.Coupon? = null //选择的优惠

    private lateinit var orderNum: String //下单成功后的订单编号


    override fun initClick() {
        iv_add_passenger.onClick {
            startActivityForResult<SelectDriverActivity>(1, "isPassenger" to true, "ticketNum" to ticketNum,"datas" to passengers.map {
                it.id
            })
        }
        iv_add_phone.onClick {
            RxPermissions(this@TicketOrderConfirmActivity).request(Manifest.permission.READ_CONTACTS).subscribe {
                if (it) {
                    startActivityForResult(Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 0)
                } else {
                    toast("没有权限")
                }
            }
        }
        tv_safe_money.onClick {
            startActivityForResult<RentSafeActivity>(2, "isTicket" to true, "money" to safeMoney)
        }
        tv_coupon.onClick {
            startActivityForResult<CouponActivity>(3, "useType" to 1, "orderMoney" to payMoney + (coupon?.money
                    ?: 0.0))
        }
        tv_cancel_rule.onClick {
            startActivity<H5Activity>("title" to "退票须知", "url" to Api.CANCEL_RULE)
        }
        tv_price.onClick {
            rl_detail.visibility = if (rl_detail.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        tv_action.onClick {
            if (passengerNum == 0) {
                toast("请添加乘客")
                return@onClick
            }
            val phone = et_phone.text.toString().trim()
            if (!phone.isValidPhone()) {
                toast("请填写正确的联系电话")
                return@onClick
            }
            val passengersId = StringBuilder()
            passengers.forEach {
                passengersId.append(it.id).append(",")
            }
            passengersId.deleteCharAt(passengersId.length - 1)
            showDialog(canCancel = false)
            HttpManager.createTicketOrder(userId, passengersId.toString(), date.toTime("yyyy-MM-dd ") + startTime,
                    intent.getIntExtra("id", 0), intent.getIntExtra("startPointId", 0),
                    intent.getIntExtra("endPointId", 0), passengerNum, payMoney, serverMoney * passengerNum,
                    coupon?.id ?: 0, coupon?.money ?: 0.0, if (switch_btn.isChecked) discountMoney else 0.0,
                    if (isBuySafe) safeMoney * passengerNum else 0.0, startPoint, endPoint, start, end, phone).request(this@TicketOrderConfirmActivity) { _, data ->
                data?.let {
                    orderNum = it.optString("orderNum")
                    startActivityForResult<PayActivity>(4, "id" to it.optInt("id"), "money" to payMoney, "time" to it.optLong("createTime"), "isCreate" to 1, "type" to 2)
                }
            }
        }
        switch_btn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (discountMoney >= payMoney) {
                    toast("抵扣金额不能大于订单金额")
                    switch_btn.isChecked = false
                    return@setOnCheckedChangeListener
                }
                tv_score_discount_money.visible()
                tv_score.text = SpanBuilder("共${totalScore}积分")
                        .bold(1, totalScore.toString().length + 1).build()
            } else {
                tv_score_discount_money.gone()
                val s = String.format("%.2f", discountMoney)
                tv_score.text = SpanBuilder("共${totalScore}积分，可抵扣￥$s")
                        .bold(1, totalScore.toString().length + 1).bold(totalScore.toString().length + 8, totalScore.toString().length + s.length + 8).build()
            }
            updatePrice()
        }
    }

    override fun initView() {
        title = "预订车票"
        et_phone.setText(SPUtils.instance().getString(Const.User.USER_PHONE))
        tv_start_city.text = start
        tv_end_city.text = end
        tv_start_address.text = startPoint
        tv_end_address.text = endPoint
        tv_time.text = "${date.toTime("MM月dd日 ")}${date.toWeek()} ${startTime}上车"
        if (endTime != null && endTime.isNotEmpty() && endTime != "null")
            tv_arrive_time.text = String.format("预计%s到达", endTime)
        tv_limit.text = "限购${ticketNum}张"
        getData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                1 -> {
                    val drivers = data.getSerializableExtra("driver") as ArrayList<com.hbcx.user.beans.Driver>
                    passengerNum = drivers.size
                    updatePrice()
                    passengers.clear()
                    passengers.addAll(drivers)
                    ll_passenger.removeViews(1, ll_passenger.childCount - 1)
                    if (drivers.isNotEmpty()) {
                        drivers.forEach {
                            val view = LayoutInflater.from(this).inflate(R.layout.item_passenger, null)
                            view.tv_name.text = it.name
                            view.tv_id_card.text = "身份证号 ${it.idCards}"
                            view.iv_delete.onClick { _ ->
                                ll_passenger.removeView(view)
                                passengers.remove(it)
                                passengerNum--
                                updatePrice()
                            }
                            ll_passenger.addView(view)
                        }
                    }
                }
                0 -> {
                    val uri = data.data
                    val contacts = getPhoneContacts(uri)
                    et_phone.setText(contacts[1].replace(" ", ""))
                }
                2 -> {
                    val safe = data.getIntExtra("safe", 0)
                    if (safe == 1) {
                        tv_safe_money.text = "放弃出行保障"
                        isBuySafe = false
                    } else {
                        tv_safe_money.text = "￥${safeMoney}元/人"
                        isBuySafe = true
                    }
                    updatePrice()
                }
                3 -> {
                    coupon = data.getSerializableExtra("data") as com.hbcx.user.beans.Coupon
                    tv_coupon.text = "抵扣${coupon?.money}元"
                    tv_coupon_count.text = ""
                    updatePrice()
                }
                4 -> {//支付成功
                    startActivity<TicketPaySuccessActivity>("id" to data.getIntExtra("id", 0), "orderNum" to orderNum)
                    finish()
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED && requestCode == 3) {
            coupon = null
            tv_coupon.text = "未使用"
            updatePrice()
        }
    }

    private fun updatePrice() {
        tv_person_count.text = "${passengerNum}人"
        tv_ticket_price.text = SpanBuilder("￥${money}×${passengerNum}人").color(this, 0, money.toString().length + 1, R.color.color_money_text).build()
        tv_safe_price.text = SpanBuilder("￥${if (isBuySafe) safeMoney else 0.0}×${passengerNum}人").color(this, 0, safeMoney.toString().length + 1, R.color.color_money_text).build()
        tv_service_price.text = SpanBuilder("￥${serverMoney}×${passengerNum}人").color(this, 0, serverMoney.toString().length + 1, R.color.color_money_text).build()
        val original = (money + (if (isBuySafe) safeMoney else 0.0) + serverMoney) * passengerNum //抵扣前价格
        payMoney = original - (if (switch_btn.isChecked) discountMoney else 0.0) - (coupon?.money
                ?: 0.0)
        if (payMoney <= 0) { //金额不能被抵扣的情况
            coupon = null
            tv_coupon.text = "未使用"
            payMoney = original
            switch_btn.isChecked = false
        }
        tv_score_price.text = "￥${if (switch_btn.isChecked) discountMoney else 0.0}"
        tv_coupon_price.text = "￥${coupon?.money ?: 0.0}"
        tv_price.text = String.format("￥%.2f", payMoney)
        if (coupon == null)
            getUsefulCouponNum()
    }

    /**
     * 获取下单相关数据
     */
    private fun getData() {
        HttpManager.getTicketOrderData(userId, money).request(this) { _, data ->
            safeMoney = data?.optDouble("ticketInsuranceMoney") ?: 0.0
            totalScore = data?.optInt("integral") ?: 0
            usefulScore = data?.optInt("fullIntegral") ?: 0
            serverMoney = data?.optDouble("serverMoney") ?: 0.0
            discountMoney = data?.optDouble("money") ?: 0.0
            if (discountMoney>10)
                discountMoney = 10.0
            tv_safe_money.text = String.format("￥%.2f元/人", safeMoney)
            tv_score_discount_money.text = "抵扣￥$discountMoney"
            if (totalScore >= usefulScore) {
                switch_btn.visible()
                val s = String.format("%.2f", discountMoney)
                tv_score.text = SpanBuilder("共${totalScore}积分，可抵扣￥$s")
                        .bold(1, totalScore.toString().length + 1).bold(totalScore.toString().length + 8, totalScore.toString().length + s.length + 8).build()
            } else {
                tv_score.text = SpanBuilder("共${totalScore}积分，满${usefulScore}积分可用")
                        .bold(1, totalScore.toString().length + 1).bold(totalScore.toString().length + 5, totalScore.toString().length + usefulScore.toString().length + 5).build()
            }
            updatePrice()
        }
    }

    private fun getUsefulCouponNum() {
        HttpManager.couponNum(userId, payMoney, 1).request(this) { _, data ->
            data?.let {
                tv_coupon_count.text = "(${it.optInt("couponNum")}张可用)"
            }
        }
    }

    private fun getPhoneContacts(uri: Uri): ArrayList<String> {
        val contact: ArrayList<String> = arrayListOf()
        val cr = contentResolver
        val cursor: Cursor = cr.query(uri, null, null, null, null)
        cursor.moveToFirst()
        val nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
        contact.add(cursor.getString(nameFieldColumnIndex))
        val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
        val phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null)
        if (phone != null) {
            phone.moveToFirst()
            contact.add(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))
        }
        phone.close()
        cursor.close()
        return contact
    }
}