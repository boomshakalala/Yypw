package cn.sinata.xldutils.utils

import android.net.Uri
import android.util.Base64
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.regex.Pattern


/**
 * 字符串长度。中文算2个字符。英文算1个字符计算。
 */
fun String?.lengthCN(): Int {

    if (this.isNullOrEmpty()) {
        return 0
    }
    var len = 0
    var count = 0
    while (count < this!!.length) {
        val c = get(count)
        len += if (c.toInt() < 128) {
            1
        } else {
            2
        }
        count++
    }
    return len
}

/**
 * 是否null或者'null'字符
 */
fun String?.isNulls(): Boolean = isNullOrEmpty() || equals("null")

/**
 * urlEncode
 */
fun String?.urlEncode(): String {
    return if (isNullOrEmpty()) "" else URLEncoder.encode(this, "utf-8")
}

/**
 * urlDecode
 */
fun String?.urlDecode(): String {
    return if (isNullOrEmpty()) "" else URLDecoder.decode(this, "utf-8")
}

/**
 * base64
 */
fun String?.base64(charset: Charset = Charsets.UTF_8): String {
    return if (isNullOrEmpty()) "" else Base64.encodeToString(this!!.toByteArray(charset), Base64.DEFAULT)
}

/**
 * 是否有效手机号
 */
fun String?.isValidPhone(): Boolean {
    if (isNullOrEmpty()) {
        return false
    }
    if (this!!.length != 11) {
        return false
    }
    val expression = "(^(13|14|15|16|17|18|19)[0-9]{9}$)"
    val pattern = Pattern.compile(expression)
    val matcher = pattern.matcher(this)
    if (matcher.matches()) {
        return true
    }
    return false
}

/**
 * 是否有效邮箱地址
 */
fun String?.isValidEmail(): Boolean {
    if (isNullOrEmpty()) {
        return false
    }
    val pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$")
    val matcher = pattern.matcher(this)
    if (matcher.matches()) {
        return true
    }
    return false
}

/**
 * 返回隐藏后的手机号，
 */
fun String?.hidePhone(): String {
    if (isNullOrEmpty()) {
        return ""
    }
    if (this!!.length < 7) {
        return this
    }
    return substring(0, 3) + "****" + substring(length - 4, length)
}

/**
 * 隐藏身份证号，必须不为null并且length大于10才返回处理后的字符串
 */
fun String?.hideIdCard(): String {
    if (isNullOrEmpty()) {
        return ""
    }
    if (this!!.length < 10) {
        return this
    }
    return substring(0, 3) + "*****" + substring(length - 4, length)
}

fun String?.isFilePath(): Boolean {
    if (isNullOrEmpty()) {
        return false
    }
    return File(this).exists()
}

/**
 * md5字符串
 */
fun String?.md5(): String {
    if (this == null) {
        return ""
    }
    try {
        val bmd5 = MessageDigest.getInstance("MD5")
        bmd5.update(this.toByteArray())
        var i: Int
        val buf = StringBuffer()
        val b = bmd5.digest()
        for (offset in b.indices) {
            i = b[offset].toInt()
            if (i < 0) {
                i += 256
            }
            if (i < 16) {
                buf.append("0")
            }
            buf.append(Integer.toHexString(i))
        }
        return buf.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: NullPointerException) {
        e.printStackTrace()
    }
    return ""
}

fun String?.toImageUri(): Uri {
    if (this == null) {
        return Uri.parse("")
    }
    return Uri.parse(this)
}

fun String?.isLetterDigit(): Boolean {
    if (this == null) {
        return false
    }
    var isDigit = false//定义一个boolean值，用来表示是否包含数字
    var isLetter = false//定义一个boolean值，用来表示是否包含字母
    this.forEach {
        if (Character.isDigit(it)) {
            isDigit = true
        }
        //用char包装类中的判断字母的方法判断每一个字符
        if (Character.isLetter(it)) {
            isLetter = true
        }
    }
    val regex = "^[a-zA-Z0-9]+$"
    return isDigit && isLetter && matches(Regex(regex))
}
/**
 * 是否有效身份证
 */
fun String?.isValidIdCard(): Boolean {
    if (isNullOrEmpty()) {
        return false
    }
    if (this!!.length != 18) {
        return false
    }
    val expression = "(^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]\$)"
    val pattern = Pattern.compile(expression)
    val matcher = pattern.matcher(this)
    if (matcher.matches()) {
        return true
    }
    return false
}