package com.hbcx.user.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import cn.sinata.xldutils.utils.screenWidth
import com.hbcx.user.R
import kotlinx.android.synthetic.main.dialog_label_layout.*
import kotlinx.android.synthetic.main.item_label.view.*
import org.jetbrains.anko.wrapContent

/**
 * 标签提示弹窗
 */
class LabelListDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_FRAME, R.style.FadeDialog)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog.window.setLayout((screenWidth()*0.75).toInt(), wrapContent)
        dialog.window.setGravity(Gravity.CENTER)
        dialog.setCanceledOnTouchOutside(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.dialog_label_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val labels = arguments!!.getSerializable("data") as ArrayList<com.hbcx.user.beans.CarLabel>
        tv_name.text = arguments!!.getString("title")
        labels.forEach {
            val view = LayoutInflater.from(context).inflate(R.layout.item_label, null)
            view.tv_name.text = it.name
            view.tv_describe.text = it.remark
            ll_detail.addView(view)
        }
        iv_close.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }
}