package com.twoeightnine.root.xvii.crypto

import android.annotation.SuppressLint
import android.content.Context
import com.twoeightnine.root.xvii.crypto.cipher.Cipher
import com.twoeightnine.root.xvii.crypto.cipher.Pbkdf2HmacSha1
import com.twoeightnine.root.xvii.crypto.dh.DhData
import com.twoeightnine.root.xvii.crypto.dh.DiffieHellman
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import com.twoeightnine.root.xvii.utils.crypto.CryptoUtil
import com.twoeightnine.root.xvii.utils.getBytesFromFile
import com.twoeightnine.root.xvii.utils.getNameFromUrl
import com.twoeightnine.root.xvii.utils.writeBytesToFile
import io.reactivex.Single
import java.math.BigInteger

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

    /**
     * provides asymmetric key exchange
     */
    private lateinit var dh: DiffieHellman

    init {
        if (storage.hasKey(peerId)) {
            key = storage.getKey(peerId)
        }
    }

    /**
     * used to check if [CryptoEngine] can be used
     */
    fun isKeyRequired() = !::key.isInitialized

    /**
     * check if [key] is set
     * if not throws [IllegalStateException]
     */
    private fun checkKey() {
        if (isKeyRequired()) {
            throw IllegalStateException("No key provided!")
        }
    }

    /**
     * derives secure key from [userKey]
     * saves as [key] and into [storage]
     */
    fun setKey(userKey: String, save: Boolean = true) {
        key = Pbkdf2HmacSha1.deriveFromKey(userKey)
        if (save) {
            storage.saveKey(peerId, key)
        }
    }

    /**
     * removes all the keys and a prime
     */
    fun resetStorage() {
        storage.clear()
    }

    /**
     * initiates key exchange
     * @see [DhData], [DiffieHellman]
     */
    @SuppressLint("CheckResult")
    fun startExchange(onKeysGenerated: (String) -> Unit) {
        Single.fromCallable {
            dh = DiffieHellman(BigInteger(storage.prime))
            val dhData = dh.getDhData()
            wrapKey(DhData.serialize(dhData))
        }
                .compose(applySingleSchedulers())
                .subscribe(onKeysGenerated)
    }

    /**
     * supports exchange, receives [DhData], obtains [key]
     * returns own public nonce
     */
    fun supportExchange(keyEx: String): String {
        val dhData = DhData.deserialize(unwrapKey(keyEx))
        dh = DiffieHellman(dhData)
        setKey(dh.key.toString())
        return wrapKey(numToStr(dh.publicOwn))
    }

    /**
     * finishes the exchange, receive other public nonce, obtains [key]
     */
    fun finishExchange(publicOtherWrapped: String) {
        val publicOther = strToNum(publicOtherWrapped)
        dh.publicOther = publicOther
        setKey(dh.key.toString())
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

    @SuppressLint("CheckResult")
    fun encryptFile(context: Context, path: String, onEncrypted: (String) -> Unit) {
        Single.fromCallable {
            val bytes = getBytesFromFile(context, path)
            Cipher.encrypt(bytes, key)
        }
                .compose(applySingleSchedulers())
                .subscribe { cipher ->
                    val resultName = "${getNameFromUrl(path)}${CryptoUtil.EXTENSION}"
                    val cipherPath = writeBytesToFile(context, cipher, resultName)
                    onEncrypted(cipherPath)
                }
    }

    /**
     * returns:
     *      - flag is file is verified
     *      - path of decrypted file
     */
    @SuppressLint("CheckResult")
    fun decryptFile(context: Context, path: String, onDecrypted: (Boolean, String?) -> Unit) {
        Single.fromCallable {
            val bytes = getBytesFromFile(context, path)
            Cipher.decrypt(bytes, key)
        }
                .compose(applySingleSchedulers())
                .subscribe { cipherResult ->
                    if (!cipherResult.verified || cipherResult.bytes == null) {
                        onDecrypted(false, null)
                    } else {
                        val resultName = "${getNameFromUrl(path)}${CryptoUtil.EXTENSION}"
                        val cipherPath = writeBytesToFile(context, cipherResult.bytes, resultName)
                        onDecrypted(true, cipherPath)
                    }
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

        fun numToStr(num: BigInteger) = toBase64(num.toByteArray())

        fun strToNum(str: String) = BigInteger(fromBase64(str))

    }
}