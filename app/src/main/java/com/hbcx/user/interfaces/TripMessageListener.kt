package com.hbcx.user.interfaces

import org.json.JSONObject

/**
 * 行程中消息接口
 */
interface TripMessageListener {
    //行程中司机位置等信息
    fun onCancel(obj: JSONObject)
}