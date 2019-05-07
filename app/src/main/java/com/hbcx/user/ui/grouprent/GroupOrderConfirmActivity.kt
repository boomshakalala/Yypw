package com.hbcx.user.ui.grouprent

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import cn.sinata.xldutils.adapter.ImagePagerAdapter
import com.amap.api.services.help.Tip
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import android.provider.ContactsContract
import cn.sinata.xldutils.callPhone
import cn.sinata.xldutils.utils.*
import com.hbcx.user.network.Api
import com.hbcx.user.ui.H5Activity
import com.hbcx.user.ui.PaySuccessActivity
import com.hbcx.user.ui.rent.CompanyActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_group_order_confirm.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


class GroupOrderConfirmActivity : TranslateStatusBarActivity() {
    private val id by lazy {
        intent.getIntExtra("id", 0)
    }
    private val userId by lazy {
        SPUtils.instance().getInt(Const.User.USER_ID)
    }

    private val type by lazy {
        intent.getStringExtra("type")
    }

    private val startTip by lazy {
        intent.getParcelableExtra("startTip") as Tip
    }
    private val endTip by lazy {
        intent.getParcelableExtra("endTip") as Tip
    }
    private val startTime by lazy {
        intent.getLongExtra("startTime", 0)
    }
    private val duration by lazy {
        intent.getIntExtra("duration", 0)
    }
    private val startCity by lazy {
        intent.getStringExtra("startCity")
    }
    private val endCity by lazy {
        intent.getStringExtra("endCity")
    }

    private var carDetail: com.hbcx.user.beans.GroupCarDetail? = null //页面数据

    private var num = 1 //车辆数量

    override fun setContentView() = R.layout.activity_group_order_confirm

    override fun initClick() {
        iv_add_phone.setOnClickListener {
            RxPermissions(this).request(Manifest.permission.READ_CONTACTS).subscribe {
                if (it) {
                    startActivityForResult(Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 0)
                } else {
                    toast("没有权限")
                }
            }
        }
        rb_rule.setOnClickListener {
            rb_rule.isChecked = !rb_rule.isChecked
        }
        tv_action.setOnClickListener {
            val phone = et_phone.text.toString()
            if (!phone.isValidPhone()) {
                toast("请输入正确的手机号")
                return@setOnClickListener
            }
            if (!rb_rule.isChecked) {
                toast("请同意预约条款")
                return@setOnClickListener
            }
            showDialog()
            carDetail?.let {
                HttpManager.createGroupRentOrder(it.id, userId, startTip.name, endTip.name, startCity, endCity, startTip.point.longitude
                        , startTip.point.latitude, endTip.point.longitude, endTip.point.latitude, phone, startTime, duration, num).request(this) { _, data ->
                    data?.let {
                        startActivity<PaySuccessActivity>("id" to it.optInt("id"), "type" to 1)
                        finish()
                    }
                }
            }
        }
        tv_reduce.setOnClickListener {
            if (num == 1) else num--
            tv_count.text = "${num}辆"
            updatePrice()
        }
        tv_add.setOnClickListener {
            if (num == 5)
                toast("包车数量最多5辆")
            else num++
            tv_count.text = "${num}辆"
            updatePrice()
        }
        tv_rule.setOnClickListener {
            startActivity<H5Activity>("title" to "预定条款", "url" to Api.GROUP_RENT_RULE)
        }
    }

    override fun initView() {
        title = "填写订单"
        showDialog()
        getData()
        tv_start_city.text = startCity
        tv_start_address.text = startTip.name
        tv_end_city.text = endCity
        et_phone.setText(SPUtils.instance().getString(Const.User.USER_PHONE))
        tv_end_address.text = endTip.name
        tv_start_time_and_duration.text = "${startTime.toTime("MM-dd")} ${startTime.toWeek()} ${startTime.toTime("HH:mm")}出发 ${duration}天"
        tv_type.text = type
    }

    private fun getData() {
        HttpManager.getGroupCarDetail(id, duration, startTip.point.longitude, startTip.point.latitude, endTip.point.longitude, endTip.point.latitude).request(this) { _, data ->
            data?.let {
                carDetail = it
                updatePrice()
                view_pager.adapter = ImagePagerAdapter(supportFragmentManager, it.imgUrl.map { it.imgUrl } as ArrayList<String>)
                com.hbcx.user.views.ViewPagerIndicator(this, ll_indicator, R.mipmap.indicator_gray, R.mipmap.indicator_white, it.imgUrl.size).setupWithBanner(view_pager)
                tv_name.text = it.brandName + it.modelName
                tv_person_count.text = "可乘坐${it.pedestal}人"
                tv_company.text = it.companyName
                tv_rent_point.text = "公司地址：${it.address}"
                tv_open_time.text = "营业时间：${it.startTime}-${it.endTime}"
                val s = "￥${it.money}/天"
                tv_money.text = SpanBuilder(s).size(s.length - 2, s.length, 12).color(this, s.length - 2, s.length, R.color.black_text).build()
                rl_company.setOnClickListener {
                    startActivity<CompanyActivity>("id" to data.companyId, "type" to 1, "tip" to startTip)
                }
                iv_call.setOnClickListener {
                    callPhone(data.contactNumber)
                }
            }
        }
    }

    private fun updatePrice() {
        if (carDetail == null)
            return
        tv_price.text = String.format("￥%.2f—￥%.2f", carDetail!!.startMoney * num, carDetail!!.endMoney * num)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                0 -> {
                    val uri = data.data
                    val contacts = getPhoneContacts(uri)
                    et_phone.setText(contacts[1].replace(" ", ""))
                }
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