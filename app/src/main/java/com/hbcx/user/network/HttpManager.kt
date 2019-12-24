package com.hbcx.user.network

import android.text.TextUtils
import android.util.Log
import cn.sinata.util.DES
import cn.sinata.xldutils.data.ResultData
import cn.sinata.xldutils.defaultScheduler
import com.google.gson.JsonObject
import com.hbcx.user.beans.*
import com.hbcx.user.utils.Const
import io.reactivex.Flowable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

/**
 * 网络请求处理
 */
object HttpManager {

    const val PAGE_SIZE = 20
    const val encodeDES = true

    private class ParamsBuilder private constructor() {
        private val sb: StringBuilder = StringBuilder()

        fun build(): String {
            return sb.toString()
        }

        fun build(des: Boolean): String {
            return if (des) {
                Log.d("server:", sb.toString())
                DES.encryptDES(sb.toString())
            } else {
                sb.toString()
            }
        }

        fun append(key: String, value: String): ParamsBuilder {
            _append(key, value)
            return this
        }

        fun append(key: String, value: Int): ParamsBuilder {
            _append(key, value)
            return this
        }

        fun append(key: String, value: Double): ParamsBuilder {
            _append(key, value)
            return this
        }

        fun append(key: String, value: Float): ParamsBuilder {
            _append(key, value)
            return this
        }

        fun append(key: String, value: Long): ParamsBuilder {
            _append(key, value)
            return this
        }

        private fun _append(key: String, value: Any) {
            var value = value
            if (value is String) {

                if ("null" == value || TextUtils.isEmpty(value.toString())) {
                    value = ""
                }
            }
            if (sb.isEmpty()) {
                sb.append(key)
                sb.append(SPLIT)
                sb.append(value)
            } else {
                if (sb.contains(BEGIN)) {
                    sb.append(AND)
                    sb.append(key)
                    sb.append(SPLIT)
                    sb.append(value)
                } else {
                    sb.append(BEGIN)
                    sb.append(key)
                    sb.append(SPLIT)
                    sb.append(value)
                }
            }
        }

        companion object {
            const val SPLIT = "="
            const val AND = "&"
            const val BEGIN = "?"

            fun create(): ParamsBuilder {
                return ParamsBuilder()
            }
        }

    }

    /**
     * 发起请求方法
     */
    private fun request() =
            RRetrofit.instance().create(ApiService::class.java)


//    fun uploadFile(context: Context, file: File):Flowable<String>{
//        // 明文设置secret的方式建议只在测试时使用，更多鉴权模式请参考后面的`访问控制`章节
//        val credentialProvider = OSSPlainTextAKSKCredentialProvider(Const.OSS_AK, Const.OSS_AKS)
//        val oss = OSSClient(context.applicationContext, Api.OSS_END_POINT, credentialProvider)
//        val objectKey = "DaZhou/image/" + System.currentTimeMillis() + "." + file.suffix()
//        // 构造上传请求
//        val put = PutObjectRequest(Const.BUCKET_NAME, objectKey, file.path)
//        return Flowable.create(FlowableOnSubscribe<String> {
//            try {
//                oss.putObject(put)
//                val url = oss.presignPublicObjectURL(Const.BUCKET_NAME, objectKey)
//                it.onNext(url)
//                it.onComplete()
//            } catch (e: ClientException) {
//                e.printStackTrace()
//                it.onError(ResultException("上传失败！"))
//            } catch (e: ServiceException) {
//                e.printStackTrace()
//                it.onError(ResultException("上传失败！"))
//            }
//        }, BackpressureStrategy.DROP).defaultScheduler()
//    }
    /**
     * 登录
     */
    fun login(phone: String, passWord: String, type: Int, cityName: String): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.LOGIN).append("phone", phone).append("passWord", passWord).append("type", type).append("cityName", cityName)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 重置密码并登录
     */
    fun forgetPwd(phone: String, passWord: String, code: String): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.FORGET).append("phone", phone).append("passWord", passWord).append("code", code)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }
