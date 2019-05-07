package com.hbcx.user.ui.ticket

import android.app.Activity
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.rxutils.ResultSubscriber
import cn.sinata.xldutils.rxutils.request
import cn.sinata.xldutils.utils.*
import cn.sinata.xldutils.visible
import com.google.gson.JsonObject
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.AuthenTicationUtil
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_add_passenger.*
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast

class AddPassengerActivity :TranslateStatusBarActivity(){
    override fun setContentView() = R.layout.activity_add_passenger

    override fun initClick() {
        tv_add.setOnClickListener {
            addDriver()
        }
        rg_driver_license.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rb_yes){
                tv_tip.visible()
                ll_license.visible()
            }
            else{
                tv_tip.gone()
                ll_license.gone()
            }
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
        if (name.isEmpty()){
            toast("请输入姓名")
            return
        }
        if (!idCard.isValidIdCard()){
            toast("请输入正确的身份证号")
            return
        }
        if (rg_driver_license.checkedRadioButtonId == R.id.rb_yes&&num.isEmpty()) {
            toast("请输入档案编号")
            return
        }
        if(rg_driver_license.checkedRadioButtonId == R.id.rb_yes && AuthenTicationUtil.IDCardValidate(num) != "YES"){
            toast("请输入正确的档案编号")
            return
        }
        if (phone.isNotEmpty()&&!phone.isValidPhone()){
            toast("手机号不正确")
            return
        }
        tv_add.isEnabled = false
        HttpManager.addPerson(userId,name,idCard,if (rg_driver_license.checkedRadioButtonId==R.id.rb_yes) 1 else 2,
                if (rg_driver_license.checkedRadioButtonId==R.id.rb_yes) num else "",phone,driver?.id?:0).request(this@AddPassengerActivity,success = {_,_->
            toast(if (driver!=null) "修改成功" else "添加成功")
            setResult(Activity.RESULT_OK)
            finish()
        },error = {_,_->
            tv_add.isEnabled = true
        })
//        HttpManager.checkIdCard(name,idCard).subscribe(object : ResultSubscriber<JsonObject>(this@AddPassengerActivity){
//            override fun onNext(t: JsonObject) {
//                dismissDialog()
//                val result = t.optInt("error_code",-1)
//                val data = t.optJsonObj("result")
//                if (result == 0){
//                    val rel = data.optInt("res")
//                    if (rel == 1){ //匹配成功
//
//                    }
//
//                }else
//                    toast(t.optString("reason"))
//            }
//
//            override fun onError(t: Throwable) {
//                super.onError(t)
//                dismissDialog()
//                toast("银行卡查询出错")
//            }
//        })
    }

    override fun initView() {
        val data = intent.getSerializableExtra("data")
        if (data != null){
            driver = data as com.hbcx.user.beans.Driver
            et_name.setText(driver!!.name)
            et_id_card.setText(driver!!.idCards)
            et_num.setText(driver!!.licenseNum)
            et_phone.setText(driver!!.phone)
            if (driver!!.licenseOrNot == 1){
                rg_driver_license.check(R.id.rb_yes)
                ll_license.visible()
                tv_tip.visible()
            }else{
                rg_driver_license.check(R.id.rb_no)
                ll_license.gone()
                tv_tip.gone()
            }
            tv_add.text = "确认修改"
        }
        title = if (driver==null)"新增常用乘车人" else "编辑常用乘车人"
    }

}
