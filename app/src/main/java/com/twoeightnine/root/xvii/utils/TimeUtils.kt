package com.twoeightnine.root.xvii.utils

import android.annotation.SuppressLint
import com.twoeightnine.root.xvii.lg.Lg
import java.text.SimpleDateFormat
import java.util.*


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
    val isTruncated = date.count { it == '.' } == 1
    val inPattern = if (isTruncated) "dd.MM" else "dd.MM.yyyy"
    val outPattern = if (isTruncated) "dd MMM" else "dd MMM yyyy"
    SimpleDateFormat(outPattern).format(SimpleDateFormat(inPattern).parse(date))
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