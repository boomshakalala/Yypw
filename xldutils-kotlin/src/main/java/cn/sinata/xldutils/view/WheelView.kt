package cn.sinata.xldutils.view

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.jetbrains.anko.dip
import java.util.*
import kotlin.properties.Delegates

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 7/1/14.
 */
class WheelView : ScrollView {

    interface OnWheelViewListener {
        fun onSelected(selectedIndex: Int, item: String)
    }


    private var mContext: Context? = null

    private var views: LinearLayout? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    //    String[] items;
    private var items: MutableList<String>? = null

    public fun getItems(): List<String>? = items

    fun setItems(list: List<String>) {
        if (null == items) {
            items = ArrayList()
        }
        items!!.clear()
        items!!.addAll(list)

        // 前面和后面补全
        for (i in 0 until offset) {
            items!!.add(0, "")
            items!!.add("")
        }
        views?.removeAllViews()
        initData()

    }

    var offset = OFF_SET_DEFAULT // 偏移量（需要在最前面和最后面补全）

    private var displayItemCount: Int = 0 // 每页显示的数量

    private var selectedIndex = 1


    private fun init(context: Context) {
        this.mContext = context
        this.isVerticalScrollBarEnabled = false

        views = LinearLayout(context)
        views!!.orientation = LinearLayout.VERTICAL
        this.addView(views)

        scrollerTask = Runnable {
            val newY = scrollY
            if (initialY - newY == 0) { // stopped
                val remainder = initialY % itemHeight
                val divided = initialY / itemHeight
                //                    Log.d(TAG, "initialY: " + initialY);
                //                    Log.d(TAG, "remainder: " + remainder + ", divided: " + divided);
                if (remainder == 0) {
                    selectedIndex = divided + offset

                    onSeletedCallBack()
                } else {
                    if (remainder > itemHeight / 2) {
                        this@WheelView.post {
                            this@WheelView.smoothScrollTo(0, initialY - remainder + itemHeight)
                            selectedIndex = divided + offset + 1
                            onSeletedCallBack()
                        }
                    } else {
                        this@WheelView.post {
                            this@WheelView.smoothScrollTo(0, initialY - remainder)
                            selectedIndex = divided + offset
                            onSeletedCallBack()
                        }
                    }
                }
            } else {
                initialY = scrollY
                this@WheelView.postDelayed(scrollerTask, newCheck.toLong())
            }
        }
    }

    private var initialY: Int = 0

    private var scrollerTask: Runnable by Delegates.notNull()
    private var newCheck = 50

    private fun startScrollerTask() {

        initialY = scrollY
        this.postDelayed(scrollerTask, newCheck.toLong())
    }

    private fun initData() {
        displayItemCount = offset * 2 + 1

        for (item in items!!) {
            views!!.addView(createView(item))
        }

        refreshItemView(0)
    }

    private var itemHeight = 0

