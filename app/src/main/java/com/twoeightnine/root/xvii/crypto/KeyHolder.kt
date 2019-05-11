package com.twoeightnine.root.xvii.crypto

import android.os.Build
import com.twoeightnine.root.xvii.lg.Lg

object KeyHolder {

    private var keyInternal = "areYouKiddingMe?".toByteArray()

    val key: ByteArray
        get() = keyInternal

    fun reinit() {
        Thread {
            try {
                val data = sha256(StringBuilder()
                        .append(Build.MANUFACTURER)
                        .append(Build.BRAND)
                        .append(Build.MODEL)
                        .toString())

                keyInternal = data.toByteArray()
                l("key restored")
            } catch (e: Exception) {
                e.printStackTrace()
                l("error: ${e.message}")
            }
        }.start()
    }

    private fun l(s: String) {
        Lg.i("[key holder] $s")
    }
}