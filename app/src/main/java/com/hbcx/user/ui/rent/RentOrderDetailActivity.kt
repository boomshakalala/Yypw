package com.hbcx.user.ui.rent

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import cn.sinata.xldutils.callPhone
import cn.sinata.xldutils.utils.hideIdCard
import cn.sinata.xldutils.utils.toTime
import cn.sinata.xldutils.utils.toWeek
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.help.Tip
import com.hbcx.user.R
import com.hbcx.user.dialogs.PriceDialog
import com.hbcx.user.dialogs.TipDialog
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.MainActivity
import com.hbcx.user.ui.PayActivity
import com.hbcx.user.ui.PaySuccessActivity
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.request
import com.hbcx.user.utils.requestByF
import kotlinx.android.synthetic.main.activity_rent_order.*
import org.jetbrains.anko.*

class RentOrderDetailActivity : TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.activity_rent_order

    private val id by lazy {
        intent.getIntExtra("id", 0)
    }
    private val isCreate by lazy {
        //下单页面过来
        intent.getIntExtra("isCreate", 0)
    }

    override fun initClick() {
    }

    override fun initView() {
        title = "订单详情"
        showDialog()
        getData()
    }

    private fun getData() {
        HttpManager.getRentOrderDetail(id).request(this) { _, data ->
            data?.let {
                tv_state.text = it.getStateStr()
                tv_state.textColorResource = it.getStateColor()
                tv_start_date.text = it.startTime.toTime("MM-dd")
                tv_end_date.text = it.endTime.toTime("MM-dd")
                tv_start_time.text = "${it.startTime.toWeek()} ${it.startTime.toTime("HH:mm")}"
                tv_end_time.text = "${it.endTime.toWeek()} ${it.endTime.toTime("HH:mm")}"
                tv_rent_day.text = "${it.times}${if (it.hourOrDay == 1) "小时" else "天"}"
                tv_title.text = "${it.brandName}${it.modelName}"
                tv_type.text = it.levelName
                tv_parameter.text = "${it.gears}·${it.pedestal}座·${it.displacement}"
                iv_car.setImageURI(it.imgUrl)
                tv_company.text = it.companyName
                tv_rent_point.text = "取车门店：距${it.address}${String.format("%.2f", it.distance / 1000)}km"
                tv_rent_point.setOnClickListener {
                    val tip = Tip()
                    tip.setPostion(LatLonPoint(data.lat, data.lon))
                    tip.name = data.address
                    startActivity<CompanyActivity>("id" to data.companyId, "tip" to tip)
                }
                tv_open_time.text = "营业时间：${it.doBusinessStartTime}-${it.doBusinessEndTime}"
                iv_call.setOnClickListener {
                    callPhone(data.contactNumber)
                }
                it.rentingCarLabelList.map {
                    val textView = TextView(this)
                    textView.text = it.name
                    textView.setTextColor(Color.parseColor("#FFA200"))
                    textView.setBackgroundResource(R.drawable.bg_label_orange)
                    textView.setPadding(dip(4), dip(2), dip(4), dip(2))
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12F)
                    fl_container.addView(textView)
                }
                tv_order_num.text = it.orderNum
                tv_order_time.text = it.createTime.toTime("yyyy-MM-dd HH:mm")
                tv_driver.text = it.name
                tv_id_card.text = it.idCards.hideIdCard()
                tv_phone.text = it.phone
                tv_phone.setOnClickListener {
                    callPhone(data.phone)
                }
                tv_money.text = "￥${it.orderMoney}"
                tv_negative.visibility = if (data.status == 4) View.GONE else View.VISIBLE
                tv_negative.text = data.getCancelStr()
                tv_positive.visibility = if (data.status in 5..7) View.GONE else View.VISIBLE
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
                    } else if (it.text == "立即支付")
                        startActivityForResult<PayActivity>(1,"money" to data.payMoney, "id" to data.id, "time" to data.createTime)
                }
                iv_detail.setOnClickListener {
                    val priceDialog = PriceDialog()
                    priceDialog.arguments = bundleOf("order" to data)
                    priceDialog.show(supportFragmentManager, "price")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK&&data!=null){
            startActivity<PaySuccessActivity>("id" to id)
        }
    }
    private fun cancelOrder() {
        val tipDialog = TipDialog()
        tipDialog.arguments = bundleOf("msg" to "是否取消该订单", "cancel" to "取消", "ok" to "确定")
        tipDialog.setDialogListener { p, s ->
            HttpManager.cancelRentOrder(id).request(this) { _, _ ->
                toast("取消成功")
                setResult(Activity.RESULT_OK)
                if (isCreate == 1) {
                    startActivity<MainActivity>()
                }
                finish()
            }
        }
        tipDialog.show(supportFragmentManager, "cancel")
    }

    private fun deleteOrder() {
        val tipDialog = TipDialog()
        tipDialog.arguments = bundleOf("msg" to "是否删除该订单", "cancel" to "取消", "ok" to "确定")
        tipDialog.setDialogListener { p, s ->
            HttpManager.deleteRentOrder(id).request(this) { _, _ ->
                toast("删除成功")
                setResult(Activity.RESULT_OK)
                if (isCreate == 1) {
                    startActivity<MainActivity>()
                }
                finish()
            }
        }
        tipDialog.show(supportFragmentManager, "cancel")
    }

    override fun onBackPressed() {
        if (isCreate == 1) {
            startActivity<MainActivity>()
        } else
            super.onBackPressed()
    }
}