    private fun createView(item: String): TextView {
        val tv = TextView(mContext)
        tv.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip(50))
        tv.setSingleLine(true)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        tv.text = item
        tv.gravity = Gravity.CENTER
        val padding = dip2px(15f)
        tv.setPadding(padding, 0, padding, 0)
        if (0 == itemHeight) {
            itemHeight = Math.max(getViewMeasuredHeight(tv),dip(50))
            views!!.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight * displayItemCount)
            this.layoutParams.height = itemHeight * displayItemCount
        }
        return tv
    }


    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        refreshItemView(t)

        scrollDirection = if (t > oldt) {
            //            Log.d(TAG, "向下滚动");
            SCROLL_DIRECTION_DOWN
        } else {
            //            Log.d(TAG, "向上滚动");
            SCROLL_DIRECTION_UP

        }
    }

    private fun refreshItemView(y: Int) {
        var position = y / itemHeight + offset
        val remainder = y % itemHeight
        val divided = y / itemHeight

        if (remainder == 0) {
            position = divided + offset
        } else {
            if (remainder > itemHeight / 2) {
                position = divided + offset + 1
            }
        }

        val childSize = views!!.childCount
        for (i in 0 until childSize) {
            val itemView = views!!.getChildAt(i) as TextView
            if (position == i) {
                itemView.setTextColor(Color.parseColor("#333333"))
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                if (itemView.paint != null)
                    itemView.paint.isFakeBoldText = true
            } else {
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                itemView.setTextColor(Color.parseColor("#999999"))
                if (itemView.paint != null)
                    itemView.paint.isFakeBoldText = false
            }
        }
    }

    /**
     * 获取选中区域的边界
     */
    private var selectedAreaBorder: IntArray? = null

    private fun obtainSelectedAreaBorder(): IntArray {
        if (null == selectedAreaBorder) {
            selectedAreaBorder = IntArray(2)
            selectedAreaBorder!![0] = itemHeight * offset
            selectedAreaBorder!![1] = itemHeight * (offset + 1)
        }
        return selectedAreaBorder as IntArray
    }


    private var scrollDirection = -1

    internal var paint: Paint? = null
    internal var viewWidth: Int = 0

    override fun setBackgroundDrawable(background: Drawable?) {
        var mBackground = background

//        if (viewWidth == 0) {
//            viewWidth = mContext!!.windowManager.defaultDisplay.width
//        }
//
//        if (null == paint) {
//            paint = Paint()
//            paint!!.color = Color.parseColor("#83cde6")
//            paint!!.strokeWidth = dip2px(1f).toFloat()
//        }
//
//        mBackground = object : Drawable() {
//            override fun draw(canvas: Canvas) {
//                canvas.drawLine((viewWidth * 1 / 6).toFloat(), obtainSelectedAreaBorder()[0].toFloat(), (viewWidth * 5 / 6).toFloat(), obtainSelectedAreaBorder()[0].toFloat(), paint!!)
//                canvas.drawLine((viewWidth * 1 / 6).toFloat(), obtainSelectedAreaBorder()[1].toFloat(), (viewWidth * 5 / 6).toFloat(), obtainSelectedAreaBorder()[1].toFloat(), paint!!)
//            }
//
//            override fun setAlpha(alpha: Int) {
//
//            }
//
//            override fun setColorFilter(cf: ColorFilter?) {
//
//            }
//
//            override fun getOpacity(): Int = 0
//        }


        super.setBackgroundDrawable(mBackground)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        setBackgroundDrawable(null)
    }

    /**
     * 选中回调
     */
    private fun onSeletedCallBack() {
        if (null != onWheelViewListener) {
            onWheelViewListener!!.onSelected(seletedIndex, seletedItem)
        }

    }

    fun setSeletion(position: Int) {
        selectedIndex = position + offset
        this.post { this@WheelView.smoothScrollTo(0, position * itemHeight) }

    }

    val seletedItem: String
        get() = items!![selectedIndex]

    val seletedIndex: Int
        get() = selectedIndex - offset


    override fun fling(velocityY: Int) {
        super.fling(velocityY / 3)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {

            startScrollerTask()
        }
        return super.onTouchEvent(ev)
    }

    private var onWheelViewListener: OnWheelViewListener? = null

    fun setOnWheelViewListener(l:(selectedIndex: Int, item: String)->Unit){
        onWheelViewListener = object :WheelView.OnWheelViewListener{
            override fun onSelected(selectedIndex: Int, item: String) {
                l(selectedIndex,item)
            }
        }
    }

    private fun dip2px(dpValue: Float): Int {
        val scale = mContext!!.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun getViewMeasuredHeight(view: View): Int {
        val width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE shr 2, View.MeasureSpec.AT_MOST)
        view.measure(width, expandSpec)
        return view.measuredHeight
    }

    companion object {
        val OFF_SET_DEFAULT = 2
        private val SCROLL_DIRECTION_UP = 0
        private val SCROLL_DIRECTION_DOWN = 1
    }

}