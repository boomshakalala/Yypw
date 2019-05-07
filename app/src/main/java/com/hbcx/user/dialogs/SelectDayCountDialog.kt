package com.hbcx.user.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.sinata.xldutils.gone
import com.hbcx.user.R
import com.hbcx.user.interfaces.OnDialogListener
import kotlinx.android.synthetic.main.dialog_select_time_layout.*
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent

/**
 * 日子数选择弹窗
 */
class SelectDayCountDialog : DialogFragment() {
    private val data = arrayListOf<String>()
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
        val day = arguments?.getInt("day",0)?:0
        for (i in 1..day){
            data.add("${i}天")
        }
        tv_duration.gone()
        rg_type.gone()
        wv_hour.gone()
        wv_minute.gone()
        wv_day.setItems(data)
        //默认今天
        wv_day.setSeletion(1)
        tv_cancel.setOnClickListener {
            dismissAllowingStateLoss()
        }

        tv_sure.setOnClickListener {
                dialogListener?.onClick(wv_day.seletedIndex, wv_day.seletedItem)
            dismissAllowingStateLoss()
        }
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