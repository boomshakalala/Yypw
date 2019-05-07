package cn.sinata.xldutils.rxutils

import io.reactivex.subscribers.DisposableSubscriber


/**
 *
 */
abstract class ResultSubscriber<T>(helper: RequestHelper) : DisposableSubscriber<T>() {
    protected var helper: RequestHelper? = helper
    override fun onStart() {
        super.onStart()
        helper?.onBindHelper(this)
    }
    override fun onNext(t: T){
        helper?.onRequestFinish()
    }

    override fun onComplete() {

    }

    override fun onError(t: Throwable) {
        helper?.onRequestFinish()
    }
}

