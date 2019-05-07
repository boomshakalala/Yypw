package cn.sinata.xldutils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.app.Fragment
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import cn.sinata.xldutils.utils.toImageUri
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.toast
import java.io.File
import java.util.*

/**
 * 打印公司日志
 */
fun Any?.sysErr(msg:Any?){
    if (xldUtils.DEBUG)
        Log.e("sinata","--------"+msg)
}

fun <T> Flowable<T>.ioScheduler(): Flowable<T> = this.subscribeOn(Schedulers.io())
fun <T> Flowable<T>.defaultScheduler(): Flowable<T> = this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
fun <T> Observable<T>.defaultScheduler(): Observable<T> = this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun Activity.callPhone(phone:String?){
//    val isAllow = permission(Manifest.permission.CALL_PHONE,"拨号权限",15)
    RxPermissions(this).request(Manifest.permission.CALL_PHONE).subscribe {
        if (it) {
            val p = phone ?: ""
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + p)
            this.startActivity(intent)
        } else {
            toast("没有拨号权限")
        }
    }
}

fun Fragment.callPhone(phone:String?){
    activity?.callPhone(phone)
}

fun View?.visible(){
    this?.let {
        if (it.visibility != View.VISIBLE) {
            it.visibility = View.VISIBLE
        }
    }
}

fun View?.gone(){
    this?.let {
        if (it.visibility != View.GONE) {
            it.visibility = View.GONE
        }
    }
}
fun View?.invisible(){
    this?.let {
        if (it.visibility != View.INVISIBLE) {
            it.visibility = View.INVISIBLE
        }
    }
}

fun File?.suffix():String {
    if (this == null) {
        return ""
    }
    if (!this.isFile) {
        return ""
    }
    val fileName = this.name
    return if (fileName.contains(".")) {
        fileName.substring(fileName.lastIndexOf(".") + 1)
    } else ""
}

inline fun Any.downLoadByFresco(url: String?, crossinline onFinish:(bitmap: Bitmap?)->Unit){
    val imagePipeline = Fresco.getImagePipeline()
    val imageRequest = ImageRequestBuilder.newBuilderWithSource(url.toImageUri()).build()
    val dataSource1 = imagePipeline.fetchDecodedImage(imageRequest, null)
    dataSource1.subscribe(object : BaseBitmapDataSubscriber() {
        override fun onNewResultImpl(bitmap: Bitmap?) {
            onFinish(bitmap)
        }
        override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
            onFinish(null)
        }
    }, CallerThreadExecutor.getInstance())
}

fun Context.getUUID(): String {
    try {
        val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val tmDevice: String
        val tmSerial: String
        val androidId: String = "" + android.provider.Settings.Secure.getString(this.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        tmDevice = "" + tm.deviceId
        tmSerial = "" + tm.simSerialNumber
        val deviceUuid = UUID(androidId.hashCode().toLong(), tmDevice.hashCode().toLong() shl 32 or tmSerial.hashCode().toLong())
        return deviceUuid.toString()
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
    return ""
}
