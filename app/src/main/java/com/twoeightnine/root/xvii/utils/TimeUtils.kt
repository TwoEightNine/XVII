package com.twoeightnine.root.xvii.utils

import android.annotation.SuppressLint
import com.twoeightnine.root.xvii.lg.Lg
import java.text.SimpleDateFormat
import java.util.*

//fun getTime(ts: Int, full: Boolean = false, format: String = "HH:mm"): String {
//
//    val dateTs = Date(ts * 1000L)
//    val dateCurr = Date(System.currentTimeMillis())
//    val year = SimpleDateFormat("yyyy")
//    val month = SimpleDateFormat("MM")
//    val day = SimpleDateFormat("dd")
//    val hour = SimpleDateFormat("HH")
//    val min = SimpleDateFormat("mm")
//    val time = SimpleDateFormat(format)
//    year.timeZone = TimeZone.getDefault()
//    month.timeZone = TimeZone.getDefault()
//    day.timeZone = TimeZone.getDefault()
//    hour.timeZone = TimeZone.getDefault()
//    min.timeZone = TimeZone.getDefault()
//    val result: String
//    if (year.format(dateTs) != year.format(dateCurr)) {
//        result = "${day.format(dateTs)}.${month.format(dateTs)}.${year.format(dateTs)}"
//        return if (full) {
//            "$result ${time.format(dateTs)}"
//        } else {
//            result
//        }
//    }
//    if (month.format(dateTs) != month.format(dateCurr)) {
//        result = "${day.format(dateTs)}.${month.format(dateTs)}"
//        return if (full) {
//            "$result ${time.format(dateTs)}"
//        } else {
//            result
//        }
//    }
//    if (day.format(dateTs) != day.format(dateCurr)) {
//        result = "${day.format(dateTs)}.${month.format(dateTs)}"
//        return if (full) {
//            "$result ${time.format(dateTs)}"
//        } else {
//            result
//        }
//    }
//    return time.format(dateTs)
//}

@SuppressLint("SimpleDateFormat")
fun getTime(ts: Int, full: Boolean = false, onlyTime: Boolean = false, format: String? = null): String {
    val date = Date(ts * 1000L)
    val today = Date()
    if (format != null) {
        return SimpleDateFormat(format).format(date)
    }
    val fmt = when {
        onlyTime || today.day == date.day &&
                today.month == date.month &&
                today.year == date.year -> "HH:mm"
        full && today.year == date.year -> "HH:mm dd MMM"
        full -> "HH:mm dd MMM yyyy"
        today.year == date.year -> "dd MMM"
        else -> "dd MMM yyyy"
    }
    return SimpleDateFormat(fmt).format(date).toLowerCase()
}

fun secToTime(sec: Int): String {
    val min = sec / 60
    val secn = sec % 60
    return if (secn > 9) {
        "$min:$secn"
    } else {
        "$min:0$secn"
    }
}

@SuppressLint("SimpleDateFormat")
fun formatDate(date: String): String = try {
    SimpleDateFormat("dd MMM yyyy").format(SimpleDateFormat("dd.MM.yyyy").parse(date))
} catch (e: Exception) {
    Lg.wtf("format date: ${e.message}")
    e.printStackTrace()
    date
}

/**
 * fucking vk format allows "d.M.yyyy" WTF???
 */
fun formatBdate(bdate: String?) = bdate
        ?.split(".")
        ?.map {
            if (it.length == 1) "0$it" else it
        }
        ?.joinToString(".") ?: ""

fun time() = (System.currentTimeMillis() / 1000L).toInt()