package com.hbcx.user.ui.rent

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import cn.sinata.xldutils.adapter.ImagePagerAdapter
import com.amap.api.services.help.Tip
import com.hbcx.user.R
import com.hbcx.user.network.Api
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.H5Activity
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import android.provider.ContactsContract
import cn.sinata.xldutils.callPhone
import cn.sinata.xldutils.utils.*
import com.hbcx.user.dialogs.LabelListDialog
import com.hbcx.user.ui.PayActivity
import com.hbcx.user.ui.PaySuccessActivity
import com.hbcx.user.ui.user.CouponActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_order_confirm.*
import org.jetbrains.anko.*


class OrderConfirmActivity : TranslateStatusBarActivity() {
    private val HANDLER_TIME = 2000L //自动轮播时间
    private val mHandler = Handler()

    private val id by lazy {
        intent.getIntExtra("id", 0)
    }
    private val tip by lazy {
        intent.getParcelableExtra("tip") as Tip
    }
    private val startTime by lazy {
        intent.getLongExtra("startTime", 0)
    }
    private val endTime by lazy {
        intent.getLongExtra("endTime", 0)
    }
    private val duration by lazy {
        intent.getStringExtra("duration")
    }
    private val hourOrDay by lazy { //小时租车或者天(1=小时，2=天)
        if (duration.endsWith("小时")) 1 else 2
    }
    private val userId by lazy {
        SPUtils.instance().getInt(Const.User.USER_ID)
    }

    private var carDetail: com.hbcx.user.beans.CarDetail? = null //页面数据
    private var carSafe: com.hbcx.user.beans.CarSafe? = null //租车保障费
    private var safeMoney:Double = 0.0 //选择的保险
    private var orderMoney:Double = 0.0 //订单总额，供优惠券页面使用

    private var driver: com.hbcx.user.beans.Driver? = null //选择的司机
    private var coupon: com.hbcx.user.beans.Coupon? = null //选择的优惠

    override fun setContentView() = R.layout.activity_order_confirm

