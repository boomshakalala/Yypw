package com.hbcx.user.network

import cn.sinata.xldutils.data.ResultData
import com.google.gson.JsonObject
import com.hbcx.user.beans.Message
import com.hbcx.user.utils.Const
import io.reactivex.Flowable
import okhttp3.MultipartBody
import retrofit2.http.*

/**
 *
 */
interface ApiService {

    @POST("app/server")
    fun stringRequest(@Query("key") key: String): Flowable<ResultData<String>>

    @POST("app/server")
    fun simpleRequest(@Query("key") key: String): Flowable<ResultData<JsonObject>>

    @POST("app/server")
    fun getBanner(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.Banner>>>

    @POST("app/server")
    fun getNearbyDriver(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.NearbyDriver>>>

    @POST("app/server")
    fun getOrderDetail(@Query("key") key: String): Flowable<ResultData<com.hbcx.user.beans.Order>>

    @POST("app/server")
    fun getPayInfo(@Query("key") key: String): Flowable<ResultData<com.hbcx.user.beans.PayInfo>>

    @POST("app/server")
    fun getFastOrderList(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.Order>>>

    @POST("app/server")
    fun getRentOrderList(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.RentOrder>>>

    @POST("app/server")
    fun getGroupRentOrderList(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.GroupRentOrder>>>

    @POST("app/server")
    fun sendSms(@Query("phone") server: String, @Query("phone") phone: String, @Query("type") type: Int): Flowable<ResultData<JsonObject>>

    @POST("app/server")
    fun getRentMain(@Query("key") key: String): Flowable<ResultData<com.hbcx.user.beans.RentMain>>

    @POST("app/server")
    fun getGroupRentMain(@Query("key") key: String): Flowable<ResultData<com.hbcx.user.beans.GroupRentMain>>

    @POST("app/server")
    fun getCarList(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.CarList>>>

    @POST("app/server")
    fun getGroupCarList(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.GroupCarList>>>

    @POST("app/server")
    fun getCarLabels(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.CarLabel>>>

    @POST("app/server")
    fun getCarBrands(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.CarBrand>>>

    @POST("app/server")
    fun getCarDetail(@Query("key") key: String): Flowable<ResultData<com.hbcx.user.beans.CarDetail>>

    @POST("app/server")
    fun getGroupCarDetail(@Query("key") key: String): Flowable<ResultData<com.hbcx.user.beans.GroupCarDetail>>

    @POST("app/server")
    fun getCarSafe(@Query("key") key: String): Flowable<ResultData<com.hbcx.user.beans.CarSafe>>

    @POST("app/server")
    fun companyInfo(@Query("key") key: String): Flowable<ResultData<com.hbcx.user.beans.CompanyInfo>>

    @POST("app/server")
    fun rentOrderDetail(@Query("key") key: String): Flowable<ResultData<com.hbcx.user.beans.RentOrder>>

    @POST("app/server")
    fun groupRentOrderDetail(@Query("key") key: String): Flowable<ResultData<com.hbcx.user.beans.GroupRentOrder>>

    @POST("app/server")
    fun companyPoint(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.CompanyPoint>>>

    @POST("app/server")
    fun getDriver(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.Driver>>>

    @POST("app/server")
    fun getCoupons(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.Coupon>>>

    @POST("app/server")
    fun getOpenCity(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.OpenCity>>>

    @POST("app/server")
    fun getEndCity(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.OpenProvince>>>


    @POST("app/server")
    fun getMessages(@Query("key") key: String): Flowable<ResultData<ArrayList<Message>>>

    @POST("app/server")
    fun lineTypeList(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.LineType>>>

    @POST("app/server")
    fun lineList(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.TicketLine>>>

    @POST("app/server")
    fun ticketLineList(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.TicketList>>>

    @POST("app/server")
    fun getLineDetail(@Query("key") key: String): Flowable<ResultData<com.hbcx.user.beans.TicketLineDetail>>

    @POST("app/server")
    fun getSpecialLineDetail(@Query("key") key: String): Flowable<ResultData<com.hbcx.user.beans.LineDetail>>

    @POST("app/server")
    fun getIntegral(@Query("key") key: String): Flowable<ResultData<com.hbcx.user.beans.IntegralData>>

    /**文件上传**/
    @Multipart
    @POST("app/public/uplaodImg")
    fun uploadFile(@Part() filePart: MultipartBody.Part): Flowable<ResultData<JsonObject>>

    @POST("app/server")
    fun getPassengerList(@Query("key") key: String): Flowable<ResultData<ArrayList<com.hbcx.user.beans.PassengerTicket>>>

    @POST("app/server")
    fun getTicketOrderDetail(@Query("key") key:String):Flowable<ResultData<com.hbcx.user.beans.TicketOrder>>

    @POST("app/server")
    fun getTicketOrderList(@Query("key") key:String):Flowable<ResultData<ArrayList<com.hbcx.user.beans.TicketOrder>>>

    @POST("app/server")
    fun getInviteRecord(@Query("key") key:String):Flowable<ResultData<ArrayList<com.hbcx.user.beans.InviteRecord>>>

    @POST("app/server")
    fun getStaions(@Query("key") key:String):Flowable<ResultData<ArrayList<com.hbcx.user.beans.Region>>>

    @POST("app/server")
    fun getStations(@Query("key") key:String):Flowable<ResultData<ArrayList<com.hbcx.user.beans.Station>>>

    @GET
    fun checkIdCard(@Url url:String, @Query("key") key: String, @Query("idcard") idcard: String, @Query("realname") realname: String) :Flowable<JsonObject>
}