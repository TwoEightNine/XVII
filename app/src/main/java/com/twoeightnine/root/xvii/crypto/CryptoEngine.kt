package com.twoeightnine.root.xvii.crypto

import android.content.Context

class CryptoEngine(
        private val context: Context,
        private val peerId: Int
) {

    private val storage = CryptoStorage(context)




//    fun encrypt(message: String): String {
//
//    }

    companion object {

        const val DATA_PREFIX = "xvii{"
        const val DATA_POSTFIX = "}"
        const val KEY_PREFIX = "keyex{"
        const val KEY_POSTFIX = "}"

        const val EXTENSION = ".xvii"

    }
}