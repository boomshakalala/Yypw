package com.hbcx.user.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import com.hbcx.user.R



object StatusBarUtil {
    private var window: Window? = null

    /**
     * @param window 深色状态栏字体模式,小米和魅族公开了各自的实现方法，支持底层Android4.4以上的版本。而Android官方在6.0版本才有了深色状态栏字体API。
     * 所以Android4.4以上系统版本可以修改状态栏颜色，但是只有小米的MIUI、魅族的Flyme和Android6.0以上系统可以把状态栏文字和图标换成深色。
     */
    //初始化状态栏为全透明
    fun initStatus(window: Window) {
        StatusBarUtil.window = window
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.BLACK
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                MIUISetStatusBarLightMode(window, true)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.BLACK
                FlymeSetStatusBarLightMode(window, true)
                MIUISetStatusBarLightMode(window, true)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                MIUISetStatusBarLightMode(window, true)
                FlymeSetStatusBarLightMode(window, true)
            }
        }
    }

    //初始化状态栏为全透明
    fun initStatus2(window: Window) {
        StatusBarUtil.window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.BLACK
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.BLACK
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        FlymeSetStatusBarLightMode(window, false)
        MIUISetStatusBarLightMode(window, false)
    }

    fun initBarHeight(context: Context, mStatusBar: View?, mNavigationBar: View?) {
        if (mStatusBar != null) {
            initStatusBarHeight(context, mStatusBar)
        }
        if (mNavigationBar != null) {
            initNavigationBarHeight(context, mNavigationBar)
        }
    }

    //初始化状态栏、（导航栏）的高度，需要用到沉浸式状态栏的页面 需在该页面顶部添加一个view来填充状态栏
    private fun initStatusBarHeight(context: Context, mStatusBar: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    getStatusBarHeight(context))
            mStatusBar.layoutParams = params
        }
        initStatusBarColor(context, mStatusBar)
    }

    private fun initStatusBarColor(context: Context, statusBar: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            statusBar.setBackgroundColor(context.resources.getColor(R.color.white))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (FlymeSetStatusBarLightMode(window, true) || MIUISetStatusBarLightMode(window, true)) {
                statusBar.setBackgroundColor(context.resources.getColor(R.color.white))
            } else {
                statusBar.setBackgroundColor(context.resources.getColor(R.color.white))
            }
        }
    }

    //初始化状态栏、（导航栏）的高度，需要用到沉浸式状态栏的页面 需在该页面顶部添加一个view来填充状态栏
    private fun initNavigationBarHeight(context: Context, mNavigationbar: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val params = mNavigationbar.layoutParams
            params.height = getNavigationHeight(context)
            mNavigationbar.layoutParams = params
        }
    }

    //初始化状态栏、（导航栏）的高度，需要用到沉浸式状态栏的页面 内容布局不是 LinearLayout 时,设置padding top
    private fun initStatusBarHeight2(context: Context, mStatusbar: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mStatusbar.setPadding(mStatusbar.paddingLeft, getStatusBarHeight(context) + mStatusbar.paddingTop,
                    mStatusbar.paddingRight, mStatusbar.paddingBottom)
        }
    }

    // 获得状态栏高度
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(resourceId)
    }

    // 获得导航栏高度
    fun getNavigationHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (navigationBarExist(context)) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    private fun navigationBarExist(context: Context): Boolean {
        val id = context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && navigationBarExist2(context as Activity)
    }

    private fun navigationBarExist2(activity: Activity): Boolean {
        val windowManager = activity.windowManager
        val d = windowManager.defaultDisplay

        val realDisplayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealMetrics(realDisplayMetrics)
        }

        val realHeight = realDisplayMetrics.heightPixels
        val realWidth = realDisplayMetrics.widthPixels

        val displayMetrics = DisplayMetrics()
        d.getMetrics(displayMetrics)

        val displayHeight = displayMetrics.heightPixels
        val displayWidth = displayMetrics.widthPixels

        return realWidth - displayWidth > 0 || realHeight - displayHeight > 0
    }


    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    fun FlymeSetStatusBarLightMode(window: Window?, dark: Boolean): Boolean {
        var result = false
        if (window != null) {
            try {
                val lp = window.attributes
                val darkFlag = WindowManager.LayoutParams::class.java
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java
                        .getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                if (dark) {
                    value = value or bit
                } else {
                    value = value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                window.attributes = lp
                result = true
            } catch (e: Exception) {

            }

        }
        return result
    }


    /**
     * 设置状态栏字体图标为深色，需要MIUIV6以上
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    fun MIUISetStatusBarLightMode(window: Window?, dark: Boolean): Boolean {
        var result = false
        if (window != null) {
            val clazz = window.javaClass
            try {
                var darkModeFlag = 0
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag)//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag)//清除黑色字体
                }
                result = true
            } catch (e: Exception) {

            }

        }
        return result
    }

    /**
     * 设置状态栏颜色 *
     *
     * @param activity 需要设置的activity
     * 需要根布局为LinearLayout
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    fun setStatus(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置状态栏透明
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            // 生成一个状态栏大小的矩形
            val statusView = createStatusView(activity)
            // 添加 statusView 到布局中
            val decorView = activity.window.decorView as ViewGroup
            decorView.addView(statusView)
            // 设置根布局的参数
            val rootView = (activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
            initStatusBarColor(activity, rootView)
            rootView.fitsSystemWindows = true
            rootView.clipToPadding = true
        }
    }

    /**
     * 生成一个和状态栏大小相同的矩形条 * * @param activity 需要设置的activity *
     *
     * @return 状态栏矩形条
     */
    private fun createStatusView(activity: Activity): View {
        // 绘制一个和状态栏一样高的矩形
        val statusView = View(activity)
        //        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        //                getStatusBarHeight(activity));
        //        statusView.setLayoutParams(params);
        //        statusView.setBackgroundColor(color);
        initStatusBarHeight(activity, statusView)

        return statusView
    }
}
