package com.hbcx.user.ui.user

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import cn.sinata.xldutils.utils.SPUtils
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_feedback.*
import org.jetbrains.anko.toast

class FeedbackActivity:TranslateStatusBarActivity(), TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        if (s!=null){
            tv_count.text = "还可以输入${200 - s.length}字"
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun setContentView() = R.layout.activity_feedback

    override fun initClick() {
    }

    override fun initView() {
        title = "意见反馈"
        titleBar.addRightButton("提交",onClickListener = View.OnClickListener {
            val content = et_content.text.toString()
            if (content.isEmpty()){
                toast("请输入内容")
                return@OnClickListener
            }
            titleBar.getRightButton(0)?.isEnabled = false
            showDialog()
            HttpManager.feedback(SPUtils.instance().getInt(Const.User.USER_ID),content).request(this,success = {_,_->
                toast("提交成功")
                finish()
            },error = {_,_->
                titleBar.getRightButton(0)?.isEnabled = true
            })
        })
        et_content.addTextChangedListener(this)
    }
}