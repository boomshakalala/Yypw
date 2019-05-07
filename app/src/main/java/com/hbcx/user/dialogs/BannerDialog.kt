package com.hbcx.user.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout

import cn.sinata.xldutils.activity.WebViewActivity
import cn.sinata.xldutils.adapter.BaseRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.utils.screenWidth
import com.hbcx.user.R
import com.hbcx.user.network.Api
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.dialog_banner_layout.*
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.forEachChildWithIndex
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.wrapContent

/**
 * 广告弹出
 */
class BannerDialog : DialogFragment() {

    private val mBanners = ArrayList<com.hbcx.user.beans.Banner>()
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.FadeDialog)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val w = (screenWidth() * 0.75).toInt()
        dialog.window.setLayout(w, wrapContent)
        dialog.window.setGravity(Gravity.CENTER)
        dialog.setCanceledOnTouchOutside(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.dialog_banner_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val w = (screenWidth() * 0.75).toInt()
        mRecyclerView.layoutParams.height = (w / 0.714f).toInt()
        mRecyclerView.requestLayout()
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mRecyclerView.adapter = object : BaseRecyclerAdapter<com.hbcx.user.beans.Banner>(mBanners, R.layout.item_banner) {
            override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.Banner) {
                holder.setImageURI(R.id.imageView, data.imgUrl)
                holder.setOnClickListener(R.id.imageView, View.OnClickListener {
                    if (data.jumpType == 2 || data.jumpType == 3) {
                        val url = if (data.jumpType == 2) {
                            Api.URL2 + data.jumpUrl
                        } else {
                            data.jumpUrl ?: ""
                        }
                        startActivity<WebViewActivity>("url" to url)
                    }
                })
            }

            override fun getItemCount(): Int {
                return Int.MAX_VALUE
            }
        }

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (isDetached) {
                        return
                    }
                    val first = (recyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()%mBanners.size
                    val last = (recyclerView?.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()%mBanners.size
                    val current = (first + last) / 2
                    if (mBanners.size > 0 && current < mBanners.size) {
                        ll_in.forEachChildWithIndex { i, view ->
                            if (i == current) {
                                view.setBackgroundResource(R.drawable.ic_circle_white)
                            } else {
                                view.setBackgroundResource(R.drawable.ic_circle_gray)
                            }
                        }
                    }
                }
            }
        })

        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(mRecyclerView)
        tv_close.setOnClickListener {
            dismissAllowingStateLoss()
        }

        mBanners.clear()
        val t = if (arguments != null && arguments!!.containsKey("data")) {
            arguments!!.getParcelableArrayList<com.hbcx.user.beans.Banner>("data")
        } else {
            ArrayList()
        }
        mBanners.addAll(t)
        mRecyclerView.adapter?.notifyDataSetChanged()

        ll_in.removeAllViews()
        mBanners.forEachWithIndex { i, banner ->
            val v = ImageView(context)
            val params = LinearLayout.LayoutParams(dip(5), dip(5))
            params.leftMargin = dip(2)
            params.rightMargin = dip(2)
            if (i == 0) {
                v.setBackgroundResource(R.drawable.ic_circle_white)
            } else {
                v.setBackgroundResource(R.drawable.ic_circle_gray)
            }
            v.layoutParams = params
            ll_in.addView(v)
        }
    }

    override fun show(manager: FragmentManager?, tag: String?) {
        val ft = manager?.beginTransaction()
        ft?.add(this, tag)
        ft?.commitAllowingStateLoss()
    }

    override fun onDestroy() {
        try {
            if (!compositeDisposable.isDisposed) {
                compositeDisposable.dispose()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}