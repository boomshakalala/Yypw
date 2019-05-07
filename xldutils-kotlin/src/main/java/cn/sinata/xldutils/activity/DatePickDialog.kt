package cn.sinata.xldutils.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.sinata.xldutils.R
import cn.sinata.xldutils.widget.MDatePicker
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.find

/**
 * 日期选择
 */
@SuppressLint("ValidFragment")
class DatePickDialog(val min: Long = 0, val max: Long = 0) : DialogFragment() {

    private val datePicker by lazy {
        find<MDatePicker>(R.id.mDatePicker)
    }

    private val cancelView by lazy {
        find<TextView>(R.id.action_cancel)
    }

    private val sureView by lazy {
        find<TextView>(R.id.action_sure)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_FRAME, R.style.Dialog)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog.window.setGravity(Gravity.BOTTOM)
        dialog.setCanceledOnTouchOutside(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.activity_date_pick_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (max > 0) {
                datePicker.maxDate = max
            }
            if (min > 0) {
                datePicker.minDate = min
            }
        }

        datePicker.setDividerColor(Color.TRANSPARENT)
        datePicker.setPickerMargin(0)
        datePicker.setTextSize(16f)
        cancelView.setOnClickListener {
            dismissAllowingStateLoss()
        }

        sureView.setOnClickListener {
            onDateSetListener?.onSet(datePicker.year, datePicker.month + 1, datePicker.dayOfMonth)
            dismissAllowingStateLoss()
        }
    }

    fun setOnDateSetListener(listener: OnDateSetListener?) {
        this.onDateSetListener = listener
    }

    fun setOnDateSetListener(listener: (year: Int, month: Int, day: Int) -> Unit) {
        this.onDateSetListener = object : OnDateSetListener {
            override fun onSet(year: Int, month: Int, day: Int) {
                listener(year, month, day)
            }
        }
    }

    private var onDateSetListener: OnDateSetListener? = null

    interface OnDateSetListener {
        fun onSet(year: Int, month: Int, day: Int)
    }


}
