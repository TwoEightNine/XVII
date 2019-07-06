package com.twoeightnine.root.xvii.crypto

import android.os.Build
import com.twoeightnine.root.xvii.lg.Lg
import java.math.BigInteger
import kotlin.experimental.inv
import kotlin.experimental.xor

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
                var prime = BigInteger(data.substring(13, 23), 16).setBit(0)
                val two = BigInteger.valueOf(2L)
                while (!isPrime(prime)) prime += two
                var num = BigInteger(data.substring(2, 9), 16)
                num = num.modPow(BigInteger(data.substring(0, 2), 16), prime)
                keyInternal = sha256Raw(num.toByteArray())
                for (i in 1 until 313) {
                    var r = i
                    keyInternal[r % 32] = (keyInternal[(r + 13) % 32]
                            xor keyInternal[(r + 11) % 32]).inv()
                    r *= i
                    keyInternal[r % 32] = (keyInternal[(r + 7) % 32]
                            xor keyInternal[(r + 17) % 32]).inv()
                }
//                keyInternal = data.toByteArray()
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