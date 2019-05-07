package com.hbcx.user.beans

import java.io.Serializable

data class NearbyDriver(val distance:String,val lon:Double,val lat:Double)
data class Appraise(val score:Int,val createTime:Long,val remark:String)
data class Coupon(val money:Double,val expiryTime:Long,val id:Int,val state:Int,val useType:Int,var isChecked:Boolean = false):Serializable{
    fun getTitle():String{
        return when(useType){
            1->"票务优惠券"
            2->"租车优惠券"
            3->"包车优惠券"
            4->"快车优惠券"
            5->"专车优惠券"
            else -> "通用优惠券"
        }
    }
    fun getDescribe():String{
        return when(useType){
            1->"仅限票务使用"
            2->"仅限租车使用"
            3->"仅限包车使用"
            4->"仅限快车使用"
            5->"仅限专车使用"
            else -> ""
        }
    }
}