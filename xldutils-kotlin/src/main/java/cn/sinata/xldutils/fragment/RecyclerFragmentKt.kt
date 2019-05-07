package cn.sinata.xldutils.fragment

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import cn.sinata.xldutils.R
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
/**
 *
 */
fun RecyclerFragment.addItemDecoration(size:Int = 1,colorRes:Int = R.color.dividing_line_color) {
    val itemDe = HorizontalDividerItemDecoration.Builder(context)
            .size(size)
            .color(ContextCompat.getColor(context!!, colorRes))
            .build()
    addItemDecoration(itemDe)
}

fun RecyclerFragment.addItemDecoration(itemDecoration: RecyclerView.ItemDecoration){
    mSwipeRefreshLayout.addItemDecoration(itemDecoration)
}