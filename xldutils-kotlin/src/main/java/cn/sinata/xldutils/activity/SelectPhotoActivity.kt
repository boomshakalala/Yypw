package cn.sinata.xldutils.activity

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.sinata.xldutils.R
import cn.sinata.xldutils.adapter.BaseRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.utils.isFilePath
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import com.facebook.drawee.view.SimpleDraweeView
import com.tbruyelle.rxpermissions2.RxPermissions
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 选择照片，可多选。(单选还么写。。)
 */
class SelectPhotoActivity : RecyclerActivity() {

    companion object {
        const val MAX = "max"
        const val SELECT_LIST = "selectList"
    }

    private var tempFile: File? = null
    var maxNumber = 100//默认最多可选100个
    val imagePaths = ArrayList<Image>()
    val selectImages = ArrayList<String>()
    val LOADER_ALL: Int = 0
    val defaultTakePhoto = Image("res:///" + R.drawable.ic_camera_alt_24dp)
    private val imageAdapter by lazy {
        ImageAdapter()
    }

    override fun adapter(): RecyclerView.Adapter<*> {
        return imageAdapter
    }

    override fun mode(): SwipeRefreshRecyclerLayout.Mode {
        return SwipeRefreshRecyclerLayout.Mode.None
    }

    override fun layoutManager(): RecyclerView.LayoutManager {
        return GridLayoutManager(this, 3)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        permission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.CAMERA)
//
//                ,"运行必须的权限",12)
        RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA).subscribe { }

        titleBar.addRightButton("完成", onClickListener = View.OnClickListener {
            if (selectImages.size == 0) {
                toast("您还没选择图片！")
                return@OnClickListener
            }
            val data = Intent()
            data.putStringArrayListExtra(SELECT_LIST, selectImages)
            setResult(Activity.RESULT_OK, data)
            finish()
        })

        maxNumber = intent.getIntExtra(MAX, 100)
        val temp = intent.getStringArrayListExtra(SELECT_LIST)
        if (temp != null) {
            selectImages.addAll(temp)
        }
        //拍照按钮
        imagePaths.add(defaultTakePhoto)
        imageAdapter.notifyDataSetChanged()

        supportLoaderManager.initLoader<Cursor>(LOADER_ALL, null, mLoaderCallback)
    }

    private val mLoaderCallback = object : LoaderManager.LoaderCallbacks<Cursor> {
        private val IMAGE_PROJECTION = arrayOf(MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media._ID)

        override fun onLoaderReset(loader: Loader<Cursor>) {

        }

        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
            if (data != null) {
                if (data.count > 0) {
                    data.moveToFirst()
                    imagePaths.clear()
                    imagePaths.add(defaultTakePhoto)
                    do {
                        val path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]))
                        val name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]))
                        if (path.isFilePath()) {
                            val image = Image(path)
                            if (selectImages.contains(path)) {
                                image.isSelected = true
                            }
                            imagePaths.add(image)
                        }
                    } while (data.moveToNext())
                    imageAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            return CursorLoader(this@SelectPhotoActivity,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    IMAGE_PROJECTION[4] + ">0 AND " + IMAGE_PROJECTION[3] + "=? OR " + IMAGE_PROJECTION[3] + "=? ",
                    arrayOf("image/jpeg", "image/png"), IMAGE_PROJECTION[2] + " DESC")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0) {
                //拍照
                if (tempFile != null && tempFile!!.exists()) {
                    //notify system
                    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tempFile)))
                    //重新加载数据
                    supportLoaderManager.initLoader<Cursor>(LOADER_ALL, null, mLoaderCallback)
                }
            }
        }
    }

    private inner class ImageAdapter : BaseRecyclerAdapter<Image>(imagePaths, R.layout.item_select_photo_image) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val viewHolder = super.onCreateViewHolder(parent, viewType)

            return viewHolder
        }

        override fun onBind(holder: ViewHolder, position: Int, data: Image) {
            val stateView = holder.bind<TextView>(R.id.action_state)
            val imageView = holder.bind<SimpleDraweeView>(R.id.imageView)

            //不是拍照
            if (position != 0) {
                imageView.setImageURI(Uri.parse("file://" + data.path))
                stateView.visibility = View.VISIBLE
                stateView.isSelected = data.isSelected
                if (data.isSelected) {
                    stateView.text = (selectImages.indexOf(data.path) + 1).toString()
                } else {
                    stateView.text = null
                }
            } else {
                stateView.visibility = View.GONE
                imageView.imageResource = R.drawable.ic_camera_alt_24dp
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }

            stateView.setOnClickListener {
                val selected = data.isSelected
                //本是选中状态
                if (selected) {
                    if (selectImages.contains(data.path)) {
                        selectImages.remove(data.path)
                    }
                } else {
                    if (selectImages.size >= maxNumber) {
                        toast("最多选择" + maxNumber + "张图片")
                        return@setOnClickListener
                    }
                    selectImages.add(data.path)
                }
                data.isSelected = !selected
                notifyDataSetChanged()
            }
            imageView.setOnClickListener {
                //拍照
                if (position == 0) {
                    takePhoto()
                }
            }
        }
    }

    fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMG_$timeStamp.jpg"
        //path 最好不使用data目录。如果需要通知系统刷新图库，data目录的文件其他应用没有访问权限，会无效。
        val path = Environment.getExternalStorageDirectory().absolutePath + "/DCIM/Camera/"
        val storageDir = File(path)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val image = File(storageDir, imageFileName)
        tempFile = image
        val u = Uri.fromFile(tempFile)
        intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0)
        //7.0崩溃问题
        if (Build.VERSION.SDK_INT < 24) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, u)
        } else {
            val contentValues = ContentValues(1)
            contentValues.put(MediaStore.Images.Media.DATA, tempFile?.absolutePath)
            val uri = this@SelectPhotoActivity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }
        startActivityForResult(intent, 0)
    }

    data class Image(val path: String, var isSelected: Boolean = false)
}