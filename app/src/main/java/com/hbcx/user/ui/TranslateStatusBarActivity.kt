package com.hbcx.user.ui

import android.os.Bundle
import cn.sinata.xldutils.activity.TitleActivity
import com.hbcx.user.R
import com.hbcx.user.utils.StatusBarUtil
import org.jetbrains.anko.backgroundColorResource

abstract class TranslateStatusBarActivity :TitleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(setContentView())
        titleBar.setTitleColor(R.color.textColor)
        titleBar.backgroundColorResource = R.color.white
        titleBar.leftView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_back_arrow,0,0,0)
        StatusBarUtil.initStatus(window)
        initView()
        initClick()
    }

    abstract fun setContentView():Int
    abstract fun initClick()
    abstract fun initView()
}