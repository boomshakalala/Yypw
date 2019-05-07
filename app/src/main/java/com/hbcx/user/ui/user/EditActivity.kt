package com.hbcx.user.ui.user

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Environment
import cn.sinata.xldutils.utils.SPUtils
import com.hbcx.user.R
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import top.zibin.luban.OnCompressListener
import android.text.TextUtils
import cn.qqtheme.framework.picker.DatePicker
import cn.sinata.xldutils.activity.SelectPhotoDialog
import cn.sinata.xldutils.utils.optString
import com.hbcx.user.dialogs.NickNameDialog
import com.hbcx.user.dialogs.SelectSexDialog
import com.hbcx.user.network.HttpManager
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_edit.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import top.zibin.luban.Luban
import java.io.File
import java.util.*


class EditActivity:TranslateStatusBarActivity() {
    override fun setContentView()= R.layout.activity_edit

    private val userId by lazy {
        SPUtils.instance().getInt(Const.User.USER_ID)
    }

    private var imgUrl = SPUtils.instance().getString(Const.User.USER_HEAD)
    private var nickName = SPUtils.instance().getString(Const.User.USER_NAME)
    private var birthDay = SPUtils.instance().getString(Const.User.USER_BIRTH)
    private var sex = SPUtils.instance().getInt(Const.User.USER_SEX)

    override fun initClick() {
        rl_head.onClick {
            startActivityForResult<SelectPhotoDialog>(1)
        }
        rl_name.onClick {
            val dialog = NickNameDialog()
            dialog.setDialogListener { _, s ->
                nickName = s!!
                update()
            }
            dialog.show(supportFragmentManager,"nickname")
        }
        tv_sex.onClick {
            val selectSexDialog = SelectSexDialog()
            selectSexDialog.setDialogListener { i, s ->
                sex = i+1
                update()
            }
            selectSexDialog.show(fragmentManager, "sex")
        }
        rl_birth.onClick {
            val picker = DatePicker(this@EditActivity)
            picker.setCanceledOnTouchOutside(true)
            picker.setUseWeight(true)
            picker.setRangeEnd(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
            picker.setRangeStart(1900, 1, 1)
            picker.setSelectedItem(1990, 1, 1)
            picker.setResetWhileWheel(false)
            picker.setDividerVisible(false)
            picker.setCancelTextColor(Color.parseColor("#999999"))
            picker.setTopLineColor(Color.parseColor("#999999"))
//            picker.setOffset(2)
            picker.setTextColor(resources.getColor(R.color.black_text))
            picker.setLabel("","","")
            picker.setSubmitTextColor(resources.getColor(R.color.colorPrimary))
            picker.setTextPadding(30)
            picker.setTextSize(16)
            picker.setOnDatePickListener(DatePicker.OnYearMonthDayPickListener { year, month, day ->
                birthDay = "$year-$month-$day"
                update()
            })
            picker.show()
        }
    }

    override fun initView() {
        title = "编辑资料"
        iv_head.setImageURI(SPUtils.instance().getString(Const.User.USER_HEAD))
        tv_name.text = SPUtils.instance().getString(Const.User.USER_NAME)
        tv_sex.text = if (SPUtils.instance().getInt(Const.User.USER_SEX) == 1) "男" else "女"
        tv_birth.text = SPUtils.instance().getString(Const.User.USER_BIRTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1&& resultCode == Activity.RESULT_OK&&data!=null){
            Luban.with(this)
                    .load(File(data.getStringExtra("path")))
                    .ignoreBy(100)
                    .setTargetDir(getPath())
                    .filter { path -> !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif")) }
                    .setCompressListener(object : OnCompressListener {
                        override fun onStart() {
                            showDialog(canCancel = false)
                        }
                        override fun onSuccess(file: File) {
                            HttpManager.uploadFile(File(data.getStringExtra("path"))).request(this@EditActivity){ _, data->
                                data?.let {
                                    imgUrl = it.optString("imgUrl")
                                    dismissDialog()
                                    update()
                                }
                            }
                        }
                        override fun onError(e: Throwable) {
                        }
                    }).launch()
        }
    }

    private fun getPath(): String {
        val path = Environment.getExternalStorageDirectory().toString() + "/Luban/image/"
        val file = File(path)
        return if (file.mkdirs()) {
            path
        } else path
    }

    private fun update(){
        showDialog()
        HttpManager.updateUserInfo(userId,imgUrl,nickName,sex,birthDay).request(this){_,_->
            toast("更新成功")
            setResult(Activity.RESULT_OK)
            iv_head.setImageURI(imgUrl)
            tv_name.text = nickName
            tv_sex.text = if (sex == 1) "男" else "女"
            tv_birth.text = birthDay
            SPUtils.instance().put(Const.User.USER_NAME, nickName).apply()
            SPUtils.instance().put(Const.User.USER_SEX, sex).apply()
            SPUtils.instance().put(Const.User.USER_BIRTH, birthDay).apply()
            SPUtils.instance().put(Const.User.USER_HEAD, imgUrl).apply()
        }
    }
}