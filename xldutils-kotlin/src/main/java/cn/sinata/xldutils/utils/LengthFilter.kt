package cn.sinata.xldutils.utils

import android.text.InputFilter
import android.text.Spanned

/**
 * 长度inputFilter，中文算2个长度，英文1个
 */
class LengthFilter(max: Int) : InputFilter {
    val maxLength = max.toFloat()
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence {
        var dIndex = 0
        var count = 0.0f
        while (count <= maxLength && dIndex < dest!!.length) {
            val c = dest[dIndex++]
            count += if (c < 128.toChar()) {
                0.5f
            } else {
                1.0f
            }
        }
        if (count > maxLength) {
            return dest!!.subSequence(0, dIndex - 1)
        }
        var sindex = 0
        while (count <= maxLength && sindex < source!!.length) {
            val c = source[sindex++]
            count += if (c < 128.toChar()) {
                0.5f
            } else {
                1f
            }
        }
        if (count > maxLength) {
            sindex--
        }
        return source!!.subSequence(0, sindex)
    }
}