package com.hbcx.user.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.parserTime
import cn.sinata.xldutils.utils.toTime
import cn.sinata.xldutils.utils.toWeek
import com.hbcx.user.R
import com.hbcx.user.interfaces.TimeSelectListener
import kotlinx.android.synthetic.main.dialog_select_time_layout.*
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.wrapContent
import java.util.*

/**
 * 时间选择弹窗
 */
class SelectRentTimeDialog : DialogFragment() {
    private var startDayIndex = 0
    private var startHourIndex = 0
    private var startMinIndex = 0
    private var endDayIndex = 2 //默认2天
    private var endHourIndex = 0
    private var endMinIndex = 0
    private var type = 0 //0:开始 1：结束时间 2:包车

    private var startTime = 0L
    private var endTime = 0L

    private val calendar = Calendar.getInstance()

    private val startDays: ArrayList<String> = arrayListOf()
    private val endDays: ArrayList<String> = arrayListOf()

    private val maxDay by lazy {
        arguments!!.getInt("maxDay", 0)
    }
    private val rentHour by lazy {
        //取还时间在此小时之内则为小时租车
        arguments!!.getInt("rentHour", 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_FRAME, R.style.Dialog)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog.window.setLayout(matchParent, wrapContent)
        dialog.window.setGravity(Gravity.BOTTOM)
        dialog.setCanceledOnTouchOutside(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.dialog_select_time_layout, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        type = arguments!!.getInt("type", 0)
        if (type == 2) { //包车
            tv_duration.gone()
            rg_type.gone()
        }
        startDays.addAll(getDay(true))
        endDays.addAll(getDay(false))
        wv_day.setItems(if (type == 1) endDays.subList(startDayIndex, startDayIndex + maxDay) else startDays)
        wv_day.setSeletion(if (type == 1) endDayIndex else 0) //设置初始天数
        getCurrent()
        wv_hour.setSeletion(0)
        wv_minute.setSeletion(0)
        endHourIndex = getHours().indexOf(wv_hour.seletedItem)
        endMinIndex = getMinutes().indexOf(wv_minute.seletedItem)
        val startStr = "${startDays[0].substring(0, 5)} ${wv_hour.seletedItem.substring(0, 2)}:${wv_minute.seletedItem.substring(0, 2)}"
        startTime = "${calendar.get(Calendar.YEAR)}-$startStr".parserTime("yyyy-MM-dd HH:mm")
        endTime = startTime + 24 * 60 * 60 * 2 * 1000
        rb_rent_time.text = String.format("取车时间\n%s", startStr)
        rb_return_time.text = String.format("还车时间\n%s", endTime.toTime("MM-dd HH:mm"))
        if (type == 1) {
            rg_type.check(R.id.rb_return_time)
        }
        wv_day.setOnWheelViewListener { selectedIndex, item ->
            if (isDestroy) {
                return@setOnWheelViewListener
            }
            if (type == 1) {
                endHourIndex = 0
                endMinIndex = 0
            } else {
                startHourIndex = 0
                startMinIndex = 0
            }
            when (selectedIndex) {
                0 -> {
                    getCurrent()
                    wv_hour.setSeletion(0)
                    wv_minute.setSeletion(0)
                }
                else -> {
                    wv_hour.setItems(getHours())
                    wv_minute.setItems(getMinutes())
                    wv_hour.setSeletion(0)
                    wv_minute.setSeletion(0)
                }
            }
            if (type == 1) { //还车时间选择
                val endStr = "${item.substring(0, 5)} ${wv_hour.seletedItem.substring(0, 2)}:${wv_minute.seletedItem.substring(0, 2)}"
                val time = "${calendar.get(Calendar.YEAR)}-$endStr".parserTime("yyyy-MM-dd HH:mm")
                endTime = if (time < System.currentTimeMillis()) {
                    "${calendar.get(Calendar.YEAR) + 1}-$endStr".parserTime("yyyy-MM-dd HH:mm") //翻年
                } else
                    time
                rb_return_time.text = String.format("还车时间\n%s", endTime.toTime("MM-dd HH:mm"))
                endDayIndex = selectedIndex
            } else { //取车时间
                val startStr = "${item.substring(0, 5)} ${wv_hour.seletedItem.substring(0, 2)}:${wv_minute.seletedItem.substring(0, 2)}"
                val time = "${calendar.get(Calendar.YEAR)}-$startStr".parserTime("yyyy-MM-dd HH:mm")
                startTime = if (time < System.currentTimeMillis()) {
                    "${calendar.get(Calendar.YEAR) + 1}-$startStr".parserTime("yyyy-MM-dd HH:mm") //翻年
                } else
                    time

                rb_rent_time.text = String.format("取车时间\n%s", startTime.toTime("MM-dd HH:mm"))
                startDayIndex = selectedIndex

                //刷新checkbutton
                val endStr = "${(wv_day.seletedItem+2).substring(0, 5)} ${wv_hour.seletedItem.substring(0, 2)}:${wv_minute.seletedItem.substring(0, 2)}"
                val time1 = "${calendar.get(Calendar.YEAR)}-$endStr".parserTime("yyyy-MM-dd HH:mm")
                endTime = if (time1 < System.currentTimeMillis()) {
                    "${calendar.get(Calendar.YEAR) + 1}-$endStr".parserTime("yyyy-MM-dd HH:mm") //翻年
                } else
                    time1
                rb_return_time.text = String.format("还车时间\n%s", endTime.toTime("MM-dd HH:mm"))
                endDayIndex = wv_day.seletedIndex+2
                val offset = endTime - startTime
                when {
                    offset < 0 -> tv_duration.text = "0天"
                    offset < rentHour * 60 * 60 * 1000 -> //按小时租车
                        tv_duration.text = "${if (offset % (60 * 60 * 1000) > 0) offset / (60 * 60 * 1000) + 1 else offset / (60 * 60 * 1000)}小时"
                    else -> tv_duration.text = "${if (offset % (24 * 60 * 60 * 1000) > 0) offset / (24 * 60 * 60 * 1000) + 1 else offset / (24 * 60 * 60 * 1000)}天"
                }
            }
            val offset = endTime - startTime
            when {
                offset < 0 -> tv_duration.text = "0天"
                offset < rentHour * 60 * 60 * 1000 -> //按小时租车
                    tv_duration.text = "${if (offset % (60 * 60 * 1000) > 0) offset / (60 * 60 * 1000) + 1 else offset / (60 * 60 * 1000)}小时"
                else -> tv_duration.text = "${if (offset % (24 * 60 * 60 * 1000) > 0) offset / (24 * 60 * 60 * 1000) + 1 else offset / (24 * 60 * 60 * 1000)}天"
            }
        }

        wv_hour.setOnWheelViewListener { selectedIndex, item ->
            if (isDestroy) {
                return@setOnWheelViewListener
            }
            if (type == 1) {
                endMinIndex = 0
            } else {
                startMinIndex = 0
            }
            val time = System.currentTimeMillis()
            val hour = time.toTime("HH").toInt()
            val minute = time.toTime("mm").toInt()
            if (type == 2) {
                if (wv_day.seletedIndex == 0 && selectedIndex == 0) {//今天 的现在
                    wv_minute.setItems(getMinutes(minute))
                    wv_minute.setSeletion(0)
                } else {
                    wv_minute.setItems(getMinutes())
                    wv_minute.setSeletion(0)
                }
            } else {
                if (wv_day.seletedIndex == 0 && selectedIndex == 0 && hour < 12) {//今天 的现在
                    wv_minute.setItems(getMinutes(minute))
                    wv_minute.setSeletion(0)
                } else {
                    wv_minute.setItems(getMinutes())
                    wv_minute.setSeletion(0)
                }
            }

            if (type == 1) {
                val endStr = "${wv_day.seletedItem.substring(0, 5)} ${item.substring(0, 2)}:${wv_minute.seletedItem.substring(0, 2)}"
                val time = "${calendar.get(Calendar.YEAR)}-$endStr".parserTime("yyyy-MM-dd HH:mm")
                endTime = if (time < System.currentTimeMillis()) {
                    "${calendar.get(Calendar.YEAR) + 1}-$endStr".parserTime("yyyy-MM-dd HH:mm") //翻年
                } else
                    time
                rb_return_time.text = String.format("还车时间\n%s", endTime.toTime("MM-dd HH:mm"))
                endHourIndex = selectedIndex
            } else {
                val startStr = "${wv_day.seletedItem.substring(0, 5)} ${item.substring(0, 2)}:${wv_minute.seletedItem.substring(0, 2)}"
                val time = "${calendar.get(Calendar.YEAR)}-$startStr".parserTime("yyyy-MM-dd HH:mm")
                startTime = if (time < System.currentTimeMillis()) {
                    "${calendar.get(Calendar.YEAR) + 1}-$startStr".parserTime("yyyy-MM-dd HH:mm") //翻年
                } else
                    time
                rb_rent_time.text = String.format("取车时间\n%s", startTime.toTime("MM-dd HH:mm"))
                startHourIndex = selectedIndex
            }
            val offset = endTime - startTime
            when {
                offset < 0 -> tv_duration.text = "0天"
                offset < rentHour * 60 * 60 * 1000 -> //按小时租车
                    tv_duration.text = "${if (offset % (60 * 60 * 1000) > 0) offset / (60 * 60 * 1000) + 1 else offset / (60 * 60 * 1000)}小时"
                else -> tv_duration.text = "${if (offset % (24 * 60 * 60 * 1000) > 0) offset / (24 * 60 * 60 * 1000) + 1 else offset / (24 * 60 * 60 * 1000)}天"
            }
        }

        wv_minute.setOnWheelViewListener { selectedIndex, item ->
            if (type == 1) {
                val endStr = "${wv_day.seletedItem.substring(0, 5)} ${wv_hour.seletedItem.substring(0, 2)}:${item.substring(0, 2)}"
                val time = "${calendar.get(Calendar.YEAR)}-$endStr".parserTime("yyyy-MM-dd HH:mm")
                endTime = if ( time < System.currentTimeMillis()) {
                    "${calendar.get(Calendar.YEAR) + 1}-$endStr".parserTime("yyyy-MM-dd HH:mm") //翻年
                } else
                    time
                rb_return_time.text = String.format("还车时间\n%s", endTime.toTime("MM-dd HH:mm"))
                endMinIndex = selectedIndex
            } else {
                val startStr = "${wv_day.seletedItem.substring(0, 5)} ${wv_hour.seletedItem.substring(0, 2)}:${item.substring(0, 2)}"
                val time = "${calendar.get(Calendar.YEAR)}-$startStr".parserTime("yyyy-MM-dd HH:mm")
                startTime = if ( time < System.currentTimeMillis()) {
                    "${calendar.get(Calendar.YEAR) + 1}-$startStr".parserTime("yyyy-MM-dd HH:mm") //翻年
                } else
                    time
                rb_rent_time.text = String.format("取车时间\n%s", startTime.toTime("MM-dd HH:mm"))
                startMinIndex = selectedIndex
            }
            val offset = endTime - startTime
            when {
                offset < 0 -> tv_duration.text = "0天"
                offset < rentHour * 60 * 60 * 1000 -> //按小时租车
                    tv_duration.text = "${if (offset % (60 * 60 * 1000) > 0) offset / (60 * 60 * 1000) + 1 else offset / (60 * 60 * 1000)}小时"
                else -> tv_duration.text = "${if (offset % (24 * 60 * 60 * 1000) > 0) offset / (24 * 60 * 60 * 1000) + 1 else offset / (24 * 60 * 60 * 1000)}天"
            }
        }

        tv_cancel.setOnClickListener {
            dismissAllowingStateLoss()
        }

        tv_sure.setOnClickListener {
            if (type == 2) {
                dialogListener?.onClick(startTime, startDays[startDayIndex] + startTime.toTime(" HH:mm"), endTime, "", "")
            } else {
                if (endTime <= startTime) {
                    toast("结束时间不能小于开始时间")
                    return@setOnClickListener
                } else {
                    dialogListener?.onClick(startTime, startDays[startDayIndex] + startTime.toTime(" HH:mm"), endTime, endDays[endDayIndex] + endTime.toTime(" HH:mm"), tv_duration.text.toString())
                }
            }
            dismissAllowingStateLoss()
        }

        //切换开始和结束时间
        rg_type.setOnCheckedChangeListener { _, i ->
            if (i == R.id.rb_rent_time) {
                type = 0
                wv_day.setItems(startDays)
                wv_day.setSeletion(startDayIndex)
                if (startDayIndex == 0) {
                    getCurrent()
                } else {
                    wv_hour.setItems(getHours())
                    wv_minute.setItems(getMinutes())
                }
                wv_hour.setSeletion(startHourIndex)
                wv_minute.setSeletion(startMinIndex)
            } else {
                type = 1
                wv_day.setItems(endDays.subList(startDayIndex, startDayIndex + maxDay))
                wv_day.setSeletion(endDayIndex)
                if (endDayIndex == 0) {
                    getCurrent()
                } else {
                    wv_hour.setItems(getHours())
                    wv_minute.setItems(getMinutes())
                }
                wv_hour.setSeletion(endHourIndex)
                wv_minute.setSeletion(endMinIndex)

                //刷新checkbutton
                val endStr = "${wv_day.seletedItem.substring(0, 5)} ${wv_hour.seletedItem.substring(0, 2)}:${wv_minute.seletedItem.substring(0, 2)}"
                val time = "${calendar.get(Calendar.YEAR)}-$endStr".parserTime("yyyy-MM-dd HH:mm")
                endTime = if (time < System.currentTimeMillis()) {
                    "${calendar.get(Calendar.YEAR) + 1}-$endStr".parserTime("yyyy-MM-dd HH:mm") //翻年
                } else
                    time
                rb_return_time.text = String.format("还车时间\n%s", endTime.toTime("MM-dd HH:mm"))
                endDayIndex = wv_day.seletedIndex
                val offset = endTime - startTime
                when {
                    offset < 0 -> tv_duration.text = "0天"
                    offset < rentHour * 60 * 60 * 1000 -> //按小时租车
                        tv_duration.text = "${if (offset % (60 * 60 * 1000) > 0) offset / (60 * 60 * 1000) + 1 else offset / (60 * 60 * 1000)}小时"
                    else -> tv_duration.text = "${if (offset % (24 * 60 * 60 * 1000) > 0) offset / (24 * 60 * 60 * 1000) + 1 else offset / (24 * 60 * 60 * 1000)}天"
                }
            }
        }
    }

    private fun getCurrent() {
        val time = System.currentTimeMillis()
        val hour = time.toTime("HH").toInt()
        val minute = time.toTime("mm").toInt()
        if (type == 2) {
            if (minute >= 50) {
                wv_hour.setItems(getHours(hour + 1))
                wv_minute.setItems(getMinutes())
            } else {
                wv_hour.setItems(getHours(hour))
                wv_minute.setItems(getMinutes(minute))
            }
        } else {
            if (hour > 12) { //超过12点 从10点开始
                wv_hour.setItems(getHours(10))
                wv_minute.setItems(getMinutes())
            } else { //如果没超过12点，就在当前时间加一个小时
                //如果当前分钟数已经超过50。使用下一个小时
                if (minute >= 50) {
                    wv_hour.setItems(getHours(hour + 2))
                    wv_minute.setItems(getMinutes())
                } else {
                    wv_hour.setItems(getHours(hour + 1))
                    wv_minute.setItems(getMinutes(minute))
                }
            }
        }
    }

    private fun getDay(isStart: Boolean): ArrayList<String> {
        val days = ArrayList<String>()
//        days.add("今天")
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val currentTimeMillis = System.currentTimeMillis()
        if (type == 2) {
            if (!isStart) return days
            (0 until 30L).forEach {
                val t = currentTimeMillis + (it * 24 * 60 * 60 * 1000)
                Log.e("time","count"+it+" time:"+t+" time" + t.toTime("yyyy-MM-dd"))
                days.add(t.toTime("MM-dd ") + t.toWeek())
            }
        } else {
            val start:Long = if (hour > 12) 1 else 0
            (start until start + if (isStart) 30 else 30 + maxDay).forEach {
                val t = currentTimeMillis + (it * 24 * 60 * 60 * 1000)
                days.add(t.toTime("MM-dd ") + t.toWeek())
            }
        }
        return days
    }

    private fun getHours(hour: Int = 0): ArrayList<String> {
        val hours = ArrayList<String>()
        (hour until 24).forEach {
            hours.add(String.format("%02d点", it))
        }
        return hours
    }

    private fun getMinutes(m: Int = 0): ArrayList<String> {
        val minutes = ArrayList<String>()
        (0..5).forEach {
            if (it * 10 >= m) {
                minutes.add(String.format("%02d分", it * 10))
            }
        }
        return minutes
    }

    private var isDestroy = false
    override fun onDestroyView() {
        isDestroy = true
        super.onDestroyView()
    }

    private var dialogListener: TimeSelectListener? = null

    fun setDialogListener(l: (p0: Long, s0: String?, p1: Long, s1: String?, s2: String) -> Unit) {
        dialogListener = object : TimeSelectListener {
            override fun onClick(position: Long, data: String?, position1: Long, data1: String?, data2: String) {
                l(position, data, position1, data1, data2)
            }
        }
    }
}