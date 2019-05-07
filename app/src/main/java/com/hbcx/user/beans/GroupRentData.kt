package com.hbcx.user.beans

import com.hbcx.user.R
import java.io.Serializable

data class GroupRentMain(val charteredDay: Int, var orderList: ArrayList<GroupRentOrder>?, val carLevelList: ArrayList<GroupRentType>)

data class GroupRentOrder(val id: Int, val createTime:Long,val startAddress: String,val endAddress:String,val startTime: Long, val carNum: Int, val times: Int, val status: Int,val address:String,val startCity:String,val endCity:String
                          , val balance: Double, val deposit: Double, val startMoney: Double, val endMoney: Double, val brandName: String,val imgUrl:String,val orderNum:String,val businessStartTime:String,val charteredNum:String
                          , val modelName: String, val levelName: String, val companyName: String, val contactNumber: String, val companyIcon: String,val pedestal:Int,val phone:String,val businessEndTime:String) {
    fun getStateStr(): String = when (status) { //1=待确认，2=待支付，3=待签约，4=待包车，5=包车中，6=已完成，7=已过期，8=已取消
        1 -> "待确认"
        2 -> "待支付"
        3 -> "待签约"
        4 -> "待包车"
        5 -> "包车中"
        6 -> "已完成"
        7 -> "已过期"
        else -> "已取消"
    }

    fun getStateColor(): Int = when (status) { //1=待确认，2=待支付，3=待签约，4=待包车，5=包车中，6=已完成，7=已过期，8=已取消
        1,3 -> R.color.color_tv_orange
        2 -> R.color.color_money_text
        6,7, 8 -> R.color.grey_text
        else -> R.color.colorPrimary
    }

    fun getActionStr() =
            when (status) {
        2 -> "支付订金"
        3-> "立即签约"
        else -> "联系商家"
    }

    fun getCancelStr() = when(status){
        1,2,3,4 ->"取消订单"
        else->"删除订单"
    }
}

data class GroupRentType(val id: Int, val imgUrl: String, val name: String, val remark: String) : Serializable


data class GroupCarList(val num: Int, val name: String, val companyIcon: String, val id: Int, val reserveNum: Int, var carList: ArrayList<GroupRentCarData>)

data class GroupRentCarData(val imgUrl: String, val pedestal: Int, val modelName: String, val brandName: String, val money: Double, val id: Int) : Serializable

data class GroupCarDetail(val imgUrl: ArrayList<com.hbcx.user.beans.Image>, val pedestal: Int, val modelName: String, val brandName: String, val startMoney: Double
                          , val id: Int, val address: String, val companyName: String, val companyId: Int, val money: Double,
                          val contactNumber: String, val startTime: String, val endTime: String, val endMoney: Double) : Serializable