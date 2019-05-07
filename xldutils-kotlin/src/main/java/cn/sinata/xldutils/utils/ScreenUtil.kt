package cn.sinata.xldutils.utils

import android.content.Context
import android.support.v4.app.Fragment
import android.view.View
import org.jetbrains.anko.displayMetrics
import org.jetbrains.anko.windowManager

/**
 * 屏幕尺寸工具
 */
fun Context.screenWidth():Int{
    val dm = this.displayMetrics
    this.windowManager.defaultDisplay.getMetrics(dm)
    return dm.widthPixels
}

fun Fragment.screenWidth(): Int = activity?.screenWidth()?:0

fun View.screenWidth():Int = context.screenWidth()

fun Context.screenHeight():Int{
    val dm =this. displayMetrics
    this.windowManager.defaultDisplay.getMetrics(dm)
    return dm.heightPixels
}

fun Fragment.screenHeight():Int = context?.screenHeight()?:0

fun View.screenHeight():Int = context.screenHeight()