package com.twoeightnine.root.xvii.crypto

import android.annotation.SuppressLint
import android.content.Context
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import com.twoeightnine.root.xvii.utils.getBytesFromFile
import com.twoeightnine.root.xvii.utils.getNameFromUrl
import com.twoeightnine.root.xvii.utils.writeBytesToFile
import global.msnthrp.xvii.core.crypto.algorithm.Cipher
import global.msnthrp.xvii.core.crypto.algorithm.DiffieHellman
import global.msnthrp.xvii.core.crypto.algorithm.Pbkdf2HmacSha1
import global.msnthrp.xvii.core.crypto.safeprime.DefaultSafePrimeUseCase
import global.msnthrp.xvii.core.crypto.safeprime.SafePrimeUseCase
import global.msnthrp.xvii.data.safeprime.DefaultSafePrimeRepo
import global.msnthrp.xvii.data.safeprime.storage.PreferencesSafePrimeDataSource
import global.msnthrp.xvii.data.safeprime.storage.retrofit.RetrofitSafePrimeDataSource
import io.reactivex.Single
import java.math.BigInteger

class CryptoEngine(
        context: Context,
        private val peerId: Int,
        testing: Boolean = false
) {

    var keyType = KeyType.DEFAULT
        private set

    /**
     * the place all the keys are stored in
     */
    private val storage = if (testing) CryptoStorage(context, "cryptoTest") else CryptoStorage(context)

    private val safePrimeUseCase: SafePrimeUseCase = DefaultSafePrimeUseCase(
            DefaultSafePrimeRepo(RetrofitSafePrimeDataSource(), PreferencesSafePrimeDataSource(context))
    )

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
            keyType = KeyType.CUSTOM
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
        keyType = KeyType.CUSTOM
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
            dh = DiffieHellman(safePrimeUseCase.loadSafePrime())
            val data = dh.getData()
            wrapKey(data.serialize())
        }
                .compose(applySingleSchedulers())
                .subscribe(onKeysGenerated)
    }

    /**
     * supports exchange, receives [DhData], obtains [key]
     * returns own public nonce
     */
    fun supportExchange(keyEx: String): String {
        val dhData = unwrapKey(keyEx).deserialize()
        dh = DiffieHellman(dhData)
        setKey(dh.key.toString())
        return wrapKey(numToStr(dh.publicOwn))
    }

    /**
     * finishes the exchange, receive other public nonce, obtains [key]
     */
    fun finishExchange(publicOtherWrapped: String) {
        val publicOther = strToNum(unwrapKey(publicOtherWrapped))
        dh.publicOther = publicOther
        setKey(dh.key.toString())
        keyType = KeyType.RANDOM
    }

    fun encrypt(message: String): String {
        checkKey()

        val enc = Cipher.encrypt(message.toByteArray(), key)
        return wrapData(toBase64(enc))
    }

    fun decrypt(message: String): Cipher.Result {
        checkKey()
        return try {
            val enc = fromBase64(unwrapData(message))
            Cipher.decrypt(enc, key)
        } catch (e: Exception) {
            Cipher.Result(verified = false)
        }
    }

    @SuppressLint("CheckResult")
    fun encryptFile(context: Context, path: String, onEncrypted: (String) -> Unit) {
        checkKey()

        Single.fromCallable {
            val bytes = getBytesFromFile(context, path)
            Cipher.encrypt(bytes, key)
        }
                .compose(applySingleSchedulers())
                .subscribe { cipher ->
                    val resultName = "${getNameFromUrl(path)}$EXTENSION"
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
        checkKey()

        Single.fromCallable {
            val bytes = getBytesFromFile(context, path)
            Cipher.decrypt(bytes, key)
        }
                .compose(applySingleSchedulers())
                .subscribe { cipherResult ->
                    val bytes = cipherResult.bytes
                    if (!cipherResult.verified || bytes == null) {
                        onDecrypted(false, null)
                    } else {
                        val resultName = getNameFromUrl(path).replace(EXTENSION, "")
                        val cipherPath = writeBytesToFile(context, bytes, resultName)
                        onDecrypted(true, cipherPath)
                    }
                }
    }

    fun getFingerPrint(): String {
        checkKey()
        return sha256(bytesToHex(key))
    }

    private fun DiffieHellman.Data.serialize(): String =
            "${toBase64(modulo.toByteArray())}," +
                    "${toBase64(generator.toByteArray())}," +
                    toBase64(public.toByteArray())

    private fun String.deserialize(): DiffieHellman.Data {
        val numArr = split(",")
                .map { fromBase64(it) }
                .map { BigInteger(it) }
                .toTypedArray()
        return DiffieHellman.Data(
                modulo = numArr[0],
                generator = numArr[1],
                public = numArr[2]
        )
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

    enum class KeyType(val stringRes: Int) {
        DEFAULT(R.string.default_key_type),
        CUSTOM(R.string.custom_key_type),
        RANDOM(R.string.random_key_type)
    }
}