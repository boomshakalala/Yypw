package cn.sinata.xldutils.rxutils

import cn.sinata.xldutils.data.ResultData
import cn.sinata.xldutils.defaultScheduler
import io.reactivex.Flowable

/**
 *
 */
inline fun <reified O, I : ResultData<O>> Flowable<I>.request(helper: RequestHelper, showToast: Boolean = true, crossinline success: (msg: String?, t: O?) -> Unit, crossinline error: (code: Int, msg: String) -> Unit = { _, _ -> }) {
    this.defaultScheduler().subscribe(object : ResultDataSubscriber<O>(helper) {
        override fun onSuccess(msg: String?, data: O?) {
            success(msg, data)
        }

        override fun isShowToast() = showToast

        override fun onError(code: Int, msg: String) {
            super.onError(code, msg)
            error(code, msg)
        }
    })
}