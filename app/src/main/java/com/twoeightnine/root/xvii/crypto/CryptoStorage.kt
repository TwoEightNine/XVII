package com.twoeightnine.root.xvii.crypto

import android.content.Context
import android.content.SharedPreferences

class CryptoStorage(
        private val context: Context,
        private val name: String = NAME
) {

    private val pref: SharedPreferences by lazy {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    fun saveKey(peerId: Int, key: ByteArray) {
        pref.edit().putString(getPeerKey(peerId), toBase64(key)).apply()
    }

    fun removeKey(peerId: Int) {
        pref.edit().remove(getPeerKey(peerId)).apply()
    }

    fun getKey(peerId: Int) = fromBase64(pref.getString(getPeerKey(peerId), null) ?: "")

    fun hasKey(peerId: Int) = getKey(peerId).isNotEmpty()

    private fun getPeerKey(peerId: Int) = "peer$peerId"

    fun clear() {
        pref.edit().clear().apply()
    }

    companion object {
        private const val NAME = "cryptoStorage"
    }
}