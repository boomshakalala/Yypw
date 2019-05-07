package com.hbcx.user.ui.ticket

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import cn.sinata.xldutils.fragment.BaseFragment
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.utils.requestByF
import kotlinx.android.synthetic.main.fragment_ticket.*

class TicketFragment:BaseFragment() {
    private val titles = arrayListOf<String>()
    private val fragments = arrayListOf<Fragment>()
    private var firstShow = true //是否第一次显示，若不是就只刷数据

    override fun contentViewId() = R.layout.fragment_ticket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userVisibleHint = true
    }

    override fun onFirstVisibleToUser() {
        titles.add("汽车票")
        fragments.add(TicketMainFragment())
        getData()
        firstShow = false
        view_pager_ticket.addOnPageChangeListener(object :ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position!=0){
                    val lineFragment = fragments[position] as TicketLineFragment
                    lineFragment.refresh()
                }
                if (position == 0){
                    val ticketMainFragment = fragments[0] as TicketMainFragment
                    ticketMainFragment.refreshData()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (!firstShow){
            if (view_pager_ticket.currentItem == 0){
                val ticketMainFragment = fragments[0] as TicketMainFragment
                ticketMainFragment.refreshData()
            }
        }
    }

    fun refresh(){
        if (view_pager_ticket.currentItem!=0){
            val lineFragment = fragments[view_pager_ticket.currentItem] as TicketLineFragment
            lineFragment.refresh()
        }
    }

    fun refreshOrderData(){
        val ticketMainFragment = fragments[0] as TicketMainFragment
        ticketMainFragment.refreshData()
    }

    private fun getData(){
        HttpManager.getLineTypes().requestByF(this){_,data->
            data?.let {
                it.forEach {
                    titles.add(it.name)
                    fragments.add(TicketLineFragment.getInstance(it.id))
                }
                tab_ticket.setViewPager(view_pager_ticket,titles.toTypedArray(),activity,fragments)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode in 11..14)
            fragments[0].onActivityResult(requestCode,resultCode,data)
    }
}