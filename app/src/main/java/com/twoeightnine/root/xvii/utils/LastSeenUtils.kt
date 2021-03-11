package com.twoeightnine.root.xvii.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint


object LastSeenUtils {

    private const val SPAN_SIZE_FACTOR = 0.8f
    private const val TEXT_SIZE_DEFAULT = 28

    fun getFull(
            context: Context?,
            isOnline: Boolean,
            timeStamp: Int,
            deviceCode: Int,
            textSizePx: Int = TEXT_SIZE_DEFAULT,
            withSeconds: Boolean = Prefs.showSeconds
    ): CharSequence {
        if (context == null) return ""

        val deviceIconSpan = createDeviceIconSpan(context, deviceCode, textSizePx)

        val time = when (timeStamp) {
            0 -> time() - (if (isOnline) 0 else 300)
            else -> timeStamp
        }
        val stringRes = if (isOnline) R.string.online_seen else R.string.last_seen
        val lastSeen = context.getString(stringRes, getTime(time, withSeconds = withSeconds))

        return if (deviceIconSpan != null) {
            val start = lastSeen.length + 1
            val end = start + 1
            SpannableStringBuilder()
                    .append(lastSeen)
                    .append("\u2004 ")
                    .apply {
                        setSpan(deviceIconSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
        } else {
            lastSeen
        }
    }

    private fun createDeviceIconSpan(context: Context, deviceCode: Int, textSizePx: Int): ImageSpan? {
        val height = (textSizePx * SPAN_SIZE_FACTOR).toInt()
        return getDeviceIconRes(deviceCode)
                .takeIf { it != 0 }
                ?.let { ContextCompat.getDrawable(context, it) }
                ?.apply { setBounds(0, 0, height, height) }
                ?.apply { paint(Munch.color.color50) }
                ?.let { ImageSpan(it, ImageSpan.ALIGN_BASELINE) }
    }

    @DrawableRes
    private fun getDeviceIconRes(deviceCode: Int): Int {
        if (deviceCode !in 1..7) return 0

        return when(deviceCode) {
            1 -> 0// mobile
            2, 3 -> R.drawable.ic_apple_device
            4 -> R.drawable.ic_android_device
            5, 6 -> R.drawable.ic_windows_device
            else -> 0 // full or unknown
        }
    }
}