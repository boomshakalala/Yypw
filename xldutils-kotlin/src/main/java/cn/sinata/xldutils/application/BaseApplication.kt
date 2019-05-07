package cn.sinata.xldutils.application

import android.app.Application
import cn.sinata.xldutils.xldUtils

/**
 *
 */
abstract class BaseApplication : Application() {
    var sysTime:Long = 0

    override fun onCreate() {
        super.onCreate()
        xldUtils.init(this)
    }

    abstract fun getSPName():String


}