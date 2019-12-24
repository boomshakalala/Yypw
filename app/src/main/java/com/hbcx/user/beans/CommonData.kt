package com.hbcx.user.beans

import com.github.promeg.pinyinhelper.Pinyin

data class OpenCity(val id:Int,val cityCode:String,val cityName:String,var isChecked:Boolean = false){
    fun getInitial():String = Pinyin.toPinyin(cityName[0])[0].toString()
}

data class OpenProvince(val province:String,val city:ArrayList<OpenCity>,var isChecked:Boolean = false)


data class IntegralData(val consumptionIntegral:Int,val integral:Int,val totalIntegral:Int,val integralRecordList:ArrayList<ScoreRecored>)
data class ScoreRecored(val title:String,val integral:Int,val state:Int,val createTime:Long)
data class InviteRecord(val imgUrl: String,val createTime:Long,val phone:String,val nickName:String)
data class Message(val id:Int,val title:String,val content:String,val imgUrl:String,val createTime:Long,val type:Int)