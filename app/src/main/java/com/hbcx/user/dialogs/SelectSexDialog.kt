package com.hbcx.user.dialogs

import android.app.DialogFragment
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hbcx.user.R
import com.hbcx.user.interfaces.OnDialogListener
import kotlinx.android.synthetic.main.dialog_select_sex_layout.*
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent

class SelectSexDialog : DialogFragment() {
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
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.dialog_select_sex_layout, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wv_sex.setItems(arrayListOf("男","女"))
        wv_sex.setSeletion(0)
        tv_cancel.setOnClickListener {
            dismissAllowingStateLoss()
        }
        tv_sure.setOnClickListener {
            dialogListener?.onClick(wv_sex.seletedIndex,wv_sex.seletedItem)
            dismissAllowingStateLoss()
        }
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