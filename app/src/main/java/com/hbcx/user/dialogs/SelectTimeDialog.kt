package com.hbcx.user.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.toTime
import com.hbcx.user.R
import com.hbcx.user.interfaces.OnDialogListener
import kotlinx.android.synthetic.main.dialog_select_time_layout.*
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent
import java.util.*

/**
 * 时间选择弹窗
 */
class SelectTimeDialog : DialogFragment() {
    private val reserveTime by lazy {
        arguments!!.getInt("reserveTime")
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
            inflater?.inflate(R.layout.dialog_select_time_layout, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_duration.gone()
        rg_type.gone()
        wv_day.setItems(getDay())
        //默认今天
        wv_day.setSeletion(1)
        getCurrent()

        wv_day.setOnWheelViewListener { selectedIndex, item ->
            if (isDestroy) {
                return@setOnWheelViewListener
            }
            when (selectedIndex) {
                0 -> {
                    wv_hour.setItems(arrayListOf())
                    wv_minute.setItems(arrayListOf())
                }
                1 -> getCurrent()
                else -> {
                    wv_hour.setItems(getHours())
                    wv_minute.setItems(getMinutes())
                    wv_hour.setSeletion(0)
                    wv_minute.setSeletion(0)
                }
            }
        }

        wv_hour.setOnWheelViewListener { selectedIndex, item ->
            if (isDestroy) {
                return@setOnWheelViewListener
            }
            if (wv_day.seletedIndex == 1 && selectedIndex == 0) {//今天 的现在
                val time = System.currentTimeMillis()+reserveTime*60*1000
                val minute = time.toTime("mm").toInt()
                wv_minute.setItems(getMinutes(minute))
                wv_minute.setSeletion(0)
            } else {
                wv_minute.setItems(getMinutes())
                wv_minute.setSeletion(0)
            }
        }

        tv_cancel.setOnClickListener {
            dismissAllowingStateLoss()
        }

        tv_sure.setOnClickListener {
            if (wv_day.seletedIndex == 0) {
                dialogListener?.onClick(wv_day.seletedIndex, "现在")
            } else {
                val data = wv_day.seletedItem + wv_hour.seletedItem + wv_minute.seletedItem
                val n = data.replace("点", ":").replace("分", "")
                dialogListener?.onClick(wv_day.seletedIndex, n)
            }
//
            dismissAllowingStateLoss()
        }

    }

    private fun getCurrent() {
        val time = System.currentTimeMillis()+reserveTime*60*1000
        val hour = time.toTime("HH").toInt()
        val minute = time.toTime("mm").toInt()
        //如果当前分钟数已经超过50。使用下一个小时
        if (minute >= 50) {
            wv_hour.setItems(getHours(hour + 1))
            wv_minute.setItems(getMinutes())
        } else {
            wv_hour.setItems(getHours(hour))
            wv_minute.setItems(getMinutes(minute))
        }
        wv_hour.setSeletion(0)
        wv_minute.setSeletion(0)
    }

    private fun getDay(): ArrayList<String> {
        return arrayListOf("现在", "今天", "明天", "后天")
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

    private var dialogListener: OnDialogListener? = null

    fun setDialogListener(l: (p: Int, s: String?) -> Unit) {
        dialogListener = object : OnDialogListener {
            override fun onClick(position: Int, data: String?) {
                l(position, data)
            }
        }
    }
}