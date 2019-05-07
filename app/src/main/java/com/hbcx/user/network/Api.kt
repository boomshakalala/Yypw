package com.hbcx.user.network

object Api {
//    const val BASE_URL = "http://192.168.1.152:8080/YunYou/" //胡瑶内网
    const val BASE_URL = "http://106.116.167.23/YunYou/" //外网地址
    const val URL = "$BASE_URL/app/public/advertInfo?url="
    const val BASE_RULE = BASE_URL + "app/public/getAppText?type="
    const val NOTICE_INFO = BASE_URL + "app/public/noticeInfo?url="
    const val URL2 = "$BASE_URL/app/public/bannerInfo?url="
    const val INFO_URL = BASE_URL + "sys/apptext/getAppText?type=%s"
    const val SHARE_URL = "http://106.116.167.23/user.html?userId="
    const val OSS_END_POINT = "http://oss-cn-beijing.aliyuncs.com"
    const val JUHE_URL = "http://op.juhe.cn/idcard/query" //聚合身份证校验

    /**
     * H5部分
     */
    const val RULE = "${BASE_RULE}10" //用户协议
    const val PLATFORM_RULE = "${BASE_RULE}10" //平台协议
    const val PRICE_RULE = "${BASE_RULE}8" //计费规则
    const val ABOUT = "${BASE_RULE}9" //关于我们
    const val RENT_RULE = "${BASE_RULE}4" //租车预定条款
    const val CANCEL_RULE = "${BASE_RULE}3" //退票须知
    const val RENT_SAFE_RULE = "${BASE_RULE}7" //租车保险须知
    const val TICKET_SAFE_RULE = "${BASE_RULE}6" //票务保险须知
    const val GROUP_RENT_RULE = "${BASE_RULE}5" //包车协议
    const val COMPANY_DESCRIBE = "$BASE_URL/app/public/getConpanInfoText?id=" //商家描述
    const val LINE_DESCRIBE = "${BASE_URL}app/public/getDestinationInfoText?url=" //线路详情
    const val SIGN = "${BASE_URL}app/carChartered/contract?id=" //包车签约


    //    const val SOCKET_SERVER = "192.168.3.228"
    const val SOCKET_SERVER = "106.116.167.23"
    const val SOCKET_PORT = 8888

    const val SEND_MSM = "/app/public/sendSms"
    const val GET_AD = "/app/public/getAdvert"
    const val GET_RESERVE_TIME = "/app/fastCar/getReserveTime"
    const val GET_BANNER = "/app/public/getBanner"
    const val LOGIN = "/app/user/login"
    const val FORGET = "/app/user/forgetPassword"
    const val MODIFY_PHONE = "/app/user/upPhone"
    const val CHECK_CODE = "/app/public/checkSms"
    const val SET_PWD = "/app/user/updatePassword"
    const val NEARBY_DRIVER = "/app/fastCar/getNearbyDriver"
    const val GET_CALCULATION = "/app/fastCar/costCalculation"
    const val CALL_ORDER = "/app/fastCar/sendOrder"
    const val CANCEL_ORDER = "/app/fastCar/cancleOrderByUser"
    const val COMPLAINT = "/app/fastCar/addComplaints"
    const val DRIVER_HOME = "/app/fastCar/getDriverInfo"
    const val ORDER_DETAIL = "/app/fastCar/getUserOrderDetail"
    const val DO_EVALUATE = "/app/fastCar/addComments"
    const val FAST_ORDER_LIST = "/app/fastCar/getUserOrderList"  //快专车订单列表
    const val GET_PAY_INFO = "/app/payInfo/getPayInfo"  //支付信息
    const val GET_OPEN_CITY = "/app/public/getOpenCity"  //获取开通城市


    /**租车**/
    const val RENT_MAIN = "/app/carRenting/getCarRentingFirstPageInfo"
    const val CAR_LIST = "/app/carRenting/getCarList"
    const val DELETE_RENT_ORDER = "/app/carRenting/delOrder"
    const val CAR_LABELS = "/app/carRenting/getRentingCarLabelList"
    const val CAR_BRANDS = "/app/carRenting/getCarBrandList"
    const val CAR_DETAIL = "/app/carRenting/getCarDetail"
    const val CAR_SAFE = "/app/carRenting/getRentingInsurance"
    const val COMPANY_INFO = "/app/carRenting/getComPanyInfo"
    const val GET_COMPANY_POINT = "/app/carRenting/getComPanyPoint"
    const val ADD_DRIVER = "/app/public/addRidingInfor"
    const val GET_DRIVER = "/app/public/getUserRidingInfor"
    const val RENT_CREATE_ORDER = "/app/carRenting/createOrder"
    const val RENT_ORDER = "/app/carRenting/getOrderDetail"
    const val CANCEL_RENT_ORDER = "/app/carRenting/cancleOrder"
    const val RENT_ORDER_LIST = "/app/carRenting/getOrderList"


