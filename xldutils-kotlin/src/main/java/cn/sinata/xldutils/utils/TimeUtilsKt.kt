package cn.sinata.xldutils.utils

import java.text.SimpleDateFormat
import java.util.*

/**
时间戳转换默认格式yyyy-MM-dd HH:mm:ss
 */
fun Long.toDefaultTime(): String {
//    val f = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val f = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
    return f.format(Date(this))
}

fun Long.toTime(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val f = SimpleDateFormat(format, Locale.CHINA)
    return f.format(Date(this))
}

fun Long.toMDHMTime(): String {
    val f = SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA)
    return f.format(Date(this))
}

/**
 * 两个时间戳间隔字符
 * @param nowTime 当前时间,默认读取系统时间
 */
fun Long.interval(nowTime: Long = System.currentTimeMillis()): String {
    val desc: String
    val d = Date(this)
    val n = Date(nowTime)
    val delay = n.time - d.time
    val secondsOfHour = (60 * 60).toLong()
    val secondsOfDay = secondsOfHour * 24
    val secondsOfTwoDay = secondsOfDay * 2
    val secondsOfThreeDay = secondsOfDay * 3
    // 相差的秒数
    val delaySeconds = delay / 1000
    desc = when {
//        delaySeconds < 10 -> "刚刚"
//        delaySeconds <= 60 -> delaySeconds.toString() + "秒前"
//        delaySeconds < secondsOfHour -> (delaySeconds / 60).toString() + "分前"
        delaySeconds < secondsOfDay ->
//            (delaySeconds / 60 / 60).toString() + "小时前"
        this.toTime("HH:mm")
//        delaySeconds < secondsOfTwoDay -> "一天前"
//        delaySeconds < secondsOfThreeDay -> "两天前"
        else -> this.toDefaultTime()
    }
    return desc
}

/**
 * 2个时间间隔天数
 */
fun Long.intervalTime(nowTime: Long = System.currentTimeMillis()): Int {
    val c1 = Calendar.getInstance()
    c1.timeInMillis = this
    c1.set(Calendar.HOUR_OF_DAY, 0)
    c1.set(Calendar.MINUTE, 0)
    c1.set(Calendar.SECOND, 0)
    val time1 = c1.timeInMillis

    val c2 = Calendar.getInstance()
    c2.timeInMillis = nowTime
    c2.set(Calendar.HOUR_OF_DAY, 0)
    c2.set(Calendar.MINUTE, 0)
    c2.set(Calendar.SECOND, 0)
    val time2 = c2.timeInMillis
    val delay = time2 - time1
    val count = delay / 1000 / 60 / 60 / 24
    return if (count > 0) count.toInt() else 0
}

/**
 * 间隔时间转换为分秒。
 * @param nowTime 当前时间，需要比较的时间
 */
fun Long.intervalMMSS(nowTime: Long): String {
    val d = Date(this)
    val n = Date(nowTime)
    val delay = n.time - d.time
    return delay.intervalMMSS()
}

/**
 * 间隔时间转换为时分秒。
 * @param nowTime 当前时间，需要比较的时间,大的那个值，不然是负数
 */
fun Long.intervalHHMMSS(nowTime: Long): String {
    val d = Date(this)
    val n = Date(nowTime)
    val delay = n.time - d.time
    return delay.intervalHHMMSS()
}

fun Long.intervalHHMMSS(): String {
    val desc: String
    val secondsOfHour = (60 * 60).toLong()
    val secondsOfDay = secondsOfHour * 24
    val secondsOfTwoDay = secondsOfDay * 2
    val secondsOfThreeDay = secondsOfDay * 3
    // 相差的秒数
    val delaySeconds = this / 1000
    desc = when {
        delaySeconds < 60 -> String.format(Locale.CHINA, "00:00:%02d", delaySeconds)
        delaySeconds < secondsOfHour -> String.format(Locale.CHINA, "00:%02d:%02d", delaySeconds / 60, delaySeconds % 60)
        delaySeconds < secondsOfDay -> String.format(Locale.CHINA, "%02d:%02d:%02d", delaySeconds / 60 / 60, delaySeconds / 60 % 60, delaySeconds % 60)
//        delaySeconds < secondsOfTwoDay -> "一天"
//        delaySeconds < secondsOfThreeDay -> "两天"
        else -> String.format(Locale.CHINA, "%02d:%02d:%02d", delaySeconds / 60 / 60, delaySeconds / 60 % 60, delaySeconds % 60)
    }
    return desc
}

