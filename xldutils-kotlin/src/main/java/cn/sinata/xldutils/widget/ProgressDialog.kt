package cn.sinata.xldutils.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import cn.sinata.xldutils.R
import org.jetbrains.anko.find

/**
 *
 */
class ProgressDialog : Dialog {
    private var mMessage: CharSequence = "加载中..."
    var mProgressBar :ProgressBar ? = null
    var mMessageView :TextView ? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, theme: Int) : super(context, theme)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.progress_dialog)
        mProgressBar = find<ProgressBar>(R.id.mProgressBar)
        mMessageView = find<TextView>(R.id.mMessageView)
        setMessage(mMessage)
    }

    fun setIndeterminate(indeterminate: Boolean) {
        if (mProgressBar != null) {
            mProgressBar?.isIndeterminate = indeterminate
        }
    }

    fun setMessage(message: CharSequence) {

        if (mMessageView == null) {
            mMessage = message
        } else {
            mMessageView?.text = message
        }
    }
}