//    /**
//     * 验证验证码
//     */
//    fun checkSms(phone: String, type: Int, code: String) =
//            request().checkSms(phone, type, code).defaultScheduler()

    /**
     * 发送验证码
     * @param type 类型【1=用户登录，2=用户更换手机，3=用户忘记密码，4=司机注册，5=司机更换手机，6=司机忘记密码】
     */
    fun sendSms(phone: String, type: Int): Flowable<ResultData<String>> {
        val request = ParamsBuilder.create().append("server", Api.SEND_MSM).append("phone", phone).append("type", type)
        return request().stringRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 验证验证码
     * @param type 类型【1=用户登录，2=用户更换手机，3=用户忘记密码，4=司机注册，5=司机更换手机，6=司机忘记密码】
     */
    fun checkCode(phone: String, code: String, type: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.CHECK_CODE).append("phone", phone).append("code", code).append("type", type)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 修改手机号
     */
    fun modifyPhone(id: Int, phone: String, code: String): Flowable<ResultData<String>> {
        val request = ParamsBuilder.create().append("server", Api.MODIFY_PHONE).append("id", id).append("phone", phone).append("code", code)
        return request().stringRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 修改密码
     */
    fun modifyPwd(id: Int, passWord: String): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.SET_PWD).append("id", id).append("passWord", passWord)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 广告页
     */
    fun getAd(): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.GET_AD)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 获取开通城市
     */
    fun getOpenCity(): Flowable<ResultData<ArrayList<com.hbcx.user.beans.OpenCity>>> {
        val request = ParamsBuilder.create().append("server", Api.GET_OPEN_CITY)
        return request().getOpenCity(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 获取到达城市
     */
    fun getEndCity(id: Long): Flowable<ResultData<ArrayList<OpenProvince>>> {
        val request = ParamsBuilder.create().append("server", Api.GET_END_CITY)
                .append("id", id)
        return request().getEndCity(request.build(encodeDES)).defaultScheduler()
    }


    /**
     * 城市是否开通
     */
    fun isCityOpen(cityCode: String): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.IS_CITY_OPEN)
                .append("cityCode", cityCode)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 是否有新消息
     */
    fun hasNewMsg(id: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.HAS_NEW_MSG)
                .append("id", id).append("peoType", 1)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 获取消息
     */
    fun getMsg(id: Int, page: Int): Flowable<ResultData<ArrayList<Message>>> {
        val request = ParamsBuilder.create().append("server", Api.GET_MSG_LIST)
                .append("id", id).append("peoType", 1).append("page", page).append("rows", PAGE_SIZE)
        return request().getMessages(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 清空消息
     */
    fun clearMsg(id: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.CLEAR_MSG)
                .append("id", id).append("peoType", 1)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 可预约时间
     */
    fun getReserveTime(type: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.GET_RESERVE_TIME).append("type", type)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * Banner
     */
    fun getBanner(): Flowable<ResultData<ArrayList<com.hbcx.user.beans.Banner>>> {
        val request = ParamsBuilder.create().append("server", Api.GET_BANNER)
        return request().getBanner(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 附近司机
     */
    fun getNearbyDriver(type: Int, lon: Double, lat: Double): Flowable<ResultData<ArrayList<com.hbcx.user.beans.NearbyDriver>>> {
        val request = ParamsBuilder.create().append("server", Api.NEARBY_DRIVER).append("type", type).append("lon", lon).append("lat", lat)
        return request().getNearbyDriver(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 计算费用
     */
    fun calculatePrice(type: Int, startLon: Double, startLat: Double, endLon: Double, endLat: Double, id: Int = 0): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.GET_CALCULATION)
                .append("type", type).append("startLon", startLon)
                .append("startLat", startLat).append("endLon", endLon)
                .append("endLat", endLat)
        if (id > 0)
            request.append("id", id)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 可用优惠券数量
     * @param type 使用类型(1=票务，2=租车，3=包车，4=快车，5=专车)
     */
    fun couponNum(userId: Int, money: Double, type: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.COUPONS_NUM).append("userId", userId).append("money", money).append("type", type)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 票务下单
     */
    fun createTicketOrder(userId: Int, passenger: String, rideDates: String, shiftId: Int, pointUpId: Int,
                          pointDownId: Int, peopleNum: Int, payMoney: Double, serverMoney: Double,
                          couponsId: Int, couponsMoney: Double, integralMoney: Double, insuranceMoney: Double,
                          pointUpName: String, pointDownName: String, pointUpCityName: String,
                          pointDownCityName: String, phone: String): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.CREATE_TICKET_ORDER).append("userId", userId)
                .append("passenger", passenger).append("rideDates", rideDates).append("shiftId", shiftId).append("pointUpId", pointUpId)
                .append("pointDownId", pointDownId).append("peopleNum", peopleNum).append("payMoney", payMoney).append("serverMoney", serverMoney)
                .append("couponsId", couponsId).append("couponsMoney", couponsMoney).append("integralMoney", integralMoney).append("insuranceMoney", insuranceMoney)
                .append("pointUpName", pointUpName).append("pointDownName", pointDownName).append("pointUpCityName", pointUpCityName).append("pointDownCityName", pointDownCityName)
                .append("phone", phone)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 获取订单乘坐人集合
     */
    fun getPassengerList(id: Int): Flowable<ResultData<ArrayList<com.hbcx.user.beans.PassengerTicket>>> {
        val request = ParamsBuilder.create().append("server", Api.GET_PASSENGER_LIST)
                .append("id", id)
        return request().getPassengerList(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 删除优惠券
     */
    fun delCoupon(id: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.DEL_COUPON).append("id", id)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 下单
     */
    fun callOrder(type: Int, startLon: Double, startLat: Double, endLon: Double, endLat: Double, userId: Int, startAddress: String, endAddress: String, departTimes: String? = null): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.CALL_ORDER)
                .append("type", type).append("startLon", startLon)
                .append("startLat", startLat).append("endLon", endLon)
                .append("endLat", endLat).append("userId", userId)
                .append("startAddress", startAddress).append("endAddress", endAddress)
        if (departTimes != null)
            request.append("departTimes", departTimes)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 投诉
     */
    fun complaints(orderId: Int, complaintReason: String, complaintRemark: String?): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.COMPLAINT)
                .append("orderId", orderId).append("complaintReason", complaintReason)
        if (complaintRemark != null)
            request.append("complaintRemark", complaintRemark)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 评价
     */
    fun doEvaluate(orderId: Int, score: Int, remark: String?): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.DO_EVALUATE)
                .append("orderId", orderId).append("score", score)
        if (!remark.isNullOrEmpty())
            request.append("remark", remark!!)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 司机主页
     */
    fun getDriverInfo(id: Int, page: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.DRIVER_HOME)
                .append("id", id).append("page", page).append("rows", PAGE_SIZE)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 订单详情
     */
    fun getOrderDetail(orderId: Int): Flowable<ResultData<com.hbcx.user.beans.Order>> {
        val request = ParamsBuilder.create().append("server", Api.ORDER_DETAIL)
                .append("orderId", orderId)
        return request().getOrderDetail(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 快专车订单列表
     */
    fun getFastOrderList(id: Int, page: Int, type: Int): Flowable<ResultData<ArrayList<com.hbcx.user.beans.Order>>> {
        val request = ParamsBuilder.create().append("server", Api.FAST_ORDER_LIST).append("id", id).append("page", page).append("rows", PAGE_SIZE).append("type", type)
        return request().getFastOrderList(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 订单详情
     */
    fun cancelOrder(orderId: Int, cancleReason: String, cancleRemark: String): Flowable<ResultData<com.hbcx.user.beans.Order>> {
        val request = ParamsBuilder.create().append("server", Api.CANCEL_ORDER)
                .append("orderId", orderId).append("cancleReason", cancleReason).append("cancleRemark", cancleRemark)
        return request().getOrderDetail(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 支付信息
     */
    fun getPayInfo(id: Int, type: Int, orderType: Int): Flowable<ResultData<com.hbcx.user.beans.PayInfo>> {
        val request = ParamsBuilder.create().append("server", Api.GET_PAY_INFO)
                .append("id", id).append("type", type).append("orderType", orderType)
        return request().getPayInfo(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 租车首页
     */
    fun getRentMain(id: Int): Flowable<ResultData<com.hbcx.user.beans.RentMain>> {
        val request = ParamsBuilder.create().append("server", Api.RENT_MAIN)
                .append("userId", id)
        return request().getRentMain(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 租车列表
     */
    fun getCarList(page: Int, startTime: String, endTime: String, lon: Double, lat: Double, carLevelId: Int, brandId: Int, distance: Int, rentingCarLabelId: Int, startMoney: Double, endMoney: Double): Flowable<ResultData<ArrayList<com.hbcx.user.beans.CarList>>> {
        val request = ParamsBuilder.create().append("server", Api.CAR_LIST)
                .append("page", page).append("rows", PAGE_SIZE)
                .append("startTime", startTime).append("endTime", endTime)
                .append("lon", lon).append("lat", lat)
                .append("carLevelId", carLevelId).append("brandId", brandId)
                .append("distance", distance).append("rentingCarLabelId", rentingCarLabelId)
                .append("startMoney", startMoney).append("endMoney", endMoney)

        return request().getCarList(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 包车列表
     */
    fun getGroupCarList(page: Int, startTime: String, lon: Double, lat: Double, carLevelId: Int, brandId: Int, startMoney: Double, endMoney: Double): Flowable<ResultData<ArrayList<com.hbcx.user.beans.GroupCarList>>> {
        val request = ParamsBuilder.create().append("server", Api.GROUP_CAR_LIST)
                .append("page", page).append("rows", PAGE_SIZE)
                .append("startTime", startTime)
                .append("lon", lon).append("lat", lat)
                .append("carLevelId", carLevelId).append("brandId", brandId)
                .append("startMoney", startMoney).append("endMoney", endMoney)

        return request().getGroupCarList(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 车辆标签
     */
    fun getCarLabels(): Flowable<ResultData<ArrayList<com.hbcx.user.beans.CarLabel>>> {
        val request = ParamsBuilder.create().append("server", Api.CAR_LABELS)
        return request().getCarLabels(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 车辆品牌
     */
    fun getCarBrands(): Flowable<ResultData<ArrayList<com.hbcx.user.beans.CarBrand>>> {
        val request = ParamsBuilder.create().append("server", Api.CAR_BRANDS)
        return request().getCarBrands(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 租车保障费
     */
    fun getCarSafe(id: Int): Flowable<ResultData<com.hbcx.user.beans.CarSafe>> {
        val request = ParamsBuilder.create().append("server", Api.CAR_SAFE).append("id", id)
        return request().getCarSafe(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 车辆详情
     */
    fun getCarDetail(id: Int, userId: Int, lon: Double, lat: Double): Flowable<ResultData<com.hbcx.user.beans.CarDetail>> {
        val request = ParamsBuilder.create().append("server", Api.CAR_DETAIL)
                .append("id", id).append("userId", userId).append("lon", lon).append("lat", lat)
        return request().getCarDetail(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 包车车辆详情
     */
    fun getGroupCarDetail(id: Int, day: Int, startLon: Double, startLat: Double, endLon: Double, endLat: Double): Flowable<ResultData<com.hbcx.user.beans.GroupCarDetail>> {
        val request = ParamsBuilder.create().append("server", Api.GROUP_CAR_DETAIL)
                .append("id", id).append("day", day).append("startLon", startLon).append("startLat", startLat)
                .append("endLon", endLon).append("endLat", endLat)
        return request().getGroupCarDetail(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 公司信息
     */
    fun getCompanyInfo(id: Int, lon: Double = 0.0, lat: Double = 0.0): Flowable<ResultData<com.hbcx.user.beans.CompanyInfo>> {
        val request = ParamsBuilder.create().append("server", Api.COMPANY_INFO)
                .append("id", id).append("lon", lon).append("lat", lat)
        return request().companyInfo(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 公司取还车点
     */
    fun getCompanyPoint(id: Int, lon: Double, lat: Double): Flowable<ResultData<ArrayList<com.hbcx.user.beans.CompanyPoint>>> {
        val request = ParamsBuilder.create().append("server", Api.GET_COMPANY_POINT)
                .append("id", id).append("lon", lon).append("lat", lat)
        return request().companyPoint(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 添加乘车人
     */
    fun addPerson(userId: Int, name: String, idCards: String, licenseOrNot: Int, licenseNum: String = "", phone: String = "", id: Int = 0): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.ADD_DRIVER)
                .append("userId", userId).append("name", name).append("idCards", idCards)
                .append("licenseOrNot", licenseOrNot).append("licenseNum", licenseNum).append("phone", phone)
                .append("id", id)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 获取乘车人
     */
    fun getPerson(userId: Int, licenseOrNot: Int = 0): Flowable<ResultData<ArrayList<com.hbcx.user.beans.Driver>>> {
        val request = ParamsBuilder.create().append("server", Api.GET_DRIVER)
                .append("userId", userId).append("licenseOrNot", licenseOrNot)
        return request().getDriver(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 获取优惠券
     * type 1=未使用，2=已使用，3=已过期
     * useType 1=票务，2=租车，3=包车，4=快车，5=专车
     */
    fun getCoupons(page: Int, userId: Int, type: Int, useType: Int = 0): Flowable<ResultData<ArrayList<com.hbcx.user.beans.Coupon>>> {
        val request = ParamsBuilder.create().append("server", Api.COUPONS)
                .append("userId", userId).append("page", page).append("rows", PAGE_SIZE)
                .append("type", type).append("useType", useType)
        return request().getCoupons(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 租车下单
     */
    fun createRentOreder(carId: Int, userId: Int, ridingInforId: Int, address: String, lon: Double,
                         lat: Double, phone: String, startTimes: Long, endTimes: Long, couponsId: Int = 0,
                         serverMoney: Double, companyId: Int, hourOrDay: Int, times: Double): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.RENT_CREATE_ORDER)
                .append("userId", userId).append("carId", carId).append("ridingInforId", ridingInforId)
                .append("address", address).append("lon", lon).append("lat", lat)
                .append("phone", phone).append("startTimes", startTimes).append("endTimes", endTimes)
                .append("couponsId", couponsId).append("serverMoney", serverMoney).append("companyId", companyId)
                .append("hourOrDay", hourOrDay).append("times", times)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 包车下单
     */
    fun createGroupRentOrder(carId: Int, userId: Int, startAddress: String, endAddress: String, startCity: String,
                             endCity: String, startLon: Double, startLat: Double, endLon: Double, endLat: Double,
                             phone: String, startTimes: Long, times: Int, carNum: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.GROUP_CREATE_ORDER)
                .append("userId", userId).append("carId", carId).append("startAddress", startAddress)
                .append("endAddress", endAddress).append("startCity", startCity).append("endCity", endCity)
                .append("phone", phone).append("startTimes", startTimes).append("startLon", startLon)
                .append("startLat", startLat).append("endLon", endLon).append("endLat", endLat)
                .append("carNum", carNum).append("times", times)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 租车订单详情
     */
    fun getRentOrderDetail(id: Int): Flowable<ResultData<com.hbcx.user.beans.RentOrder>> {
        val request = ParamsBuilder.create().append("server", Api.RENT_ORDER).append("id", id)
        return request().rentOrderDetail(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 包车订单详情
     */
    fun getGroupRentOrderDetail(id: Int): Flowable<ResultData<com.hbcx.user.beans.GroupRentOrder>> {
        val request = ParamsBuilder.create().append("server", Api.GROUP_RENT_ORDER).append("id", id)
        return request().groupRentOrderDetail(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 取消订单
     */
    fun cancelRentOrder(id: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.CANCEL_RENT_ORDER)
                .append("id", id)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 删除租车订单
     */
    fun deleteRentOrder(id: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.DELETE_RENT_ORDER)
                .append("id", id)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 取消包车订单
     */
    fun cancelGroupOrder(id: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.CANCEL_GROUP_ORDER)
                .append("id", id)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 包车签约
     */
    fun makeSign(id: Int): Flowable<ResultData<String>> {
        val request = ParamsBuilder.create().append("server", Api.MAKE_SIGN)
                .append("id", id)
        return request().stringRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 删除包车订单
     */
    fun deleteGroupOrder(id: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.DELETE_GROUP_ORDER)
                .append("id", id)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 租车订单列表
     */
    fun getRentOrderList(userId: Int, page: Int): Flowable<ResultData<ArrayList<com.hbcx.user.beans.RentOrder>>> {
        val request = ParamsBuilder.create().append("server", Api.RENT_ORDER_LIST).append("userId", userId).append("page", page).append("rows", PAGE_SIZE)
        return request().getRentOrderList(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 包车订单列表
     */
    fun getGroupRentOrderList(userId: Int, page: Int): Flowable<ResultData<ArrayList<com.hbcx.user.beans.GroupRentOrder>>> {
        val request = ParamsBuilder.create().append("server", Api.GROUP_RENT_ORDER_LIST).append("userId", userId).append("page", page).append("rows", PAGE_SIZE)
        return request().getGroupRentOrderList(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 包车首页
     */
    fun getGroupRentMain(id: Int): Flowable<ResultData<com.hbcx.user.beans.GroupRentMain>> {
        val request = ParamsBuilder.create().append("server", Api.GROUP_RENT_MAIN)
                .append("userId", id)
        return request().getGroupRentMain(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 计算距离
     */
    fun getDistance(startLon: Double, startLat: Double, endLon: Double, endLat: Double): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.GET_DISTANCE)
                .append("startLon", startLon).append("startLat", startLat).append("endLon", endLon).append("endLat", endLat)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 上传图片
     */
    fun uploadFile(file: File): Flowable<ResultData<JsonObject>> {
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val part = MultipartBody.Part.createFormData("myfile", file.name, requestFile)
        return request().uploadFile(part).defaultScheduler()
    }

    /**
     * 反馈
     */
    fun feedback(userId: Int, content: String): Flowable<ResultData<JsonObject>> {
        val replace = content.replace("%", "%25").replace("&", "%26")
                .replace("+", "%2B").replace("=", "2D")
                .replace("#", "%23")
        val request = ParamsBuilder.create().append("server", Api.FEEDBACK)
                .append("type", 1).append("userId", userId).append("content", replace)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 积分
     */
    fun getIntegral(id: Int, page: Int): Flowable<ResultData<com.hbcx.user.beans.IntegralData>> {
        val request = ParamsBuilder.create().append("server", Api.INTEGRAL)
                .append("rows", PAGE_SIZE).append("id", id).append("page", page)
        return request().getIntegral(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 修改用户信息
     */
    fun updateUserInfo(id: Int, imgUrl: String, nickName: String, sex: Int, birthDay: String): Flowable<ResultData<JsonObject>> {
        val replace = nickName.replace("%", "%25").replace("&", "%26")
                .replace("+", "%2B").replace("=", "2D")
                .replace("#", "%23")
        val request = ParamsBuilder.create().append("server", Api.UPDATE_USER_INFO)
                .append("imgUrl", imgUrl).append("id", id).append("nickName", replace)
                .append("sex", sex).append("birthDay", birthDay)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 线路类型
     */
    fun getLineTypes(): Flowable<ResultData<ArrayList<com.hbcx.user.beans.LineType>>> {
        val request = ParamsBuilder.create().append("server", Api.LINE_TYPE_LIST)
        return request().lineTypeList(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 线路列表
     */
    fun getLineList(page: Int, lineTypeId: Int, openCityId: Int): Flowable<ResultData<ArrayList<com.hbcx.user.beans.TicketLine>>> {
        val request = ParamsBuilder.create().append("server", Api.LINE_LIST)
                .append("page", page).append("lineTypeId", lineTypeId).append("openCityId", openCityId).append("rows", PAGE_SIZE)
        return request().lineList(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 票务班次列表
     */
    fun getTicketLineList(page: Int, startCityCode: String, endCityCode: String, lon: Double, lat: Double, days: String,startStationId:Int,endStationId:Int,lineType:Int): Flowable<ResultData<ArrayList<com.hbcx.user.beans.TicketList>>> {
        val request = ParamsBuilder.create().append("server", Api.NEW_LINE_LIST)
                .append("page", page).append("startCityCode", startCityCode).append("endCityCode", endCityCode).append("rows", 5)
                .append("lon", lon).append("lat", lat).append("days", days).append("startStationId",
                        startStationId).append("endStationId", endStationId).append("lineType", lineType)
        return request().ticketLineList(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 票务班线详情
     */
    fun getTicketLineDeatail(startCityCode: String, endCityCode: String, lon: Double, lat: Double, days: String, id: Int, startPointId: Int, endPointId: Int): Flowable<ResultData<com.hbcx.user.beans.TicketLineDetail>> {
        val request = ParamsBuilder.create().append("server", Api.TICKETLINE_DETAIL)
                .append("id", id).append("startCityCode", startCityCode).append("endCityCode", endCityCode)
                .append("lon", lon).append("lat", lat).append("days", days).append("startPointId", startPointId).append("endPointId", endPointId)
        return request().getLineDetail(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 票务专线详情
     */
    fun getSpecialLineDeatail(page: Int, openCityId: Int, lon: Double, lat: Double, days: String, id: Int): Flowable<ResultData<com.hbcx.user.beans.LineDetail>> {
        val request = ParamsBuilder.create().append("server", Api.LINE_DETAIL)
                .append("id", id).append("page", page).append("openCityId", openCityId).append("rows", PAGE_SIZE)
                .append("lon", lon).append("lat", lat).append("days", days)
        return request().getSpecialLineDetail(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 可预约天数
     */
    fun getEnableDayCount(): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.ENABLE_DAY)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 下单界面数据获取
     */
    fun getTicketOrderData(userId: Int, money: Double): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.ORDER_DATA)
                .append("money", money).append("userId", userId)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 订单详情
     */
    fun getTicketOrder(id: Int): Flowable<ResultData<com.hbcx.user.beans.TicketOrder>> {
        val request = ParamsBuilder.create().append("server", Api.TICKET_ORDER_DETAIL)
                .append("id", id)
        return request().getTicketOrderDetail(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 订单列表
     * @param type    票务首页传1
     */
    fun getTicketOrderList(id: Int, page: Int, type: Int): Flowable<ResultData<ArrayList<com.hbcx.user.beans.TicketOrder>>> {
        val request = ParamsBuilder.create().append("server", Api.TICKET_ORDER_LIST)
                .append("id", id).append("page", page).append("type", type).append("rows", PAGE_SIZE)
        return request().getTicketOrderList(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 删除票务订单
     */
    fun deleteTicketOrder(id: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.TICKET_ORDER_DELETE)
                .append("id", id)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 取消票务订单
     */
    fun cancelTicketOrder(id: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.TICKET_ORDER_CANCEL)
                .append("id", id)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 退票金额
     */
    fun getCancelMoney(id: Int, num: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.TICKET_CANCEL_MONEY)
                .append("id", id).append("num", num)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 退票
     */
    fun refundTicket(id: Int, ridingIds: String): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.TICKET_REFUND)
                .append("id", id).append("ridingIds", ridingIds)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 评价
     */
    fun evaluateTicket(orderId: Int, userId: Int, hygiene: Int, facilities: Int, punctuality: Int,
                       serviceScore: Int, attitudeScore: Int, content: String): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.TICKET_EVALUATE)
                .append("orderId", orderId).append("userId", userId).append("hygiene", hygiene).append("facilities", facilities)
                .append("punctuality", punctuality).append("serviceScore", serviceScore).append("attitudeScore", attitudeScore)
                .append("content", content)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 邀请情况
     */
    fun getInviteData(userId: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.INVITE_DATA)
                .append("userId", userId)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 邀请记录
     */
    fun getInviteRecord(userId: Int, page: Int): Flowable<ResultData<ArrayList<com.hbcx.user.beans.InviteRecord>>> {
        val request = ParamsBuilder.create().append("server", Api.INVITE_RECORD)
                .append("userId", userId).append("page", page).append("rows", PAGE_SIZE)
        return request().getInviteRecord(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 获取服务热线
     */
    fun getServicePhone(): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.GET_SERVICE)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 出发站点
     */
    fun getStartStation(cityCode: String): Flowable<ResultData<ArrayList<Region>>> {
        val request = ParamsBuilder.create().append("server", Api.START_STATION).append("cityCode", cityCode)
        return request().getStaions(request.build(encodeDES)).defaultScheduler()
    }
    /**
     * 出发站搜索
     */
    fun searchStartStation(cityCode: String,stationName:String,page:Int): Flowable<ResultData<ArrayList<Station>>> {
        val request = ParamsBuilder.create().append("server", Api.START_STATION_SEARCH).append("cityCode", cityCode)
                .append("stationName",stationName).append("page",page).append("rows", PAGE_SIZE)
        return request().getStations(request.build(encodeDES)).defaultScheduler()
    }
    /**
     * 到达站搜索
     */
    fun searchEndStation(cityCode: String,stationName:String,page:Int,lineType:Int,stationId:Int): Flowable<ResultData<ArrayList<Station>>> {
        val request = ParamsBuilder.create().append("server", Api.END_STATION_SEARCH).append("cityCode", cityCode)
                .append("stationName",stationName).append("page",page).append("rows", PAGE_SIZE).append("lineType",lineType).append("stationId", stationId)
        return request().getStations(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 到达站点
     */
    fun getEndStation(cityCode: String,lineType:Int,stationId:Int): Flowable<ResultData<ArrayList<Region>>> {
        val request = ParamsBuilder.create().append("server", Api.END_STATION).append("cityCode", cityCode)
                .append("lineType",lineType).append("stationId",stationId)
        return request().getStaions(request.build(encodeDES)).defaultScheduler()
    }

    /**
     * 验证身份证
     */
    fun checkIdCard(name: String,num:String): Flowable<JsonObject> {
        return request().checkIdCard(Api.JUHE_URL, Const.JUHE_KEY, num,name).defaultScheduler()
    }


    /**
     * 验证身份证
     */
    fun deleteTicket(id: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.DELETE_TICKET)
                .append("id", id)
        return HttpManager.request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

    fun addTicket(id: Int, number: Int): Flowable<ResultData<JsonObject>> {
        val request = ParamsBuilder.create().append("server", Api.ADD_TICKET)
                .append("id", id).append("number", number)
        return request().simpleRequest(request.build(encodeDES)).defaultScheduler()
    }

}

