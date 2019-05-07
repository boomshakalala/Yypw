package com.hbcx.user.beans

import com.hbcx.user.R
import java.io.Serializable

data class RentMain(val rentingDay:Int,val rentingHour:Int,var orderList:ArrayList<RentOrder>?,val carLevelList:ArrayList<CarLevel>)

data class CarLevel(val name:String,val id:Int):Serializable

data class CarList(val imgUrl:String,val brandName:String,val modelName:String,val levelName:String,val carList:ArrayList<CarData>)

data class CarData(val name:String,val rentingCarLabelList:ArrayList<CarLabel>,val day_money:Double,val distance:Long,val id:Int,val company_icon:String):Serializable

//汽车标签
data class CarLabel(val id:Int,val name:String,val remark:String):Serializable
//汽车品牌
data class CarBrand(val id:Int,val name:String):Serializable
//租车保障费
data class CarSafe(val comprehensiveMoney:Double,val basicsMoney:Double):Serializable

data class CarDetail(val pedestal:Int, val brandName:String, val company_id:Int, val carLineMoney:Double,
                     val distance:Long, val hour_money:Double, val companyName:String, val day_money:Double,
                     val displacement:String, val startTime:String, val id:Int, val endTime:String, val gears:String,
                     val contact_number:String, val modelName:String, val levelName:String, val ruleList:ArrayList<RentRule>?,
                     val imgUrl:ArrayList<com.hbcx.user.beans.Image>, val rentingCarLabelList:ArrayList<CarLabel>)

data class RentRule(val value:String,val key:String)

data class CompanyInfo(val startTime:String,val distance:Double,val reserveNum:Int,val name:String,val endTime:String,val id:Int,val rentingNum:Int,val contactNumber:String,val lon: Double,val lat: Double,val address: String,val companyPictures:ArrayList<com.hbcx.user.beans.Image>)

data class CompanyPoint(val distance:Double,val name: String,val lon:Double,val lat:Double)

data class Driver(val id:Int,val userId: Int,val name:String,val idCards:String,var phone:String? = "",val licenseOrNot:Int,val licenseNum:String,var isChecked:Boolean = false):Serializable{
    fun getPhoneStr():String{
        return if (phone==null||phone!!.isEmpty()||phone == "null")
            ""
        else
            phone!!
    }
}

//订单详情
data class RentOrder(val id:Int,val createTime:Long,val startTime:Long,val endTime:Long,val hourOrDay:Int,val times:Int,val status:Int,
                     val payMoney:Double,val displacement:String,val gears:String,val pedestal:Int,val brandName:String,val imgUrl:String,
                     val modelName:String,val levelName:String,val companyName:String,val contactNumber:String,val orderNum:String,
                     val couponsMoney:String,val orderMoney:String,val rentingMoney:String,val serverMoney:String,val address:String,
                     val name:String,val idCards:String,val distance:Double,val doBusinessStartTime:String,val phone:String,val lat:Double,val lon:Double,
                     val doBusinessEndTime:String,val carLineMoney:Double,val companyId:Int,val rentingCarLabelList:ArrayList<CarLabel>):Serializable{
    fun getStateStr() = when(status){
        1-> "待支付"
        2-> "待确认"
        3-> "待取车"
        4-> "租车中"
        5-> "已完成"
        6-> "已过期"
        7-> "已取消"
        else->""
    }
    fun getActionStr() = when(status){
        1-> "立即支付"
        else->"联系商家"
    }
    fun getCancelStr() = when(status){
        1,2,3 ->"取消订单"
        else->"删除订单"
    }
    fun getStateColor() = when(status){
        1-> R.color.color_money_text
        2-> R.color.color_tv_orange
        3,4-> R.color.colorPrimary
        5-> R.color.color_tv_orange
        6,7-> R.color.grey_text
        else->R.color.color_tv_orange
    }
}