    /**包车**/
    const val GROUP_RENT_MAIN = "/app/carChartered/getCarCharteredFirstPageInfo"
    const val GET_DISTANCE = "/app/carChartered/computationalDistance"
    const val CANCEL_GROUP_ORDER = "/app/carChartered/cancleOrder"
    const val DELETE_GROUP_ORDER = "/app/carChartered/delOrder"
    const val GROUP_CAR_LIST = "/app/carChartered/getCarList"
    const val GROUP_CAR_DETAIL = "/app/carChartered/getCarDetail"
    const val GROUP_CREATE_ORDER = "/app/carChartered/createOrder"
    const val GROUP_RENT_ORDER = "/app/carChartered/getOrderDetail"
    const val GROUP_RENT_ORDER_LIST = "/app/carChartered/getOrderList"
    const val MAKE_SIGN = "/app/carChartered/userContract"

    /**票务**/
    const val GET_END_CITY = "/app/ticketing/getDestinationCity"
    const val LINE_TYPE_LIST = "/app/ticketing/getLineTypeList"
    const val LINE_LIST = "/app/ticketing/getDedicatedLineList"  //专线列表
    const val TICKET_LINE_LIST = "/app/ticketing/getTicketLineList" //票务班次列表
    const val ENABLE_DAY = "/app/ticketing/getUserCanBuyDay" //可预约天数
    const val TICKETLINE_DETAIL = "/app/ticketing/getTicketLineDetail" //班线详情
    const val LINE_DETAIL = "/app/ticketing/getDedicatedDetail" //专线详情
    const val ORDER_DATA = "/app/ticketing/getOrderPageInfo" //下单界面数据
    const val CREATE_TICKET_ORDER = "/app/ticketing/createOrder" //票务下单
    const val GET_PASSENGER_LIST = "/app/ticketing/getPassengerList" //订单乘坐人集合
    const val TICKET_ORDER_DETAIL = "/app/ticketing/getOrderDetail"
    const val TICKET_ORDER_LIST = "/app/ticketing/getUserOrderList"
    const val TICKET_ORDER_DELETE = "/app/ticketing/delOrder"
    const val TICKET_ORDER_CANCEL = "/app/ticketing/cancleOrder"
    const val TICKET_CANCEL_MONEY = "/app/ticketing/getRefundMoney"
    const val TICKET_REFUND = "/app/ticketing/refundTicket" //退票
    const val TICKET_EVALUATE = "/app/ticketing/addEvaluate" //评价
    const val INVITE_DATA = "/app/user/getInviteData" //邀请情况
    const val INVITE_RECORD = "/app/user/getInviteRecordList" //邀请记录
    const val START_STATION = "/app/newTicketing/getStationList" //票务出发地
    const val END_STATION = "/app/newTicketing/getEndStationList" //票务目的地
    const val START_STATION_SEARCH = "/app/newTicketing/getSearchStationList" //票务出发地搜索
    const val END_STATION_SEARCH = "/app/newTicketing/getSearchEndStationList" //票务目的地搜索
    const val NEW_LINE_LIST = "/app/newTicketing/getTicketLineList" //票务班次列表新接口


    /**用户**/
    const val COUPONS = "/app/user/getUserCoupon"
    const val DEL_COUPON = "/app/user/delUserCoupon"
    const val COUPONS_NUM = "/app/public/getUserCanUseCouponNum"
    const val FEEDBACK = "/app/user/addFeedBack"
    const val INTEGRAL = "/app/user/getUserIntegralRecord"
    const val UPDATE_USER_INFO = "/app/user/updateUserInfo"
    const val IS_CITY_OPEN = "/app/public/cityIsOpen"
    const val HAS_NEW_MSG = "/app/public/getIsMess"
    const val CLEAR_MSG = "/app/public/clearMessAll"
    const val GET_MSG_LIST = "/app/public/getMessList"
    const val GET_SERVICE = "/app/public/getServerPhone"
}