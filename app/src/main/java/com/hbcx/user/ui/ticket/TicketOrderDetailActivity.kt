package com.hbcx.user.ui.ticket

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.CountDownTimer
import android.view.LayoutInflater
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.hideIdCard
import cn.sinata.xldutils.utils.toTime
import cn.sinata.xldutils.utils.toWeek
import cn.sinata.xldutils.visible
import com.amap.api.col.sln3.fm
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.hbcx.user.R
import com.hbcx.user.dialogs.CodeDialog
import com.hbcx.user.dialogs.TicketPriceDialog
import com.hbcx.user.dialogs.TipDialog
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.MainActivity
import com.hbcx.user.ui.PayActivity
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.request
import com.uuzuche.lib_zxing.activity.CodeUtils
import kotlinx.android.synthetic.main.activity_ticket_order_detail.*
import kotlinx.android.synthetic.main.item_passenger_info.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class TicketOrderDetailActivity : TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.activity_ticket_order_detail

    private val isCreate by lazy {
        //下单页面过来
        intent.getIntExtra("isCreate", 0)
    }
    private val id by lazy {
        intent.getIntExtra("id", 0)
    }

    private lateinit var order: com.hbcx.user.beans.TicketOrder

    override fun initClick() {
        tv_negative.onClick {
            when (tv_negative.text) {
                "申请退票" -> startActivityForResult<CancelTicketActivity>(1, "id" to id)
                "取消订单" -> {
                    val tipDialog = TipDialog()
                    tipDialog.arguments = bundleOf("msg" to "是否取消该订单", "cancel" to "取消", "ok" to "确定")
                    tipDialog.setDialogListener { p, s ->
                        HttpManager.cancelTicketOrder(id).request(this@TicketOrderDetailActivity) { _, _ ->
                            toast("取消成功")
                            setResult(Activity.RESULT_OK)
                            showDialog()
                            getData()
                        }
                    }
                    tipDialog.show(supportFragmentManager, "cancel")
                }
                "删除订单" -> {
                    val tipDialog = TipDialog()
                    tipDialog.arguments = bundleOf("msg" to "是否删除该订单", "cancel" to "取消", "ok" to "确定")
                    tipDialog.setDialogListener { p, s ->
                        HttpManager.deleteTicketOrder(id).request(this@TicketOrderDetailActivity) { _, _ ->
                            toast("删除成功")
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }
                    tipDialog.show(supportFragmentManager, "cancel")
                }
            }
        }
        tv_positive.onClick {
            when (tv_positive.text) {
                "立即支付" -> {
                    startActivityForResult<PayActivity>(2, "money" to order.payMoney, "id" to order.id, "time" to order.createTime, "type" to 2)
                }
                "立即评价" -> {
                    startActivityForResult<EvaluateActivity>(1, "id" to id)
                }
            }
        }
    }

    override fun initView() {
        title = "订单详情"
        showDialog(canCancel = false)
        getData()
        tv_negative.visible()
    }

    private fun getData() {
        HttpManager.getTicketOrder(id).request(this) { _, data ->
            data?.let {
                order = it
                tv_state.text = it.getStatusStr()
                tv_state.textColorResource = it.getStatusColorRes()
                tv_start_address.text = it.pointUpName
                tv_end_address.text = it.pointDownName
                tv_start_city.text = it.pointUpCityName
                tv_end_city.text = it.pointDownCityName
                tv_order_num.text = it.orderNum
                tv_time.text = String.format("%s %s %s上车", it.rideDate.toTime("MM月dd日"), it.rideDate.toWeek(), it.rideDate.toTime("HH:mm"))
                if (it.endTime != null && it.endTime.isNotEmpty() && it.endTime != "null")
                    tv_arrive_time.text = String.format("预计 %s到达", it.endTime)
                tv_order_time.text = it.createTime.toTime("yyyy-MM-dd HH:hh")
                tv_phone.text = it.phone
                tv_money.text = String.format("￥%.2f", it.payMoney)
                ll_passenger.removeAllViews()
                it.passengerList.forEach { it2 ->
                    val view = LayoutInflater.from(this).inflate(R.layout.item_passenger_info, null)
                    view.tv_name.text = it2.name
                    view.tv_id_card.text = it2.idCard.hideIdCard()
                    if (it2.status == 2) { //已退票
                        view.tv_canceled.visible()
                        view.iv_ticket_code.gone()
                    } else {//正常情况下
                        if (it.status != 2) { //不为待乘车都隐藏
                            view.iv_ticket_code.gone()
                        } else
                            view.iv_ticket_code.setOnClickListener {
                                val codeDialog = CodeDialog()
                                codeDialog.arguments = bundleOf("code_img" to createBarcode(if (it2.elTicket==null) "YunYou:"+it.id.toString()+"0" else "YunYou:"+it2.elTicket+"0", dip(400), dip(150)),"code_str" to if (it2.elTicket==null) it.id.toString()+"0" else it2.elTicket+"0")
                                codeDialog.show(supportFragmentManager, "code")
                            }
                    }
                    ll_passenger.addView(view)
                }
                tv_positive.visibility = it.getPositiveVisibility()
                if (it.status == 2) tv_positive.gone()  //特殊处理 详情页不需要验票码按钮
                tv_positive.text = it.getPositiveBtnStr()
                tv_negative.text = it.getNegativeBtnStr()
                if (it.status == 1) { //待付款：隐藏二维码，显示计时器
                    tv_deadline.visible()
                    val time = it.createTime + 5 * 60 * 1000 - System.currentTimeMillis()
                    if (time <= 0) {
                        toast("订单已超时，请重新下单")
                    } else
                        object : CountDownTimer(time, 1000) {
                            override fun onFinish() {
                                if (!isDestroy) {
                                    setResult(Activity.RESULT_OK)
                                    toast("订单已超时，请重新下单")
                                    finish()
                                }
                            }

                            override fun onTick(millisUntilFinished: Long) {
                                tv_deadline.text = "请在${millisUntilFinished.toTime("mm分ss秒")}内完成支付，逾期将自动取消预订！"
                            }
                        }.start()
                    rl_code.gone()
                } else if (it.status == 7 || it.status == 5) { //已取消,隐藏二维码 计时器
                    tv_deadline.gone()
                    rl_code.gone()
                } else {
                    tv_deadline.gone()
                    rl_code.visible()
                    iv_qr_code.setImageBitmap(CodeUtils.createImage("YunYou:$id"+"1", dip(136), dip(136), null))
                    tv_num.text = "验票码：${it.orderNum.substring(7)}"+"1"
                    if (it.status in (3..6)) {
                        iv_overlay.visible()
                        tv_num.textColorResource = R.color.grey_text
                        if (it.status in (3..4))
                            iv_overlay.imageResource = R.mipmap.used
                        else
                            iv_overlay.imageResource = R.mipmap.expired
                    }
                }
                iv_detail.setOnClickListener { _ ->
                    val ticketPriceDialog = TicketPriceDialog()
                    ticketPriceDialog.arguments = bundleOf("order" to it)
                    ticketPriceDialog.show(supportFragmentManager, "price")
                }
            }
        }
    }

    override fun onBackPressed() {
        if (isCreate == 1) {
            startActivity<MainActivity>()
        } else
            super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            showDialog(canCancel = false)
            setResult(Activity.RESULT_OK)
            getData()
        }
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) { //支付成功
            startActivity<TicketPaySuccessActivity>("id" to id, "orderNum" to order.orderNum)
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    //生成条码
    private fun createBarcode(contents: String, desiredWidth: Int, desiredHeight: Int): Bitmap? {
        var resultBitmap: Bitmap? = null
        val barcodeFormat = BarcodeFormat.CODE_128

        resultBitmap = encodeAsBitmap(contents, barcodeFormat,
                desiredWidth, desiredHeight)

        return resultBitmap
    }


    private fun encodeAsBitmap(contents: String,
                               format: BarcodeFormat, desiredWidth: Int, desiredHeight: Int): Bitmap {
        val WHITE = -0x1
        val BLACK = -0x1000000

        val writer = MultiFormatWriter()
        var result: BitMatrix? = null
        try {
            result = writer.encode(contents, format, desiredWidth,
                    desiredHeight, null)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        val width = result!!.width
        val height = result.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (result.get(x, y)) BLACK else WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return drawText(bitmap,contents)
    }

    private fun drawText(bitmap: Bitmap,content: String):Bitmap{
        val id = content.substring(7)
        val paint = Paint()
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL//设置填充样式
        paint.textSize = sp(12).toFloat()
//        paint.setTextAlign(Paint.Align.CENTER);
        val fm = paint.fontMetrics;
        //测量字符串的宽度
        val textWidth = paint.measureText(id);
        //绘制字符串矩形区域的高度
        val textHeight = (fm.bottom - fm.top).toInt()
        // x 轴的缩放比率
        val scaleRateX = bitmap.width / (textWidth+10)
        paint.textScaleX = scaleRateX
        //绘制文本的基线
        val baseLine = bitmap.height + textHeight
        //创建一个图层，然后在这个图层上绘制bCBitmap、content
        val result = Bitmap.createBitmap(bitmap.width,bitmap.height + 2 * textHeight,Bitmap.Config.ARGB_4444);
        val canvas = Canvas()
        canvas.drawColor(Color.WHITE);
        canvas.setBitmap(result);
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.drawText(id,0f,baseLine.toFloat(),paint)
        canvas.save()
        canvas.restore()
        return result
    }

}