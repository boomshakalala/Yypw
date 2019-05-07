package cn.sinata.xldutils.data

import com.google.gson.annotations.SerializedName

/**
 * 服务端返回数据基础格式类
 */
data class ResultData<T>(@SerializedName("code") var code: Int) {
    var data: T? = null
    @SerializedName("msg")
    var msg: String? = ""
        get() = field ?: ""
    val sys: Long? = 0
        get() = field ?: 0
}