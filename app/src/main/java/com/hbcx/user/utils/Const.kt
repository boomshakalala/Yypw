package com.hbcx.user.utils

/**
 * 一些常量等。
 * Created on 2018/3/20.
 */
object Const {
    //各类第三方key
    const val UMENG_KEY = "5c173d17f1f55613b6000279"
    const val WX_APP_ID = "wxc1e37630a589fb3c"
    const val WX_SECRET = "1049ce104f6950dbdda395f18788ca45"
    const val QQ_APP_ID = "1106342128"
    const val QQ_SECRET = "OhjFF3S6RJgTsg8y"
    const val SINA_APP_ID = ""
    const val SINA_SECRET = ""
    const val RULE = "http://www.baidu.com"
    const val PLATFORM_PROTOCOL = "http://www.baidu.com"
    const val ABOUT_US = "http://www.baidu.com"
    const val SERVICE_PHONE = "service_phone"
    const val PAY_ACTION = "cn.dznev.wx.pay"
    const val JUHE_KEY = "a7ae4cd180b4397f9cda6588367ced21"
    //阿里云oss
    const val BUCKET_NAME = "juzizhongchou"
    const val OSS_AK = "LTAIajzvD80KBbNk"
    const val OSS_AKS = "EgrGf34LmRI6MIj20Ca2jGV8BOODsN"

    object User {
        const val IS_LOGIN = "is_Login"
        const val USER_ID = "userId"
        const val USER_PHONE = "userPhone"
        const val USER_HEAD = "userHead"
        const val USER_NAME = "userName"
        const val USER_SEX = "userSex"
        const val USER_BIRTH = "userBirth"
    }

    object Code {
        const val LOG_OUT = 2
    }

    object Method {
        const val PING_RECEIVE = "PING"//服务器心跳
        const val PING_SEND = "OK"//客户端心跳
        const val LOTICO = "LOTICO"//司机上传经纬度
        const val ORDER = "ORDER_USER"//订单
        const val ORDER_FINISH = "ORDER_END"//订单完成
        const val USER_LOGIN = "USER_LOGIN"//用户其他设备登录
        const val PLAT_CANCEL = "ORDER_CANCLE_PLAT"//平台取消
        const val USER_ORDER_SETOff = "USER_ORDER_SETOff"//实时计费模式下,司机出发前往预约地点 推给用户的消息
        const val USER_ORDER_DAODA = "USER_ORDER_DAODA"//实时计费模式下,司机到达预约地点 推给用户的消息
        const val USER_ORDER_BEGIN = "USER_ORDER_BEGIN"//实时计费模式下,司机开始服务 推给用户的消息
        const val USER_ORDER_OVER = "USER_ORDER_OVER"//实时计费模式下,司机送达乘客 推给用户的消息
        const val USER_ORDER_NOYINGDA = "USER_ORDER_NOYINGDA"//用户呼叫订单,三次推单后无司机应答 订单自动取消

    }
}