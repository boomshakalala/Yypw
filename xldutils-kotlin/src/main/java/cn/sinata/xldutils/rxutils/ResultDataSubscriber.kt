package cn.sinata.xldutils.rxutils

import cn.sinata.xldutils.data.ResultData
import cn.sinata.xldutils.sysErr
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * 服务器返回数据基本处理
 */
abstract class ResultDataSubscriber<T>(helper: RequestHelper) : ResultSubscriber<ResultData<T>>(helper) {

    override fun onNext(t: ResultData<T>) {
        sysErr(t.msg+"---------"+t.data+"-----"+t.code)
        SystemUtil.sysTime = t.sys!!
        if (t.code == 0) {
            if (t.msg == null) {
                t.msg=""
            }
            onSuccess(t.msg,t.data)
        }
        else {
            onError(ResultException(t.code, t.msg))
        }
    }

    /**
     * 默认错误方法。
     */
    override fun onError(t: Throwable) {
        super.onError(t)
        sysErr(t.message)
        t.printStackTrace()
        var code = -1
        var msg = Error.REQUEST_ERROR
        when (t) {
            is JsonSyntaxException -> msg = Error.PARSER_ERROR
            is ConnectException -> msg = Error.NET_ERROR
            is SocketTimeoutException -> msg = Error.NET_ERROR
            is HttpException -> msg = Error.SERVER_ERROR
            is ResultException -> {
                code = t.code
                msg = t.message!!
            }

        }
        onError(code, msg)
    }

    /**
     * 错误时处理。如有特殊code判断，复写此方法
     */
    open fun onError(code: Int, msg: String) {
        if (isShowToast()) {
            _showToast(msg)
        }
    }

    private fun _showToast(msg: String) {
        helper?.errorToast(msg)
    }

    /**
     * 默认展示错误Toast
     */
    open fun isShowToast(): Boolean = true

    /**
     * 服务器返回成功code时数据
     */
    abstract fun onSuccess(msg: String?, data: T?)
}