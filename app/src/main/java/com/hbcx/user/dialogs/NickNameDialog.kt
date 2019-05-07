package com.hbcx.user.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.sinata.xldutils.utils.SPUtils

import cn.sinata.xldutils.utils.screenWidth
import com.hbcx.user.R
import com.hbcx.user.interfaces.OnDialogListener
import com.hbcx.user.utils.Const
import kotlinx.android.synthetic.main.dialog_nickname.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.wrapContent

/**
 * 提示弹窗
 */
class NickNameDialog : DialogFragment() {

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
            inflater.inflate(R.layout.dialog_nickname, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        et_name.setText(SPUtils.instance().getString(Const.User.USER_NAME))
        iv_close.setOnClickListener {
            dismissAllowingStateLoss()
        }
        tv_ok.onClick {
            dialogListener?.onClick(0,et_name.text.trim().toString())
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