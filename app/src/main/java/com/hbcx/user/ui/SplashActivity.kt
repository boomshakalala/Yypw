package com.hbcx.user.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import cn.sinata.xldutils.activity.BaseActivity
import cn.sinata.xldutils.activity.WebViewActivity
import cn.sinata.xldutils.utils.optInt
import cn.sinata.xldutils.utils.optString
import com.hbcx.user.R
import com.hbcx.user.network.Api
import com.hbcx.user.network.HttpManager
import com.hbcx.user.utils.request
import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.startActivity

class SplashActivity: BaseActivity(){
    private var type = 0
    private var url = ""

    private var timer:CountDownTimer = object : CountDownTimer(4000, 1000){
        override fun onFinish() {
            startActivity<MainActivity>()
            finish()
        }

        override fun onTick(millisUntilFinished: Long) {
            tv_next.text = "${millisUntilFinished/1000}s 跳过"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        tv_next.setOnClickListener {
            startActivity<MainActivity>()
            timer.cancel()
            finish()
        }

        iv_splash.setOnClickListener {
            if (type == 2) {
                val u = Api.URL+url
                startActivity<WebViewActivity>("url" to u)
            } else if (type == 3) {
                startActivity<WebViewActivity>("url" to url)
            }
        }
        getAd()
        startTimer()
    }

    private fun startTimer(){
        tv_next.visibility = View.VISIBLE
        timer.start()
    }

    private fun getAd(){
        HttpManager.getAd().request(this){_,data->
            url = data?.optString("jumpUrl")?:""
            type = data?.optInt("jumpType")?:0
            iv_splash.setImageURI(data?.get("imgUrl")?.asString)
        }
    }
}
