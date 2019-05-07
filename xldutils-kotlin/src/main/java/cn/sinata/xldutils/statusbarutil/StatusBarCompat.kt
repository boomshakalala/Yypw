package cn.sinata.xldutils.statusbarutil

import android.app.Activity
import android.content.Context
import android.os.Build
import android.support.annotation.ColorInt
import android.view.View
import android.view.ViewGroup

/**
 * Utils for status bar
 * Created by qiu on 3/29/16.
 */
object StatusBarCompat {
    private val TAG_KEY_HAVE_SET_OFFSET = -123
    //Get alpha color
    internal fun calculateStatusBarColor(color: Int, alpha: Int): Int {
        val a = 1 - alpha / 255f
        var red = color shr 16 and 0xff
        var green = color shr 8 and 0xff
        var blue = color and 0xff
        red = (red * a + 0.5).toInt()
        green = (green * a + 0.5).toInt()
        blue = (blue * a + 0.5).toInt()
        return 0xff shl 24 or (red shl 16) or (green shl 8) or blue
    }

    /**
     * set statusBarColor
     * @param statusColor color
     * *
     * @param alpha       0 - 255
     */
    fun setStatusBarColor(activity: Activity, @ColorInt statusColor: Int, alpha: Int) {
        setStatusBarColor(activity, calculateStatusBarColor(statusColor, alpha))
    }

    fun setStatusBarColor(activity: Activity, @ColorInt statusColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarCompatLollipop.setStatusBarColor(activity, statusColor)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            StatusBarCompatKitKat.setStatusBarColor(activity, statusColor)
        }
    }

    /**
     * change to full screen mode
     * @param hideStatusBarBackground hide status bar alpha Background when SDK > 21, true if hide it
     */
    @JvmOverloads fun translucentStatusBar(activity: Activity, hideStatusBarBackground: Boolean = false,needOffsetView: View? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarCompatLollipop.translucentStatusBar(activity, hideStatusBarBackground)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            StatusBarCompatKitKat.translucentStatusBar(activity)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            if (needOffsetView != null) {
                val haveSetOffset = needOffsetView.getTag(TAG_KEY_HAVE_SET_OFFSET)
                if (haveSetOffset != null && haveSetOffset as Boolean) {
                    return
                }
                val layoutParams = needOffsetView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + getStatusBarHeight(activity),
                        layoutParams.rightMargin, layoutParams.bottomMargin)
                needOffsetView.setTag(TAG_KEY_HAVE_SET_OFFSET, true)
            }
        }
    }

    @JvmOverloads fun translucentStatusBarForToolBar(activity: Activity, hideStatusBarBackground: Boolean = false,needOffsetView: View? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarCompatLollipop.translucentStatusBar(activity, hideStatusBarBackground)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            StatusBarCompatKitKat.translucentStatusBar(activity)
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
            if (needOffsetView != null) {
                val haveSetOffset = needOffsetView.getTag(TAG_KEY_HAVE_SET_OFFSET)
                if (haveSetOffset != null && haveSetOffset as Boolean) {
                    return
                }
                val layoutParams = needOffsetView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + getStatusBarHeight(activity),
                        layoutParams.rightMargin, layoutParams.bottomMargin)
                needOffsetView.setTag(TAG_KEY_HAVE_SET_OFFSET, true)
            }
        }
    }

    /**
     * 获取状态栏高度
     *
     * @param context context
     * @return 状态栏高度
     */
    private fun getStatusBarHeight(context: Context): Int {
        // 获得状态栏高度
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(resourceId)
    }
}
