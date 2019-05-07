package cn.sinata.xldutils.activity

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import cn.sinata.xldutils.R
import cn.sinata.xldutils.view.TitleBar
import org.jetbrains.anko.find

/**
 *  带有标题栏的activity
 */
abstract class TitleActivity : BaseActivity() {
    //不使用Extensions插件。
    val rootLayout by lazy {
        find<LinearLayout>(R.id.root_layout)
    }

    val titleBar by lazy {
        find<TitleBar>(R.id.titleBar)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(R.layout.activity_base_title)
        if (layoutResID > 0) {
            layoutInflater.inflate(layoutResID, rootLayout, true)
        }
    }

    override fun setContentView(view: View?) {
        super.setContentView(R.layout.activity_base_title)
        if (view != null) {
            rootLayout.addView(view)
        }
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(R.layout.activity_base_title)
        if (view != null && params != null) {
            rootLayout.addView(view, params)
        }
    }

    override fun setTitle(title: CharSequence?) {
        titleBar.titleView.setText(title)
    }
}