package cn.sinata.xldutils.rxutils

import io.reactivex.disposables.Disposable



/**
 *
 */
interface RequestHelper {
    fun onBindHelper(disposable: Disposable)
    fun onRequestFinish()
    fun errorToast(msg: String?)
}