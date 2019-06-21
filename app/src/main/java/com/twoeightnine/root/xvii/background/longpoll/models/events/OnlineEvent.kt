package com.twoeightnine.root.xvii.background.longpoll.models.events

import android.content.Context
import com.twoeightnine.root.xvii.R

data class OnlineEvent(
        val userId: Int,
        val extras: Int,
        val timeStamp: Int
) : BaseLongPollEvent() {

    override fun getType() = TYPE_ONLINE

    fun getDeviceName(context: Context?): String {
        if (context == null) return ""

        val deviceCode = extras and 0xff
        return context.resources.getStringArray(R.array.devices)[deviceCode - 1]
    }

}