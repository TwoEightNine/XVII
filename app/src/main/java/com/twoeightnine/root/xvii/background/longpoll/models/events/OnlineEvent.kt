package com.twoeightnine.root.xvii.background.longpoll.models.events

import android.content.res.Resources
import com.twoeightnine.root.xvii.R

data class OnlineEvent(
        val userId: Int,
        val extras: Int,
        val timeStamp: Int
) : BaseLongPollEvent() {

    override fun getType() = TYPE_ONLINE

    val deviceCode: Int
        get() = extras and 0xff

    companion object {

        fun getDeviceName(resources: Resources?, deviceCode: Int): String {
            if (resources == null || deviceCode !in 1..7) return ""

            return resources.getStringArray(R.array.devices)[deviceCode - 1]
        }

    }

}