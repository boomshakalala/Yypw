package cn.sinata.xldutils.statusbarutil

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Build
import android.support.v4.view.ViewCompat
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout

/**
 * After kitkat add fake status bar
 * Created by qiu on 8/27/16.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
internal object StatusBarCompatKitKat {

    private val TAG_FAKE_STATUS_BAR_VIEW = "statusBarView"
    private val TAG_MARGIN_ADDED = "marginAdded"

    /**
     * return statusBar's Height in pixels
     */
    private fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resId > 0) {
            result = context.resources.getDimensionPixelOffset(resId)
        }
        return result
    }

    /**
     * 1. Add fake statusBarView.
     * 2. set tag to statusBarView.
     */
    private fun addFakeStatusBarView(activity: Activity, statusBarColor: Int, statusBarHeight: Int): View {
        val window = activity.window
        val mDecorView = window.decorView as ViewGroup

        val mStatusBarView = View(activity)
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight)
        layoutParams.gravity = Gravity.TOP
        mStatusBarView.layoutParams = layoutParams
        mStatusBarView.setBackgroundColor(statusBarColor)
        mStatusBarView.tag = TAG_FAKE_STATUS_BAR_VIEW

        mDecorView.addView(mStatusBarView)
        return mStatusBarView
    }

    /**
     * use reserved order to remove is more quickly.
     */
    private fun removeFakeStatusBarViewIfExist(activity: Activity) {
        val window = activity.window
        val mDecorView = window.decorView as ViewGroup
        val fakeView = mDecorView.findViewWithTag<View>(TAG_FAKE_STATUS_BAR_VIEW)
        if (fakeView != null) {
            mDecorView.removeView(fakeView)
        }
    }

    /**
     * add marginTop to simulate set FitsSystemWindow true
     */
    private fun addMarginTopToContentChild(mContentChild: View?, statusBarHeight: Int) {
        if (mContentChild == null) {
            return
        }
        if (TAG_MARGIN_ADDED != mContentChild.tag) {
            val lp = mContentChild.layoutParams as FrameLayout.LayoutParams
            lp.topMargin += statusBarHeight
            mContentChild.layoutParams = lp
            mContentChild.tag = TAG_MARGIN_ADDED
        }
    }

    /**
     * remove marginTop to simulate set FitsSystemWindow false
     */
    private fun removeMarginTopOfContentChild(mContentChild: View?, statusBarHeight: Int) {
        if (mContentChild == null) {
            return
        }
        if (TAG_MARGIN_ADDED == mContentChild.tag) {
            val lp = mContentChild.layoutParams as FrameLayout.LayoutParams
            lp.topMargin -= statusBarHeight
            mContentChild.layoutParams = lp
            mContentChild.tag = null
        }
    }

    /**
     * set StatusBarColor

     * 1. set Window Flag : WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
     * 2. removeFakeStatusBarViewIfExist
     * 3. addFakeStatusBarView
     * 4. addMarginTopToContentChild
     * 5. cancel ContentChild's fitsSystemWindow
     */
    fun setStatusBarColor(activity: Activity, statusColor: Int) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        val mContentView = window.findViewById(Window.ID_ANDROID_CONTENT) as ViewGroup
        val mContentChild = mContentView.getChildAt(0)
        val statusBarHeight = getStatusBarHeight(activity)

        removeFakeStatusBarViewIfExist(activity)
        addFakeStatusBarView(activity, statusColor, statusBarHeight)
        addMarginTopToContentChild(mContentChild, statusBarHeight)

        if (mContentChild != null) {
            ViewCompat.setFitsSystemWindows(mContentChild, false)
        }
    }

    /**
     * translucentStatusBar

     * 1. set Window Flag : WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
     * 2. removeFakeStatusBarViewIfExist
     * 3. removeMarginTopOfContentChild
     * 4. cancel ContentChild's fitsSystemWindow
     */
    fun translucentStatusBar(activity: Activity) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        val mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT) as ViewGroup
        val mContentChild = mContentView.getChildAt(0)

        removeFakeStatusBarViewIfExist(activity)
        removeMarginTopOfContentChild(mContentChild, getStatusBarHeight(activity))
        if (mContentChild != null) {
            ViewCompat.setFitsSystemWindows(mContentChild, false)
        }
    }
}