    override fun initClick() {
        tv_price.setOnClickListener {
            rl_detail.visibility = if (rl_detail.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        rb_rule.setOnClickListener {
            rb_rule.isChecked = !rb_rule.isChecked
        }
        tv_rule.setOnClickListener {
            startActivity<H5Activity>("title" to "预定条款", "url" to Api.RENT_RULE)
        }
        tv_driver.setOnClickListener {
            startActivityForResult<SelectDriverActivity>(1,"isPassenger" to false)
        }
        iv_add_phone.setOnClickListener {
            RxPermissions(this).request(Manifest.permission.READ_CONTACTS).subscribe {
                if (it) {
                    startActivityForResult(Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 0)
                } else {
                    toast("没有权限")
                }
            }
        }
        tv_coupon.setOnClickListener {
            startActivityForResult<CouponActivity>(2,"orderMoney" to orderMoney)
        }
        tv_action.setOnClickListener {
            if (!rb_rule.isChecked){
                toast("请同意预约条款")
                return@setOnClickListener
            }
            if (driver==null){
                toast("请选择驾驶员")
                return@setOnClickListener
            }
            val phone = et_phone.text.toString()
            if (!phone.isValidPhone()){
                toast("请输入正确的手机号")
                return@setOnClickListener
            }
            showDialog()
            tv_action.isEnabled = false
            carDetail?.let {
                HttpManager.createRentOreder(it.id,userId,driver!!.id,tip.name,tip.point.longitude,tip.point.latitude,phone,startTime,endTime,
                        if (coupon==null) 0 else coupon!!.id,safeMoneyAll, it.company_id,hourOrDay,
                        duration.substring(0,if (hourOrDay==1) duration.length-2 else duration.length -1).toDouble()).request(this,success = {_,data->
                    data?.let {
                        startActivityForResult<PayActivity>(4,"money" to it.optDouble("payMoney"),"id" to it.optInt("id"),"time" to System.currentTimeMillis(),"isCreate" to 1)
                    }
                },error = {_,_->
                    tv_action.isEnabled = true
                })
            }
        }
        fl_container.setOnClickListener {
            val dialog = LabelListDialog()
            dialog.arguments = bundleOf("data" to carDetail!!.rentingCarLabelList,"title" to carDetail!!.companyName)
            dialog.show(supportFragmentManager,"lab")
        }
    }

    override fun initView() {
        title = "填写订单"
        titleBar.addRightButton("预订须知", onClickListener = View.OnClickListener {
            startActivity<H5Activity>("title" to "预订须知", "url" to Api.RENT_RULE)
        })
        et_phone.setText(SPUtils.instance().getString(Const.User.USER_PHONE))
        val textView = titleBar.getRightButton(0) as TextView
        textView.setTextColor(resources.getColor(R.color.colorPrimary))
        showDialog()
        getData()
        tv_start_date.text = startTime.toTime("MM-dd")
        tv_end_date.text = endTime.toTime("MM-dd")
        tv_start_time.text = "${startTime.toWeek()} ${startTime.toTime("HH:mm")}"
        tv_end_time.text = "${endTime.toWeek()} ${endTime.toTime("HH:mm")}"
        tv_rent_day.text = duration
    }

    private fun getData() {
        HttpManager.getCarDetail(id, userId, tip.point.longitude, tip.point.latitude).request(this) { _, data ->
            data?.let {
                carDetail = it
                updatePrice()
                getUsefulCouponNum()
                view_pager.adapter = ImagePagerAdapter(supportFragmentManager, it.imgUrl.map { it.imgUrl } as ArrayList<String>)
                com.hbcx.user.views.ViewPagerIndicator(this,ll_indicator,R.mipmap.indicator_gray,R.mipmap.indicator_white,it.imgUrl.size).setupWithBanner(view_pager)
                tv_name.text = it.brandName + it.modelName
                tv_parameter.text = "${it.gears}·${it.pedestal}座·${it.displacement}"
                tv_type.text = it.levelName
                tv_company.text = it.companyName
                tv_rent_point.text = "取车门店：距${tip.name}${String.format("%.2fkm", it.distance.toDouble() / 1000)}"
                tv_open_time.text = "营业时间：${it.startTime}-${it.endTime}"
                it.rentingCarLabelList.map {
                    val textView = TextView(this)
                    textView.text = it.name
                    textView.setTextColor(Color.parseColor("#FFA200"))
                    textView.setBackgroundResource(R.drawable.bg_label_orange)
                    textView.setPadding(dip(4), dip(2), dip(4), dip(2))
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12F)
                    fl_container.addView(textView)
                }
                tv_rent_point.setOnClickListener {
                    startActivity<CompanyActivity>("id" to data.company_id, "tip" to tip)
                }
                iv_call.setOnClickListener {
                    callPhone(data.contact_number)
                }
                it.ruleList?.let {
                    tv_title_1.text = it[0].key
                    tv_title_2.text = it[1].key
                    tv_title_3.text = it[2].key
                    tv_title_4.text = it[3].key
                    tv_title_5.text = it[4].key
                    tv_title_6.text = it[5].key
                    tv_rule_1.text = it[0].value
                    tv_rule_2.text = it[1].value
                    tv_rule_3.text = it[2].value
                    tv_rule_4.text = it[3].value
                    tv_rule_5.text = it[4].value
                    tv_rule_6.text = it[5].value
                }
                mCount = it.imgUrl.size
                mHandler.postDelayed(runnableForViewPager, HANDLER_TIME)
            }
        }
        HttpManager.getCarSafe(id).request(this) { _, data ->
            data?.let {
                carSafe = it
                safeMoney = it.basicsMoney
                tv_safe.text = "基础保障服务￥$safeMoney/天"
                updatePrice()
                getUsefulCouponNum()
                tv_safe.setOnClickListener {
                    startActivityForResult<RentSafeActivity>(3,"data" to data)
                }
            }
        }
    }

    var itemPosition = 0
    var mCount = 0
    /**
     * ViewPager的定时器
     */
    private var runnableForViewPager: Runnable = object : Runnable {
        override fun run() {
            if (isDestroy)
                return
            try {
                itemPosition++
                view_pager.currentItem = itemPosition % mCount
                mHandler.postDelayed(this, HANDLER_TIME)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var safeMoneyAll = 0.0
    private fun updatePrice() {
        if (carDetail==null)
            return
        val rentMoney: Double
        val couponMoney :Double= coupon?.money?:0.0
        if (hourOrDay == 1){
            val s = "￥${carDetail?.hour_money}"
            tv_rent_price.text = SpanBuilder("${s}X$duration").color(this,0,s.length,R.color.color_money_text).build()
            rentMoney = carDetail!!.hour_money*((duration.substring(0,duration.length-2)).toDouble())
            safeMoneyAll = safeMoney
        }else{
            val s = "￥${carDetail?.day_money}"
            val d = (duration.substring(0, duration.length - 1)).toDouble()
            tv_rent_price.text = SpanBuilder("${s}X$duration").color(this,0,s.length,R.color.color_money_text).build()
            rentMoney = carDetail!!.day_money*d
            safeMoneyAll = safeMoney*d
        }
        val s = "￥$safeMoney/天"
        tv_safe_price.text =SpanBuilder(s).color(this,0,s.length-2,R.color.color_money_text).build()
        tv_service_price.text = "￥${carDetail!!.carLineMoney}"
        tv_coupon_price.text = "￥$couponMoney"
        orderMoney = rentMoney+safeMoneyAll+carDetail!!.carLineMoney
        tv_price.text = String.format("￥%.2f",orderMoney-couponMoney)
    }

    private fun getUsefulCouponNum(){
        HttpManager.couponNum(userId,orderMoney,2).request(this){_,data->
            data?.let {
                tv_coupon_count.text = "(${it.optInt("couponNum")}张可用)"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                1 -> {
                    if (data.getSerializableExtra("driver") == null) {
                        tv_driver.text = ""
                        driver = null
                    } else {
                        val drivers = data.getSerializableExtra("driver") as ArrayList<com.hbcx.user.beans.Driver>
                        if (drivers.isEmpty()){
                            tv_driver.text = ""
                            driver = null
                        }else{
                            driver = drivers[0]
                            tv_driver.text = driver!!.name
                        }
                    }
                }
                0 -> {
                    val uri = data.data
                    val contacts = getPhoneContacts (uri)
                    et_phone.setText(contacts[1].replace(" ",""))
                }
                2->{
                    coupon = data.getSerializableExtra("data")  as com.hbcx.user.beans.Coupon
                    tv_coupon.text = "抵扣${coupon?.money}元"
                    updatePrice()
                }
                3->{
                    if (data.getIntExtra("safe",0) == 0){
                        safeMoney = carSafe!!.basicsMoney
                        tv_safe.text = "基础保障服务￥$safeMoney/天"
                    }else{
                        safeMoney = carSafe!!.comprehensiveMoney
                        tv_safe.text = "全面保障服务￥$safeMoney/天"
                    }
                    updatePrice()
                    getUsefulCouponNum()
                }
                4->startActivity<PaySuccessActivity>("id" to data.getIntExtra("id",0))
            }
        }else if (resultCode == Activity.RESULT_CANCELED&&requestCode == 2){
            coupon = null
            tv_coupon.text = "未使用"
            updatePrice()
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