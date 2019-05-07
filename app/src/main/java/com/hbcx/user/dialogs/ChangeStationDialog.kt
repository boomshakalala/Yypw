package com.hbcx.user.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.sinata.xldutils.utils.screenWidth
import com.hbcx.user.R
import kotlinx.android.synthetic.main.dialog_change_station.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.wrapContent

class ChangeStationDialog : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_FRAME, R.style.FadeDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_title.text = if (arguments!!.getBoolean("isStart", true)) "修改上车点" else "修改下车点"
        lv_station.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val adapter = com.hbcx.user.adapter.ChangeStationAdapter(data)
        lv_station.adapter = adapter
        val list = arguments!!.getSerializable("list") as ArrayList<com.hbcx.user.beans.BusStation>
        data.clear()
        data.addAll(list)
        adapter.notifyDataSetChanged()
        adapter.setOnItemClickListener { view, position ->
            if (checkIndex != -1)
                data[checkIndex].isChecked = false
            data[position].isChecked = true
            adapter.notifyDataSetChanged()
            checkIndex = position
        }
        btn_action.onClick {
            if (checkIndex == -1)
                return@onClick
            onBtnAction?.onBtnAction(checkIndex)
            dismiss()
        }
        iv_close.onClick {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        checkIndex = -1
    }

    private val data = arrayListOf<com.hbcx.user.beans.BusStation>()
    private var checkIndex = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_change_station, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog.window.setLayout((screenWidth() * 0.85).toInt(), wrapContent)
        dialog.window.setGravity(Gravity.CENTER)
        dialog.setCanceledOnTouchOutside(true)
    }

    private var onBtnAction: OnBtnAction? = null

    interface OnBtnAction {
        fun onBtnAction(position: Int)
    }

    fun setCallback(onBtnAction: (position: Int) -> Unit) {
        this.onBtnAction = object : OnBtnAction {
            override fun onBtnAction(position: Int) {
                onBtnAction(position)
            }
        }
    }
}