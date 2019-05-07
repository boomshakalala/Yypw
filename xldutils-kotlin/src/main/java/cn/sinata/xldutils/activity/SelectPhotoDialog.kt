package cn.sinata.xldutils.activity

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.text.TextUtils
import android.view.Gravity
import android.widget.Button
import cn.sinata.xldutils.R
import cn.sinata.xldutils.utils.alertDialog
import cn.sinata.xldutils.utils.getUrlPath
import cn.sinata.xldutils.xldUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import java.io.File
import java.util.*

class SelectPhotoDialog : DialogActivity() {

    private var tempFile: File? = null

    companion object {
        val PATH = "path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setShowSOP(false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_photo_dialog)
        window.setGravity(Gravity.BOTTOM)
        //请求权限
//        permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, "访问本地存储", 12)
        //拍照
        find<Button>(android.R.id.button1).onClick {
            RxPermissions(this@SelectPhotoDialog).request(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe {
                if (it) {//有权限
                    //检测路径是否存在，不存在就创建
                    xldUtils.initFilePath()
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    val fileName = System.currentTimeMillis().toString() + ".jpg"
                    tempFile = File(xldUtils.PICDIR, fileName)
                    val u = Uri.fromFile(tempFile)
                    intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0)
                    //7.0崩溃问题
                    if (Build.VERSION.SDK_INT < 24) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, u)
                    } else {
                        val contentValues = ContentValues(1)
                        contentValues.put(MediaStore.Images.Media.DATA, tempFile?.absolutePath)
                        val uri = this@SelectPhotoDialog.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    }
                    startActivityForResult(intent, 0)
                } else {
                    toast("没有访问设备相机权限")
                }
            }

        }
        //选择相册
        find<Button>(android.R.id.button2).onClick {
            RxPermissions(this@SelectPhotoDialog).request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe {
                if (it) {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)// 调用android的图库
                    intent.type = "image/*"
                    startActivityForResult(intent, 1)
                }else{
                    toast("没有访问设备存储权限")
                }
            }
        }
        //取消
        find<Button>(android.R.id.button3).onClick {
            onBackPressed()
        }
    }

    override fun exitAnim(): Int {
        return R.anim.popup_out
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 12) {
            if (TextUtils.equals(permissions[0], Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                //用户不同意，向用户展示该权限作用
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    alertDialog("请注意", "本应用需要使用访问本地存储权限，否则无法正常使用！", false, "确定", "取消", DialogInterface.OnClickListener { _, _ -> finish() }, DialogInterface.OnClickListener { _, _ -> finish() })
                    return
                }
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                0 -> {
                    //拍照
                    if (tempFile != null && tempFile!!.exists()) {
                        val intent = Intent()
                        intent.putExtra(PATH, tempFile!!.absolutePath)
                        setResult(RESULT_OK, intent)
                        onBackPressed()
                        overridePendingTransition(0, 0)
                    }
                }
                1 -> {
                    if (data != null) {
                        val uri = data.data
                        if (uri != null) {
                            val path = getUrlPath(uri)
                            if (path != null) {
                                val typeIndex = path.lastIndexOf(".")
                                if (typeIndex != -1) {
                                    val fileType = path.substring(typeIndex + 1).toLowerCase(Locale.CHINA)
                                    //某些设备选择图片是可以选择一些非图片的文件。然后发送出去或出错。这里简单的通过匹配后缀名来判断是否是图片文件
                                    //如果是图片文件则发送。反之给出提示
                                    if (fileType == "jpg" || fileType == "gif"
                                            || fileType == "png" || fileType == "jpeg"
                                            || fileType == "bmp" || fileType == "wbmp"
                                            || fileType == "ico" || fileType == "jpe") {
                                        val intent = Intent()
                                        intent.putExtra(PATH, path)
                                        setResult(RESULT_OK, intent)
                                        finish()
                                        overridePendingTransition(0, 0)
                                        //			                        	cropImage(path);
                                        //			                        	BitmapUtil.getInstance(this).loadImage(iv_image, path);
                                    } else {
                                        toast("无法识别的图片类型！")
                                    }
                                } else {
                                    toast("无法识别的图片类型！")
                                }
                            } else {
                                toast("无法识别的图片类型或路径！")
                            }
                        } else {
                            toast("无法识别的图片类型！")
                        }
                    }
                }
            }
        }
    }
}
