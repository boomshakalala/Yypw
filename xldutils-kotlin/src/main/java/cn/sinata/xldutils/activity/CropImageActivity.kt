package cn.sinata.xldutils.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import cn.sinata.xldutils.R
import cn.sinata.xldutils.defaultScheduler
import cn.sinata.xldutils.ioScheduler
import cn.sinata.xldutils.utils.BitmapUtils
import cn.sinata.xldutils.utils.getSDFreeSize
import cn.sinata.xldutils.view.ClipImageBorderView
import cn.sinata.xldutils.view.ClipZoomImageView
import cn.sinata.xldutils.xldUtils
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import org.jetbrains.anko.*
import org.reactivestreams.Subscription
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CropImageActivity : TitleActivity() {

    private val mZoomImageView by lazy {
        ClipZoomImageView(this)
    }

    var bitmap: Bitmap? = null
    var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_image)
        title = "裁剪图片"
        titleBar.addRightButton("确定", onClickListener = View.OnClickListener {
            if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
                toast("未检测到SD卡！请安装后重试！")
                return@OnClickListener
            }
            //如果sd卡剩余容量低于10m
            if (getSDFreeSize() < 10) {
                toast("SD卡剩余空间太小！")
                return@OnClickListener
            }
            showDialog("图片处理中...")
            url = xldUtils.PICDIR + System.currentTimeMillis() + ".jpg"
            Flowable.just(mZoomImageView.clip())
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        saveBitmapToSDCard(it)
                        Flowable.just(url)
                    }.observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : FlowableSubscriber<String>{
                        override fun onSubscribe(s: Subscription) {
                            s.request(1)
                        }

                        override fun onComplete() {
                        }

                        override fun onError(t: Throwable?) {
                            toast("处理出错！")
                            dismissDialog()
                        }

                        override fun onNext(t: String?) {
                            dismissDialog()
                            val intent = Intent()
                            intent.putExtra("path", url)
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                    })
        })
        val rl_content = find<RelativeLayout>(R.id.rl_content)
        val mClipImageView = ClipImageBorderView(this)
        val scale = intent.getFloatExtra("scale", 1f)
        val mode = intent.getIntExtra("mode", 0)
        if (mode == 1) {
            mClipImageView.setMode(ClipImageBorderView.Mode.Circle)
        }
        val lp = ViewGroup.LayoutParams(matchParent, matchParent)
        rl_content.addView(mClipImageView, lp)
        rl_content.addView(mZoomImageView, 0, lp)
        val mHorizontalPadding = if (scale < 1f) {
            dip(5)
        } else {
            dip(10)
        }
        mZoomImageView.setHorizontalPadding(mHorizontalPadding)
        mClipImageView.setHorizontalPadding(mHorizontalPadding)
        mZoomImageView.setImageScale(scale)
        mClipImageView.setImageScale(scale)
        val uri = intent.getStringExtra("uri")
        if (uri != null) {
            showDialog("图片加载中...")
            Flowable.just("").ioScheduler().flatMap {
                bitmap = BitmapUtils.decodeBitmapFromPath(this, uri)
                Flowable.just(bitmap)
            }.defaultScheduler().subscribe(object : DisposableSubscriber<Bitmap>(){
                override fun onComplete() {

                }

                override fun onNext(t: Bitmap?) {
                    dismissDialog()
                    if (t == null) {
                        toast("无法打开图片，请检查是否开启读取权限！")
                    }
                    mZoomImageView.imageBitmap = t
                }

                override fun onError(t: Throwable?) {
                    dismissDialog()
                    toast("图片打开失败，请检查是否开启读取权限！")
                }
            })
        }

    }

    private fun saveBitmapToSDCard(bmp: Bitmap) {

        val path = xldUtils.PICDIR
        var file = File(path)
        if (!file.exists()) {

            file.mkdirs()
        }
        file = File(url)
        try {
            val out = FileOutputStream(file)
            if (bmp.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush()
                out.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (bitmap != null) {
            bitmap?.recycle()
        }
    }
}
