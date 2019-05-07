package com.hbcx.user.ui.ticket

import android.app.Activity
import cn.sinata.xldutils.utils.parserTime
import cn.sinata.xldutils.utils.toTime
import com.hbcx.user.R
import com.hbcx.user.ui.TranslateStatusBarActivity
import kotlinx.android.synthetic.main.activity_calendar.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class CalendarActivity : TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.activity_calendar

    private val enableDays by lazy {
        intent.getIntExtra("enableDays",0)
    }

    override fun initClick() {
        calendarView.setOnClickDate {
            setResult(Activity.RESULT_OK,intent.putExtra("date",it.parserTime("yyyy-MM-dd")))
            finish()
        }
    }

    override fun initView() {
        title = "选择日期"
        val dates = mutableListOf<String>()
        (0 until enableDays).forEach {
            dates.add((System.currentTimeMillis()+it*24*60*60*1000).toTime("yyyyMMdd"))
        }
        calendarView.setOptionalDate(dates)
        tv_month.text = calendarView.date
        iv_next.onClick {
            calendarView.setNextMonth()
            tv_month.text = calendarView.date
        }
        iv_pre.onClick {
            calendarView.setLastMonth()
            tv_month.text = calendarView.date
        }
    }
}