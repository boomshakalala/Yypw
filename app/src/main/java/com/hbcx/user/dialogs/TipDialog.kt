package com.hbcx.user.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import cn.sinata.xldutils.utils.screenWidth
import com.hbcx.user.R
import com.hbcx.user.interfaces.OnDialogListener
import kotlinx.android.synthetic.main.dialog_tip_layout.*
import org.jetbrains.anko.wrapContent

/**
 * 提示弹窗
 */
class TipDialog : DialogFragment() {

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
            inflater.inflate(R.layout.dialog_tip_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val msg = if (arguments != null) {
            arguments!!.getString("msg")
        } else {
            ""
        }
        tv_msg.text = msg

        val cancel = if (arguments != null) {
            arguments!!.getString("cancel")
        } else {
            "取消"
        }
        val ok = if (arguments != null&& arguments!!.getString("ok")!=null) {
            arguments!!.getString("ok")
        } else {
            "确定"
        }
        tv_left.text = cancel

        tv_left.visibility = if (arguments!=null&&!arguments!!.getBoolean("notice",false)){
            dialog.setCanceledOnTouchOutside(true)
            tv_right.text = ok
            View.VISIBLE
        }else{
            dialog.setCanceledOnTouchOutside(false)
            tv_right.text = "确定"
            View.GONE
        }

        tv_left.setOnClickListener {
            dialogCancelListener?.onClick(0,"")
            dismissAllowingStateLoss()
        }
        tv_right.setOnClickListener {
            dialogListener?.onClick(1,"")
            dismissAllowingStateLoss()
        }

    }

    private var dialogListener: OnDialogListener? = null
    private var dialogCancelListener: OnDialogListener? = null

    fun setDialogListener(l: (p: Int, s: String?) -> Unit) {
        dialogListener = object : OnDialogListener {
            override fun onClick(position: Int, data: String?) {
                l(position, data)
            }
        }
    }
    fun setCancelDialogListener(l: (p: Int, s: String?) -> Unit) {
        dialogCancelListener = object : OnDialogListener {
            override fun onClick(position: Int, data: String?) {
                l(position, data)
            }
        }
    }
}