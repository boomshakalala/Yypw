package cn.sinata.xldutils.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import cn.sinata.xldutils.R
import org.jetbrains.anko.dip
import org.jetbrains.anko.padding
import org.jetbrains.anko.sp

import java.lang.reflect.Field
import java.util.ArrayList
import java.util.Calendar

/**
 *
 */
class MDatePicker : DatePicker {
    private var mPickers: MutableList<NumberPicker>? = null

    /**
     * 获得时间
     *
     * @return yyyy-mm-dd
     */
    /**
     * 设置时间
     *
     * @param strDate yyyy-mm-dd
     */
    var date: String
        get() {
            val sbDate = StringBuilder()
            sbDate.append(format2Digits(year)).append("-")
                    .append(format2Digits(month + 1)).append("-")
                    .append(format2Digits(dayOfMonth))
            return sbDate.toString()
        }
        set(strDate) {
            val day: Int
            val month: Int
            val year: Int
            if (!TextUtils.isEmpty(strDate)) {
                val dateValues = strDate.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (dateValues.size == 3) {
                    year = Integer.parseInt(dateValues[0])
                    month = Integer.parseInt(dateValues[1]) - 1
                    day = Integer.parseInt(dateValues[2])
                    updateDate(year, month, day)
                    return
                }
            }
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            updateDate(year, month, day)
        }

    constructor(context: Context) : super(context) {
        findNumberPicker()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        findNumberPicker()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        findNumberPicker()
    }

    /**
     * 得到控件里面的numberpicker组件
     */
    private fun findNumberPicker() {
        mPickers = ArrayList()
        val llFirst = getChildAt(0) as LinearLayout
        val mSpinners = llFirst.getChildAt(0) as LinearLayout

        for (i in 0 until mSpinners.childCount) {
            val picker = mSpinners.getChildAt(i) as NumberPicker
            mPickers!!.add(i, picker)
        }
    }

    private fun format2Digits(value: Int): String {
        return String.format("%02d", value)
    }


    /**
     * 设置picker间隔
     *
     * @param margin
     */
    fun setPickerMargin(margin: Int) {
        for (picker in mPickers!!) {
            val lps = picker.layoutParams as LinearLayout.LayoutParams
            lps.setMargins(margin, 0, margin, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                lps.marginStart = margin
                lps.marginEnd = margin
            }
            picker.layoutParams = lps
        }
    }

    /**
     * 设置时间选择器的分割线颜色
     */
    fun setDividerColor(color: Int) {
        mPickers!!.indices
                .map { mPickers!![it] }
                .forEach {
                    try {
                        val pf = NumberPicker::class.java.getDeclaredField("mSelectionDivider")
                        pf.isAccessible = true
                        pf.set(it, ColorDrawable(color))
                    } catch (e: NoSuchFieldException) {
                        e.printStackTrace()
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    }
                }
    }

    fun setTextSize(size: Float) {
        mPickers!!.indices
                .map { mPickers!![it] }
                .forEach {
                    try {
                        //这里改变初始化时的中间一行文字大小。
                        val editText = it.findViewById<View>(resources.getIdentifier("numberpicker_input","id","android"))
                        if (editText != null && editText is EditText) {
                            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,size)
                        }
                        //这里改变滚动后文字大小。
                        val pf = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
                        pf.isAccessible = true
                        if (pf.get(it) is Paint) {
                            (pf.get(it) as Paint).textSize = sp(size).toFloat()
                        }
                    } catch (e: NoSuchFieldException) {
                        e.printStackTrace()
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    }
                }
    }
}