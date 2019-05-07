package com.hbcx.user.beans

import android.view.View
import com.hbcx.user.R
import java.io.Serializable

data class TicketOrder(val id: Int, val createTime: Long, val rideDate: Long, val orderNum: String, val phone: String, val endTime: String,val ticketMoney:Double,
                       val status: Int, val totalMoney: Double, val serverMoney: Double, val insuranceMoney: Double, val integralMoney: Double,
                       val couponsMoney: Double, val payMoney: Double, val pointDownCityName: String, val pointDownName: String, val pointUpName: String
                       ,val pointUpCityName:String,val peopleNum:Int,val passengerList:ArrayList<PassengerTicket>):Serializable{
    fun getStatusStr() = when(status){//状态(1=待支付，2=待乘车，3=已乘车，4=已评价，5=已退票，6=已过期，7=已取消)
        1->"待支付"
        2->"待乘车"
        3->"已乘车"
        4->"已完成"
        5->"已退票"
        6->"已过期"
        7->"已取消"
        else->""
    }
    fun getStatusColorRes() = when(status){//状态(1=待支付，2=待乘车，3=已乘车，4=已评价，5=已退票，6=已过期，7=已取消)
        1,2-> R.color.color_money_text
        3,4->R.color.colorPrimary
        else->R.color.grey_text
    }
    fun getPositiveVisibility() = if (status in (1..3)) View.VISIBLE else View.GONE

    fun getNegativeBtnStr() = when(status){
        1->"取消订单"
        2->"申请退票"
        else->"删除订单"
    }

    fun getPositiveBtnStr() = when(status){
        1->"立即支付"
        2->" 验票码 "
        3->"立即评价"
        else->""
    }
}

/**专线详情*/
data class LineDetail(val name: String, val remark: String, val id: Int, val content: String, val imgUrl: ArrayList<com.hbcx.user.beans.Image>, val lineList: ArrayList<TicketList>)

data class TicketLine(val id: Int, val name: String, val remark: String, val imgUrl: String)

data class TicketList(val id: Int, val pedestal: String, val km1: Double, val start_time: String, val km2: Double, val money: Double, val endName: String, val startCityCode: String, val endCityCode: String, val startPointId: Int, val endPointId: Int, val startName: String, var facilitiesName: String?)

/**班线详情**/
data class TicketLineDetail(val id: Int, val pedestal: String, val km1: Double, val start_time: String, val km2: Double, val startLat: Double, val endLon: Double
                            , val endLat: Double, val money: Double, val startLon: Double, val endName: String, val startName: String,
                            val stationList: ArrayList<BusStation>,val startCityName:String,val endCityName:String)

/**站点**/
data class BusStation(val id: Int, val times: String, val name: String, val isUpOrDown: Int, val type: Int,val lon:Double,val lat:Double,
                      var isChecked: Boolean = false) //选择Dialog使用

data class LineType(val id: Int, val name: String)

/**详情乘客票务信息*/
data class PassengerTicket(val id: Int, val name: String, val idCard: String, val status: Int, val elTicket: String?):Serializable

/**搜索历史记录**/
data class TicketHistory(val start: String, val end: String, val startCode: String, val endCode: String, val startId: Long,val startStationId:Long,val endStationId:Long,val lineType: Long)

data class Region(
    val areaCode: String = "",
    val name: String = "",
    val stationList: ArrayList<Station> = arrayListOf()
)

data class Station(
    val areaCode: String = "",
    val lineType: Int = 0,
    val stationAddress: String = "",
    val stationId: Int = 0,
    val stationName: String = ""
)