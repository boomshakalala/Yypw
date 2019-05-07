package com.hbcx.user.ui.ticket

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.widget.RatingBar
import com.hbcx.user.R
import com.hbcx.user.ui.TranslateStatusBarActivity
import android.os.Handler
import android.view.View
import cn.sinata.xldutils.utils.SPUtils
import com.hbcx.user.network.HttpManager
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_evaluate.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast


class EvaluateActivity : TranslateStatusBarActivity(), RatingBar.OnRatingBarChangeListener, TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        s?.let {
            if (it.isEmpty()) {
                tv_count.text = "还可以输入200字"
            } else {
                tv_count.text = "还可以输入${200 - it.length}字"
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    private val id by lazy { //订单id
        intent.getIntExtra("id",0)
    }

    private val userId by lazy {
        SPUtils.instance().getInt(Const.User.USER_ID)
    }

    private var sanitation = 0
    private var facility = 0
    private var onTimeRate = 0
    private var service = 0
    private var attitude = 0
    private var score = 0f
    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
        when (ratingBar) {
            rb_sanitation -> {
                sanitation = rating.toInt()
                tv_sanitation.text = sanitation.toString()
            }
            rb_facility -> {
                facility = rating.toInt()
                tv_facility.text = facility.toString()
            }
            rb_service -> {
                service = rating.toInt()
                tv_service.text = service.toString()
            }
            rb_attitude -> {
                attitude = rating.toInt()
                tv_attitude.text = attitude.toString()
            }
            rb_on_time_rate -> {
                onTimeRate = rating.toInt()
                tv_on_time_rate.text = onTimeRate.toString()
            }
        }
        score = (sanitation + facility + service + attitude + onTimeRate).toFloat() / 5
        tv_score.text = String.format("%.1f",score)
    }

    override fun setContentView() = R.layout.activity_evaluate

    override fun initClick() {
        rb_sanitation.onRatingBarChangeListener = this
        rb_attitude.onRatingBarChangeListener = this
        rb_on_time_rate.onRatingBarChangeListener = this
        rb_service.onRatingBarChangeListener = this
        rb_facility.onRatingBarChangeListener = this
        btn_action.onClick {
            if (sanitation == 0){
                toast("请为车辆卫生打分")
                return@onClick
            }
            if (facility == 0){
                toast("请为车辆设施打分")
                return@onClick
            }
            if (onTimeRate == 0){
                toast("请为车辆准点率打分")
                return@onClick
            }
            if (service == 0){
                toast("请为司乘服务打分")
                return@onClick
            }
            if (attitude == 0){
                toast("请为司乘态度打分")
                return@onClick
            }
            val s = et_content.text.toString().trim()
            if (s.isEmpty()){
                toast("请输入评价内容")
                return@onClick
            }
            HttpManager.evaluateTicket(id,userId,sanitation,facility,onTimeRate,service,attitude,s).request(this@EvaluateActivity){_,_->
                toast("评价成功")
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        et_content.setOnTouchListener { v, event ->
            changeScrollView()
            return@setOnTouchListener false
        }

    }

    override fun initView() {
        title = "立即评价"
        et_content.addTextChangedListener(this)
    }

    private fun changeScrollView() {
        Handler().postDelayed({
            //将ScrollView滚动到底
            mScrollView.fullScroll(View.FOCUS_DOWN)
            et_content.requestFocus()
        }, 100)
    }

}