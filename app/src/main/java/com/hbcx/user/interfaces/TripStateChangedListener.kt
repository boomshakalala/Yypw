package com.hbcx.user.interfaces

import com.amap.api.maps.model.LatLng

/**
 * 行程状态改变接口
 */
interface TripStateChangedListener {

    //等待应答
    fun onWaitingResponse(latLng: LatLng)
    //司机接单
    fun onResponse(order: com.hbcx.user.beans.Order)
    //司机前往预约地点
    fun onWaitingDriver(order: com.hbcx.user.beans.Order)
    //司机到达预约地点
    fun onDriverArrived(order: com.hbcx.user.beans.Order)
    //行程中
    fun onTripping(order: com.hbcx.user.beans.Order)
    //行程结束
    fun onTripFinished(order: com.hbcx.user.beans.Order)
    //无司机应答
    fun onNoResponse()
}