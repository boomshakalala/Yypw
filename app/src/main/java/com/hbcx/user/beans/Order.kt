package com.hbcx.user.beans

import java.io.Serializable

/**
 * 订单
 */
data class Order(val id: Int? = 0) : Serializable {
    val imgUrl: String? = "" //司机头像
    val driverOrderNums: Int? = 0 //司机接单数
    val nickName: String? = ""
    val carColor: String? = ""
    val driverId: Int? = 0
    val modelName: String? = "" //车型名称
    val brandName: String? = "" //车辆品牌名称
    val licensePlate: String? = "" //车牌号
    var score: Double? = 0.0
    var type: Int? = 0 //服务类型(1=快车，2=专车经济，3=专车舒适，4=专车商务)
    val orderType: String? = "" //订单类型(1=普通，2=预约)
    val orderNum: String? = "" //订单编号
    val phone: String? = ""
    val startAddress: String? = ""
    val endAddress: String? = ""
    val cancleTime: Long? = 0 //可免责取消订单时间
    val setOutIsNot: Boolean? = false //待接驾状态(司机是否出发，false否，true是)
    val estimateTime: Int? = 0
    val serviceTime: Long? = 0 // 	已经行驶时间(服务中)
    val arrivalTime: Long? = 0 // 	到达预约地点时间(用户判断司机等待时间)
    val estimateDistance: Double? = 0.0
    val serviceDistance: Double? = 0.0 //已经行驶距离(服务中)
    val departureTime: Long? = 0 //预约出发时间
    val duration: Int? = 0 //时间
    val durationMoney: Double? = 0.0 //时长费
    val couponsMoney: Double? = 0.0 //优惠券
    val mileage: Double? = 0.0 //里程
    val mileageMoney: Double? = 0.0 //里程费
    val payMoney: Double? = 0.0 //支付金额
    val startLat: String? = ""
    val startLon: String? = ""
    val endLon: String? = ""
    val endLat: String? = ""
    val createTime: Long? = 0
    val time: Int? = 0
    val lon: Double? = 0.0
    val lat: Double? = 0.0
    var status: Int? = 0 //订单状态(1=待应答，2=待接驾，3=待上车，4=服务中，5=待支付，6=取消待支付，7=待评价，8=已完成，9=已取消)

    var evaluateContent: String? = ""//评价内容
    var evaluateScore: Int? = 0//评分

    val cancleMoney: Double? = 0.0 //取消订单金额

    val orderMoney: Double? = 0.0 //订单金额
    val longDurationMoney: Double? = 0.0 //远途费
    val longMileage: Double? = 0.0 //远途距离
    val nightMoney: Double? = 0.0 //夜间费


    fun getStateStr(): String {
        //1=待接单 2=已接单 3=出发前往预约地点 4=到达预约地点 5=开始服务
        // 6=待支付 7=完成服务(待评价) 8=完成服务(已评价) 9=无偿取消
        // 10=有偿取消 11=已改派 12=异常结束
        return when (status) {
            1 -> {
                "等待应答"
            }
            2 -> {
                "等待接驾"
            }
            3 -> {
                "等待上车"
            }
            4 -> {
                "服务中"
            }
            5 -> {
                "待支付"
            }
            6 -> {
                "取消待支付"
            }
            7 -> {
                "待评价"
            }
            8 -> {
                "已完成"
            }
            9 -> {
                "已取消"
            }
            else -> ""
        }
    }
}