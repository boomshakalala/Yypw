package cn.sinata.xldutils.adapter.util

import android.support.annotation.IdRes
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView

/**
 *
 */
class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val mConvertView = view
    @Suppress("UNCHECKED_CAST")
    fun <T : View> bind(viewId: Int): T {// 通过ViewId得到View
        var viewArray: SparseArray<View>?
        if (mConvertView.tag == null) {
            viewArray = SparseArray<View>()
            mConvertView.tag = viewArray
        } else {
            viewArray = mConvertView.tag as SparseArray<View>
        }

        var childView: View? = viewArray.get(viewId)
        if (childView == null) {
            childView = mConvertView.findViewById(viewId)
            viewArray.put(viewId, childView)
        }
        return childView as T

    }

    /**
     * 设置TextView文字

     * @param resId TextView的id
     * *
     * @param text  文字内容
     */
    fun setText(resId: Int, text: CharSequence?) {
        if (bind<View>(resId) is TextView)
            bind<TextView>(resId).text = text
    }

    fun setImageURI(@IdRes resId: Int, url: String?) {
        val view = bind<View>(resId)
        if (view is SimpleDraweeView) {
            view.setImageURI(url)
        }
    }

    fun setOnClickListener(@IdRes resId: Int, l: View.OnClickListener) {
        bind<View>(resId).setOnClickListener(l)
    }

}