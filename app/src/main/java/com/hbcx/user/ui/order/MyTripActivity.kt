package com.hbcx.user.ui.order

import android.content.Intent
import android.support.v4.app.Fragment
import com.hbcx.user.R
import com.hbcx.user.ui.TranslateStatusBarActivity
import kotlinx.android.synthetic.main.activity_my_trip.*

class MyTripActivity: TranslateStatusBarActivity() {
    override fun setContentView(): Int = R.layout.activity_my_trip

    override fun initClick() {
    }

    private val ticket = TicketOrderFragment()
    private val rent = RentOrderFragment()
    private val groupRent = GroupRentOrderFragment()
    private val fast = FastOrderFragment.instance(1)
    private val specail = FastOrderFragment.instance(2)

    private val fragments by lazy {
        arrayListOf<Fragment>()
    }

    override fun initView() {
        titleBar.setTitle("我的行程")
        fragments.add(ticket)
        fragments.add(rent)
        fragments.add(groupRent)
        fragments.add(fast)
        fragments.add(specail)
        tab_top.setViewPager(view_pager, arrayOf("票务","租车","包车","快车","专车"),this,fragments)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            1->fast.onActivityResult(requestCode,resultCode,data)
            2->specail.onActivityResult(requestCode,resultCode,data)
            3,30->rent.onActivityResult(requestCode,resultCode,data)
            4,40,50->groupRent.onActivityResult(requestCode,resultCode,data)
            5,60,70->ticket.onActivityResult(requestCode,resultCode,data)
        }
    }
}