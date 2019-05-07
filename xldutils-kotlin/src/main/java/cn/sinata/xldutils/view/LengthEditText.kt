package cn.sinata.xldutils.view

import android.content.Context
import android.content.res.TypedArray
import android.text.InputFilter
import android.util.AttributeSet
import android.widget.EditText

import cn.sinata.xldutils.R
import cn.sinata.xldutils.sysErr
import cn.sinata.xldutils.utils.LengthFilter

/**
 *  没有使用@JvmOverloads，不知道是书写bug，还是kotlin的bug，用@JvmOverloads在init里面初始化时。
 *  filters = arrayOf<InputFilter>(LengthFilter(max))这句怎么都不生效。。。
 *
 *  长度maxLength按中文1个，英文半个算。
 */

class LengthEditText : EditText {

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    internal fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.LengthEditText)
            val max = a.getInt(R.styleable.LengthEditText_android_maxLength, -1)
            a.recycle()
            if (max >= 0) {
                filters = arrayOf<InputFilter>(LengthFilter(max))
            }
        }
    }
}
