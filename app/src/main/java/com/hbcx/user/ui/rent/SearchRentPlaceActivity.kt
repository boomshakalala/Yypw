package com.hbcx.user.ui.rent

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import cn.sinata.xldutils.*
import cn.sinata.xldutils.rxutils.ResultException
import cn.sinata.xldutils.view.SwipeRefreshRecyclerLayout
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.amap.api.services.help.Tip
import com.hbcx.user.R
import com.hbcx.user.db.DBHelper
import com.hbcx.user.db.HistoryDBManager
import com.hbcx.user.ui.SelectCityActivity
import com.hbcx.user.ui.TranslateStatusBarActivity
import io.reactivex.Flowable
import io.reactivex.subscribers.DisposableSubscriber
import kotlinx.android.synthetic.main.activity_rent_search_address.*
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast

class SearchRentPlaceActivity : TranslateStatusBarActivity() {
    private val type by lazy { //1:票务 2:租车
        intent.getIntExtra("type", 0)
    }
    private val city by lazy { //票务出发城市
        intent.getStringExtra("city")
    }

    override fun setContentView() = R.layout.activity_rent_search_address

    override fun initClick() {//清空历记录
        clearHistoryView.setOnClickListener {
            HistoryDBManager().clearHistory(this, DBHelper.HISTORY_RENT_TABLE_NAME)
            clearHistoryView.gone()
            mTips.clear()
            tipAdapter.notifyDataSetChanged()
        }

        et_content.addTextChangedListener(textWatcher)

        tipAdapter.setOnItemClickListener { _, position ->
            val tip = mTips[position]
            HistoryDBManager().saveHistory(this, tip, DBHelper.HISTORY_RENT_TABLE_NAME)
            val intent = Intent()
            intent.putExtra("data", tip)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        if (type != 1&&type != 2)
            titleBar.titleView.setOnClickListener {
                startActivityForResult<SelectCityActivity>(1)
            }
    }

    override fun initView() {
        if (type != 1&&type != 2) {
            titleBar.titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.drop_down, 0)
            titleBar.setTitle(com.hbcx.user.YyApplication.selectCityName)
            titleBar.titleView.textSize = 14f
        } else
            title = if(type == 2 )"租车地" else "出发地"
        mSwipeRefreshLayout.setMode(SwipeRefreshRecyclerLayout.Mode.None)
        mSwipeRefreshLayout.setLayoutManager(LinearLayoutManager(this))
        mSwipeRefreshLayout.setAdapter(tipAdapter)
        tipAdapter.setClearView(clearHistoryView)
        clearHistoryView.gone()
        val history = HistoryDBManager().getAddressList(this, 10, DBHelper.HISTORY_RENT_TABLE_NAME)
        if (history.isNotEmpty()) {
            mTips.clear()
            mTips.addAll(history)
            //显示清空按钮
            clearHistoryView.visible()
            tipAdapter.notifyDataSetChanged()
        }
    }

    private val mTips = ArrayList<Tip>()

    private val tipAdapter by lazy {
        com.hbcx.user.adapter.TipAdapter(mTips)
    }

    private val clearHistoryView by lazy {
        layoutInflater.inflate(R.layout.view_clear_history, mSwipeRefreshLayout.mRecyclerView, false)
    }

    private var disposable: DisposableSubscriber<List<Tip>>? = null

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

            if (s == null || s.isEmpty()) {

            } else {
                search(s.toString())
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }

    private fun search(keyword: String) {
        //取消以前的订阅
        if (disposable != null && !disposable!!.isDisposed) {
            disposable?.dispose()
        }
        val query = InputtipsQuery(keyword, if (type!=1) com.hbcx.user.YyApplication.selectCityName else city)
        query.cityLimit = true
        val inputtips = Inputtips(this, query)

        disposable = object : DisposableSubscriber<List<Tip>>() {

            override fun onComplete() {

            }

            override fun onNext(t: List<Tip>?) {
                clearHistoryView.gone()
                sysErr("------t-->$t")
                mTips.clear()
                if (t == null) {

                } else {
                    mTips.addAll(t.filter { it.point != null && (it.point.latitude != 0.0 || it.point.longitude != 0.0) })
                }
                tipAdapter.notifyDataSetChanged()
            }

            override fun onError(t: Throwable?) {
                clearHistoryView.gone()
                if (t is ResultException) {
                    toast(t.message.toString())
                } else {
                    toast("搜索出错啦！")
                }
            }
        }
        Flowable.just(inputtips).ioScheduler().flatMap {
            sysErr("----------inputtips----------")
            val list = try {
                it.requestInputtips()
            } catch (e: Exception) {
                null
            }
            sysErr("----list--->$list")
            if (list == null) {
                Flowable.error(ResultException("没有搜索到相关数据"))
            } else {
                Flowable.just(list)
            }
        }.defaultScheduler().subscribe(disposable)
    }

    override fun onDestroy() {
        try {
            et_content.removeTextChangedListener(textWatcher)
        } catch (e: Exception) {

        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (type != 1&&type != 2)
            title = com.hbcx.user.YyApplication.selectCityName
    }
}