/**
 * 间隔时间转换为分秒。
 * @return
 */
fun Long.intervalMMSS(): String {
    val desc: String
    val secondsOfHour = (60 * 60).toLong()
    val secondsOfDay = secondsOfHour * 24
    val secondsOfTwoDay = secondsOfDay * 2
    val secondsOfThreeDay = secondsOfDay * 3
    // 相差的秒数
    val delaySeconds = this / 1000
    desc = when {
        delaySeconds < 60 -> String.format(Locale.CHINA, "00:%02d", delaySeconds)
        delaySeconds < secondsOfHour -> String.format(Locale.CHINA, "%02d:%02d", delaySeconds / 60, delaySeconds % 60)
        delaySeconds < secondsOfDay -> (delaySeconds / 60 / 60).toString() + "小时"
        delaySeconds < secondsOfTwoDay -> "一天"
        delaySeconds < secondsOfThreeDay -> "两天"
        else -> "超过两天"
    }
    return desc
}

/**
 * 时间戳转星期
 */
fun Long.toWeek(pre: String = "周"): String {
    val myDate: Int
    val week: String
    val cd = Calendar.getInstance()
    cd.time = Date(this)
    myDate = cd.get(Calendar.DAY_OF_WEEK)
    // 获取指定日期转换成星期几
    week = when (myDate) {
        1 -> String.format("%s日", pre)
        2 -> String.format("%s一", pre)
        3 -> String.format("%s二", pre)
        4 -> String.format("%s三", pre)
        5 -> String.format("%s四", pre)
        6 -> String.format("%s五", pre)
        7 -> String.format("%s六", pre)
        else -> ""
    }
    return week
}

///**
// * yyyy-MM-dd HH:mm 型时间字符串1是否小于时间2
// */
//fun String?.isBefore(time:String?):Boolean{
//    if (this == null || time == null) {
//        return false
//    }
//    val f = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
//    try {
//        val d1 = f.parse(this)
//        val t1 = d1.time
//        val d2 = f.parse(time)
//        val t2 = d2.time
//        sysErr(""+t1+"-------"+t2)
//        return t1<t2
//    }catch (e:ParseException){
//        e.printStackTrace()
//    }
//    return false
//}

fun String?.parserTime(format: String = "yyyy-MM-dd HH:mm:ss"): Long {
    if (this == null) {
        return 0
    }

    val ft = if (format.isEmpty()) {
        "yyyy-MM-dd HH:mm:ss"
    } else {
        format
    }
    val f = SimpleDateFormat(ft, Locale.CHINA)
    return try {
        val date = f.parse(this)
        date.time
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }
}

fun String?.parserDate(format: String = "yyyy-MM-dd HH:mm:ss"): Date {
    if (this == null) {
        return Date()
    }

    val ft = if (format.isEmpty()) {
        "yyyy-MM-dd HH:mm:ss"
    } else {
        format
    }
    val f = SimpleDateFormat(ft, Locale.CHINA)
    return try {
        f.parse(this)
    } catch (e: Exception) {
        e.printStackTrace()
        Date()
    }
}

fun Long.timeDay(nowTime: Long = System.currentTimeMillis()): String {
    val desc: String
    val c = Calendar.getInstance()
    c.timeInMillis = nowTime
    val nowDay = c.get(Calendar.DAY_OF_MONTH)
    c.set(Calendar.DAY_OF_MONTH, nowDay + 1)
    val nextDay = c.timeInMillis
    c.set(Calendar.DAY_OF_MONTH, nowDay + 2)
    val next2Day = c.timeInMillis
    desc = when {
        this.toTime("yyyy-MM-dd") == nowTime.toTime("yyyy-MM-dd") -> "今天"
        this.toTime("yyyy-MM-dd") == nextDay.toTime("yyyy-MM-dd") -> "明天"
        this.toTime("yyyy-MM-dd") == next2Day.toTime("yyyy-MM-dd") -> "后天"
        else -> ""
    }
    return "$desc ${this.toMDHMTime()}"
}