/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.utils

import android.annotation.SuppressLint
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.lg.L
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


const val SS = ":ss"
const val HH_MM = "HH:mm"
const val DD_MM = "dd.MM"
const val DD_MMM = "dd MMM"
const val DD_MM_YYYY = "dd.MM.yyyy"
const val DD_MMM_YYYY = "dd MMM yyyy"

@SuppressLint("SimpleDateFormat")
fun getTime(ts: Int, shortened: Boolean = false, withSeconds: Boolean = false, noDate: Boolean = false, format: String? = null): String {
    val date = Date(ts * 1000L)

    if (format != null) {
        return SimpleDateFormat(format).format(date)
    }

    val today = Date()
    val yesterday = Date(today.time - TimeUnit.DAYS.toMillis(1))

    val isToday = today.isTheSameDay(date)
    val isYesterday = yesterday.isTheSameDay(date)
    val isThisYear = today.year == date.year

    val seconds = if (withSeconds) SS else ""
    val fmt = when {
        noDate || isToday -> "$HH_MM$seconds"
        !shortened && isYesterday -> "$HH_MM$seconds"
        !shortened && isThisYear -> "$HH_MM$seconds $DD_MMM"
        !shortened -> "$HH_MM$seconds $DD_MMM_YYYY"
        today.year == date.year -> DD_MMM
        else -> DD_MMM_YYYY
    }
    val formatted = SimpleDateFormat(fmt).format(date).toLowerCase()
    return when {
        !shortened && isYesterday && !noDate -> "${App.context.getString(R.string.date_yesterday)} $formatted"
        else -> formatted
    }
}

@SuppressLint("SimpleDateFormat")
fun getDate(ts: Int): String {
    val date = Date(ts * 1000L)
    val today = Date()
    val fmt = when {
        today.year == date.year -> DD_MMM
        else -> DD_MMM_YYYY
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
    val inPattern = if (isTruncated) DD_MM else DD_MM_YYYY
    val outPattern = if (isTruncated) DD_MMM else DD_MMM_YYYY
    SimpleDateFormat(outPattern).format(SimpleDateFormat(inPattern).parse(date))
} catch (e: Exception) {
    L.def().warn()
            .throwable(e)
            .log("unable to format date")
    date
}

/**
 * fucking vk format allows "d.M.yyyy" WTF???
 */
fun formatBdate(bdate: String?) = bdate
        ?.split(".")?.joinToString(".") {
            if (it.length == 1) "0$it" else it
        } ?: ""

fun getMinutes(): String {
    val minutes = Calendar.getInstance().get(Calendar.MINUTE)
    return when {
        minutes < 10 -> "0$minutes"
        else -> minutes.toString()
    }
}

fun time() = (System.currentTimeMillis() / 1000L).toInt()

private fun Date.isTheSameDay(date: Date): Boolean {
    return this.date == date.date &&
            month == date.month &&
            year == date.year
}