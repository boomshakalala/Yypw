package com.hbcx.user.ui.user

import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.CheckedTextView
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.visible
import com.hbcx.user.R
import com.hbcx.user.ui.TranslateStatusBarActivity
import kotlinx.android.synthetic.main.activity_my_coupon.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MyCouponActivity:TranslateStatusBarActivity(), View.OnClickListener, ViewPager.OnPageChangeListener {
    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        (fragments[position] as CouponFragment).refresh()
    }

    override fun onClick(v: View?) {
        v as CheckedTextView
        if (v.id != checkItem!!.id){
            checkItem!!.isChecked = false
        }
        v.isChecked = true
        checkItem = v
        useType = when(v.id){
            R.id.tv_ticket-> {
                title = "票务券"
                1 }
            R.id.tv_rent-> {
                title = "租车券"
                2
            }
            R.id.tv_group_rent-> {
                title  = "包车券"
                3
            }
            R.id.tv_fast-> {
                title  = "快车券"
                4}
            R.id.tv_special-> {
                title = "专车券"
                5 }
            else ->{
                title = "我的优惠券"
                0
            }
        }
        ll_filter.gone()
        titleBar.titleView.setCompoundDrawablesWithIntrinsicBounds(0,0,R.mipmap.ic_panel_down,0)
        (fragments[mViewPager.currentItem] as CouponFragment).refresh()
    }

    private var checkItem:CheckedTextView?=null
    override fun setContentView() = R.layout.activity_my_coupon

    private val fragments = arrayListOf<Fragment>()

    override fun initClick() {
        titleBar.titleView.onClick {
            if (ll_filter.visibility == View.GONE){
                ll_filter.visible()
                titleBar.titleView.setCompoundDrawablesWithIntrinsicBounds(0,0,R.mipmap.ic_panel,0)
            }else{
                ll_filter.gone()
                titleBar.titleView.setCompoundDrawablesWithIntrinsicBounds(0,0,R.mipmap.ic_panel_down,0)
            }
        }
        bg_view.onClick {
            ll_filter.gone()
            titleBar.titleView.setCompoundDrawablesWithIntrinsicBounds(0,0,R.mipmap.ic_panel_down,0)
        }
        tv_all.setOnClickListener(this)
        tv_ticket.setOnClickListener(this)
        tv_rent.setOnClickListener(this)
        tv_group_rent.setOnClickListener(this)
        tv_fast.setOnClickListener(this)
        tv_special.setOnClickListener(this)
    }

    override fun initView() {
        title = "我的优惠券"
        checkItem = tv_all
        titleBar.titleView.setCompoundDrawablesWithIntrinsicBounds(0,0,R.mipmap.ic_panel_down,0)
        fragments.add(CouponFragment.getInstance(1))
        fragments.add(CouponFragment.getInstance(2))
        fragments.add(CouponFragment.getInstance(3))
        tab_top.setViewPager(mViewPager, arrayOf("未使用","使用记录","已过期"),this,fragments)
        mViewPager.addOnPageChangeListener(this)
    }

    companion object {
        var useType = 0
    }
}