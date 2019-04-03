package com.twoeightnine.root.xvii.crypto

import android.content.Context
import com.twoeightnine.root.xvii.crypto.cipher.Cipher
import com.twoeightnine.root.xvii.crypto.cipher.Pbkdf2HmacSha1

class CryptoEngine(
        private val context: Context,
        private val peerId: Int,
        private val testing: Boolean = false
) {

    /**
     * the place all the keys are stored in
     */
    private val storage = if (testing) CryptoStorage(context, "cryptoTest") else CryptoStorage(context)

    /**
     * the key for current [peerId]
     */
    private lateinit var key: ByteArray

    init {
        if (storage.hasKey(peerId)) {
            key = storage.getKey(peerId)
        }
    }

    /**
     * derives secure key from [userKey]
     * saves as [key] and into [storage]
     */
    fun setKey(userKey: String) {
        key = Pbkdf2HmacSha1.deriveFromKey(userKey)
        storage.saveKey(peerId, key)
    }

    fun encrypt(message: String): String {
        checkKey()

        val enc = Cipher.encrypt(message.toByteArray(), key)
        return wrapData(toBase64(enc))
    }

    fun decrypt(message: String): Cipher.Result {
        checkKey()

        val enc = fromBase64(unwrapData(message))
        return Cipher.decrypt(enc, key)
    }

    fun resetStorage() {
        storage.clear()
    }

    /**
     * check if [key] is set
     * if not throws [IllegalStateException]
     */
    private fun checkKey() {
        if (!::key.isInitialized) {
            throw IllegalStateException("No key provided!")
        }
    }

    companion object {

        const val DATA_PREFIX = "xvii{"
        const val DATA_POSTFIX = "}"
        const val KEY_PREFIX = "keyex{"
        const val KEY_POSTFIX = "}"

        const val EXTENSION = ".xvii"

        fun wrapData(text: String) = "$DATA_PREFIX$text$DATA_POSTFIX"

        fun unwrapData(text: String) = if (text.startsWith(DATA_PREFIX) && text.endsWith(DATA_POSTFIX)) {
            text.substring(DATA_PREFIX.length, text.length - DATA_POSTFIX.length)
        } else {
            text
        }

        fun wrapKey(text: String) = "$KEY_PREFIX$text$KEY_POSTFIX"

        fun unwrapKey(text: String) = if (text.startsWith(KEY_PREFIX) && text.endsWith(KEY_POSTFIX)) {
            text.substring(KEY_PREFIX.length, text.length - KEY_POSTFIX.length)
        } else {
            text
        }

    }
}