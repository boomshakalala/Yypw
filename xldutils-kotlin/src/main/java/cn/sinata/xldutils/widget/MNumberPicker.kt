package cn.sinata.xldutils.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker

class MNumberPicker : NumberPicker, NumberPicker.Formatter {
    override fun format(value: Int): String {
        return if (displayedValues != null) {
            displayedValues[value]
        } else {
            ""
        }

    }

    constructor(context: Context) : super(context) {
        setFormatter(this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setFormatter(this)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setFormatter(this)
    }

    override fun addView(child: View) {
        super.addView(child)
        updateView(child)
    }

    override fun addView(child: View, index: Int,
                         params: android.view.ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        updateView(child)
    }

    override fun addView(child: View, params: android.view.ViewGroup.LayoutParams) {
        super.addView(child, params)
        updateView(child)
    }

    fun updateView(view: View) {
        (view as? EditText)?.setTextColor(Color.parseColor("#333333"))
        (view as? EditText)?.setTextSize(TypedValue.COMPLEX_UNIT_SP,16f)
        (view as? EditText)?.isEnabled = false
        (view as? EditText)?.isFocusable = false
        (view as? EditText)?.isFocusableInTouchMode = false
        //分割线透明
        try {
            val pf = NumberPicker::class.java.getDeclaredField("mSelectionDivider")
            pf.isAccessible = true
            pf.set(this, ColorDrawable(Color.TRANSPARENT))
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

}