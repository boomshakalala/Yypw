package cn.sinata.xldutils.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Window
import cn.sinata.xldutils.R
import cn.sinata.xldutils.rxutils.RequestHelper
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.widget.ProgressDialog
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.toast
import java.lang.Exception

/**
 * 基础activity，包含设置默认强制竖屏显示，广播方式实现关闭全部继承自该activity，并注册了关闭广播的子类
 *
 */
abstract class BaseActivity : AppCompatActivity(), AnkoLogger,RequestHelper {

    private lateinit var ACTION_CLOSE_ALL: String
    private val compositeDisposable = CompositeDisposable()
    //改用lazy初始，第一次使用时才会初始化
    private val dialog: ProgressDialog by lazy {
        ProgressDialog(this, R.style.Theme_ProgressDialog)
    }

    var isDestroy = false

    private val closeAllReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && TextUtils.equals(intent.action, ACTION_CLOSE_ALL)) {
                finish()
            }
        }
    }

    private var isCanSop = true

    fun setShowSOP(showSop: Boolean) {
        isCanSop = showSop
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            //设置竖屏显示
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            if (isCanSop) {
                //设置竖屏显示
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
        ACTION_CLOSE_ALL = "cn.sinata.base.%s.all.close".format(packageName)
        System.err.println(ACTION_CLOSE_ALL)
        if (isRegisterCloseBroadReceiver()) {
            registerReceiver(closeAllReceiver, IntentFilter(ACTION_CLOSE_ALL))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    protected open fun closeAll() {
        val intent = Intent(ACTION_CLOSE_ALL)
        sendBroadcast(intent)
    }

    /**
     * 是否注册关闭全部的广播
     */
    protected fun isRegisterCloseBroadReceiver(): Boolean {
        return true
    }

    override fun onDestroy() {
        isDestroy = true
        super.onDestroy()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }

        try {
            if (isRegisterCloseBroadReceiver()) {
                unregisterReceiver(closeAllReceiver)
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }

        dismissDialog()
    }

    fun showDialog(msg: String = "加载中...", canCancel: Boolean = true) {
        if (!canCancel) {
            dialog.setOnCancelListener {
                if (this.finishWhenCancelDialog()) {
                    finish()
                }
            }
        } else {
            //这里设置如果是可以取消的监听器置null了。可以自己在页面上重新设置想要的操作。这里不知道具体需求。
            dialog.setOnCancelListener(null)
        }
        dialog.setCanceledOnTouchOutside(canCancel)
        dialog.setMessage(msg)
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    open fun dismissDialog() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    /**
     * 是否在取消progressDialog的时候关闭页面
     */
    protected open fun finishWhenCancelDialog() = true

    override fun onRequestFinish() {
        dismissDialog()
    }

    override fun onBindHelper(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun errorToast(msg: String?) {
        toast(msg.toString())
    }
}