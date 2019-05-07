package cn.sinata.xldutils.utils

import android.app.Activity
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.text.TextUtils

/**
 * alert弹窗
 */
fun Activity.alertDialog(title:String="请注意", message: String, outCancel:Boolean = true, positive:String = "确定", negative:String? = null,
                         pListener: DialogInterface.OnClickListener? = null, nListener:DialogInterface.OnClickListener? = null) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setCancelable(outCancel)
    if (!TextUtils.isEmpty(positive)) {
        builder.setPositiveButton(positive, pListener)
    }
    if (!TextUtils.isEmpty(negative)) {
        builder.setNegativeButton(negative, nListener)
    }
    builder.create().show()
}