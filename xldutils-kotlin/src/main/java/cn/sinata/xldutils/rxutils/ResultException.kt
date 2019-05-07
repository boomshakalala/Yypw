package cn.sinata.xldutils.rxutils

/**
 * 接口相关异常
 */
class ResultException : Exception{
    var code:Int = -1
    constructor(msg:String?):super(msg)
    constructor(code: Int, msg: String?) : super(msg){
        this.code = code
    }

    constructor(throwable: Throwable) : super(throwable)
    constructor(code: Int, throwable: Throwable) : super(throwable){
        this.code = code
    }


}