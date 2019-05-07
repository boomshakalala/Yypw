package com.hbcx.user.ui.rent

import android.app.Activity
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.isValidIdCard
import cn.sinata.xldutils.utils.isValidPhone
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.AuthenTicationUtil
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_add_driver.*
import org.jetbrains.anko.toast

class AddDriverActivity : TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.activity_add_driver

    override fun initClick() {
        tv_add.setOnClickListener {
            addDriver()
        }
    }

    private val userId by lazy {
        SPUtils.instance().getInt(Const.User.USER_ID)
    }

    private var driver: com.hbcx.user.beans.Driver? = null

    private fun addDriver() {
        val name = et_name.text.toString().trim()
        val idCard = et_id_card.text.toString().trim()
        val num = et_num.text.toString().trim()
        val phone = et_phone.text.toString().trim()
        if (name.isEmpty()) {
            toast("请输入姓名")
            return
        }
        if (!idCard.isValidIdCard()) {
            toast("请输入正确的身份证号")
            return
        }
        if (num.isEmpty()) {
            toast("请输入档案编号")
            return
        }
        if(num.isNotEmpty() && AuthenTicationUtil.IDCardValidate(num) != "YES"){
            toast("请输入正确的档案编号")
            return
        }
        if (phone.isNotEmpty() && !phone.isValidPhone()) {
            toast("手机号不正确")
            return
        }
        tv_add.isEnabled = false
        HttpManager.addPerson(userId, name, idCard, 1, num, phone, driver?.id
                ?: 0).request(this,success = { _, _ ->
            toast(if (driver != null) "修改成功" else "添加成功")
            setResult(Activity.RESULT_OK)
            finish()
        },error = {_,_->
            tv_add.isEnabled = true
        })
    }

    override fun initView() {
        val data = intent.getSerializableExtra("data")
        if (data != null) {
            driver = data as com.hbcx.user.beans.Driver
            et_name.setText(driver!!.name)
            et_id_card.setText(driver!!.idCards)
            et_num.setText(driver!!.licenseNum)
            et_phone.setText(driver!!.phone)
            tv_add.text = "确认修改"
        }
        title = if (driver == null) "新增驾驶员" else "编辑驾驶员"
    }

}
