package com.hbcx.user.ui

import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.SpanBuilder
import com.hbcx.user.R
import com.hbcx.user.ui.grouprent.GroupRentOrderDetailActivity
import com.hbcx.user.ui.rent.RentOrderDetailActivity
import com.hbcx.user.utils.Const
import kotlinx.android.synthetic.main.activity_pay_success.*
import org.jetbrains.anko.startActivity

class PaySuccessActivity : TranslateStatusBarActivity() {
    val id by lazy {
        intent.getIntExtra("id", 0)
    }
    val type by lazy { //0:租车支付  1：包车提交  2：包车支付
        intent.getIntExtra("type", 0)
    }

    override fun setContentView() = R.layout.activity_pay_success

    override fun initClick() {
        tv_go_home.setOnClickListener {
            startActivity<MainActivity>()
        }
        tv_see_detail.setOnClickListener {
            when (type) {
                0 -> startActivity<RentOrderDetailActivity>("id" to id,"isCreate" to 1)
                1 -> startActivity<GroupRentOrderDetailActivity>("id" to id,"isCreate" to true)
                2 -> startActivity<GroupRentOrderDetailActivity>("id" to id)
            }
            finish()
        }
    }

    override fun initView() {
        if (type == 0)
            tv_go_home.text = "返回租车首页"
        else
            tv_go_home.text = "返回包车首页"

        if (type == 1) {
            title = "完成提交"
            tv_msg.text = "提交成功"
            tv_tip_1.text = SpanBuilder("※ 商家会及时与您联系，了解你的详细包车需求以及交流实际包车费用").color(this, 0, 1, R.color.color_tv_orange).build()
            tv_tip_2.text = SpanBuilder("※ 与商家商议好价格后，您需支付一定的预订金，若您因其他原因需要取消包车，预订金不予退还").color(this, 0, 1, R.color.color_tv_orange).build()
            val phone = SPUtils.instance().getString(Const.SERVICE_PHONE)
            tv_hot_line.text = SpanBuilder("※ 若有任何疑问请拨打客服热线：$phone")
                    .color(this, 0, 1, R.color.color_tv_orange)
                    .color(this, 16, 16+phone.length, R.color.black_text).build()
        } else {
            title = "完成支付"
            tv_tip_1.text = SpanBuilder("※ 请根据预约时间到商家门店租车").color(this, 0, 1, R.color.color_tv_orange).build()
            tv_tip_2.text = SpanBuilder("※ 到店时请用身份证+驾驶证+驾驶员国内信用卡作为取车凭证").color(this, 0, 1, R.color.color_tv_orange).build()
            val phone = SPUtils.instance().getString(Const.SERVICE_PHONE)
            tv_hot_line.text = SpanBuilder("※ 若有任何疑问请拨打客服热线：$phone")
                    .color(this, 0, 1, R.color.color_tv_orange)
                    .color(this, 16, 16+phone.length, R.color.black_text).build()
        }
    }
}