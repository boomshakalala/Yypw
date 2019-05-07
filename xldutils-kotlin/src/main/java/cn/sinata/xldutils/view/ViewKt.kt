package cn.sinata.xldutils.view

import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView

/**
 *
 */
fun ViewManager.titleBar() = titleBar {}
inline fun ViewManager.titleBar(init: TitleBar.() -> Unit): TitleBar =
        ankoView({ TitleBar(it) },0, init)

fun ViewManager.swipeRefreshRecyclerLayout() = swipeRefreshRecyclerLayout {}
inline fun ViewManager.swipeRefreshRecyclerLayout(init: SwipeRefreshRecyclerLayout.() -> Unit): SwipeRefreshRecyclerLayout =
        ankoView({ SwipeRefreshRecyclerLayout(it) },0, init)