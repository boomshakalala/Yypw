package com.hbcx.user.ui.trip


import cn.sinata.xldutils.utils.SpanBuilder
import com.hbcx.user.R
import com.hbcx.user.ui.TranslateStatusBarActivity
import kotlinx.android.synthetic.main.activity_price_detail.*


class PriceDetailActivity : TranslateStatusBarActivity() {
    override fun setContentView(): Int = R.layout.activity_price_detail

    override fun initClick() {
    }

    override fun initView() {
        title = "费用明细"
        if (order != null) {
            setUI()
        }
    }

    private fun setUI() {
//        tv_content1.text = order!!.departureTime!!.timeDay()
//        tv_content2.text = order!!.startAddress
//        tv_content3.text = order!!.endAddress
        val p = String.format("%.2f元", order!!.orderMoney)
        tv_price.text = SpanBuilder(p)
                .size(p.length - 1, p.length, 14)
                .color(this, p.length - 1, p.length, R.color.black_text)
                .build()
        tv_mileage_price.text = String.format("里程费（%.2f公里）", order!!.mileage)
        tv_time_price.text = String.format("时长费（%d分钟）", order!!.duration)
        tv_long_price.text = String.format("远途费（%.2f公里）", order!!.longMileage)
        tv_price_1.text = "${order!!.mileageMoney}元"
        tv_price_2.text = "${order!!.durationMoney}元"
        tv_price_3.text = "${order!!.longDurationMoney}元"
        tv_price_4.text = "${order!!.nightMoney}元"
        tv_price_5.text = "${order!!.couponsMoney}元"
    }


    private val order by lazy {
        intent.getSerializableExtra("order") as com.hbcx.user.beans.Order
    }

}
