package com.twoeightnine.root.xvii.crypto

import android.content.Context
import android.content.SharedPreferences
import com.twoeightnine.root.xvii.managers.KeyStorage
import com.twoeightnine.root.xvii.utils.time

class CryptoStorage(private val context: Context) {

    private val pref: SharedPreferences by lazy {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    var prime
        get() = pref.getString(PRIME, DEFAULT_PRIME)
        set(value) = pref.edit().putString(PRIME, value).apply()

    var ts
        get() = pref.getInt(TS, 0)
        set(value) = pref.edit().putInt(TS, value).apply()

    fun saveKey(peerId: Int, key: String) {
        pref.edit().putString(getPeerKey(peerId), key).apply()
    }

    fun removeKey(peerId: Int) {
        pref.edit().remove(getPeerKey(peerId)).apply()
    }

    fun getKey(peerId: Int) = pref.getString(getPeerKey(peerId), null)

    fun hasKey(peerId: Int) = !getKey(peerId).isNullOrEmpty()

    fun isObsolete() = time() - ts > STORAGE_DURATION

    fun isDefault() = KeyStorage.prime == DEFAULT_PRIME

    private fun getPeerKey(peerId: Int) = "peer$peerId"

    companion object {
        private const val NAME = "cryptoStorage"

        private const val PRIME = "prime"
        private const val TS = "timeStamp"

        private const val DEFAULT_PRIME = "429960845873088536599738146849398890197656281978746052260302" +
                "647466290912363305996665498753182126120318295110244403964643426486915779918338905631403" +
                "871028184935255084712703423613529366297760543627384218281702559557613931230981025214701" +
                "436292843374882547050410898749274017561380961864641986822934974094546625703373934105581" +
                "804151000069136171694933799628596746360744089987859632744266134003621159571422862980144" +
                "043377717793249604329463406248840895370685965329721512935512704815510998868368597285047" +
                "022910731679048010756286396108128504010975404344672924191827643103234869173733652744217" +
                "51734483875743936086632531541480503"

        private const val STORAGE_DURATION = 3600 * 24 * 30
    }
}