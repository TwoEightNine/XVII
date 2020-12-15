package global.msnthrp.xvii.core.crypto.engine

import android.annotation.SuppressLint
import global.msnthrp.xvii.core.crypto.CryptoConsts
import global.msnthrp.xvii.core.crypto.CryptoUtils
import global.msnthrp.xvii.core.crypto.algorithm.Cipher
import global.msnthrp.xvii.core.crypto.algorithm.DiffieHellman
import global.msnthrp.xvii.core.crypto.algorithm.Pbkdf2HmacSha1
import global.msnthrp.xvii.core.crypto.safeprime.SafePrimeUseCase
import java.io.File
import java.math.BigInteger

class CryptoEngineUseCase(
        private val peerId: Int,
        private val safePrimeUseCase: SafePrimeUseCase,
        private val cryptoEngineRepo: CryptoEngineRepo,
        private val cryptoEngineEncoder: CryptoEngineEncoder,
        private val cryptoEngineFileSource: CryptoEngineFileSource
) {

    /**
     * the key for current [peerId]
     */
    private lateinit var key: ByteArray

    /**
     * provides asymmetric key exchange
     */
    private lateinit var dh: DiffieHellman

    init {
        cryptoEngineRepo.getKeyOrNull(peerId)?.also { keyBytes ->
            key = keyBytes
        }
    }

    /**
     * used to check if [CryptoEngineUseCase] can be used
     */
    fun isKeyRequired() = !::key.isInitialized

    /**
     * check if [key] is set
     * @throws IllegalStateException if key is not set
     */
    private fun checkKey() {
        if (isKeyRequired()) {
            throw IllegalStateException("No key provided!")
        }
    }

    /**
     * derives secure key from [userKey]
     * saves as [key] and into [cryptoEngineRepo]
     */
    fun setKey(userKey: String, save: Boolean = true) {
        key = Pbkdf2HmacSha1.deriveFromKey(userKey)
        if (save) {
            cryptoEngineRepo.setKey(peerId, key)
        }
    }

    /**
     * removes all the keys and a prime
     */
    fun resetStorage() {
        cryptoEngineRepo.clearAll()
    }

    /**
     * initiates key exchange
     * @return key exchange string to send in message
     * @see [DiffieHellman.Data], [DiffieHellman]
     */
    @SuppressLint("CheckResult")
    fun startExchange(): String {
        dh = DiffieHellman(safePrimeUseCase.loadSafePrime())
        val data = dh.getData()
        return wrapKey(data.serialize())
    }

    /**
     * supports exchange, receives [DiffieHellman.Data], obtains [key]
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
    }

    /**
     * encrypts [message]
     * @return
     */
    fun encrypt(message: String): String {
        checkKey()

        val enc = Cipher.encrypt(message.toByteArray(), key)
        return wrapData(cryptoEngineEncoder.encode(enc))
    }

    /**
     * decrypts [message] and verifies it
     * @return decrypted message, or null if corrupted
     * @throws IllegalStateException if key is not set
     */
    fun decrypt(message: String): String? {
        checkKey()

        val cipherResult = try {
            val enc = cryptoEngineEncoder.decode(unwrapData(message))
            Cipher.decrypt(enc, key)
        } catch (e: Exception) {
            Cipher.Result(verified = false)
        }

        return cipherResult.ifVerifiedOrNull(::String)
    }

    /**
     * encrypts content of [file] and saves it into new one
     * @return new file or null in case of failure
     */
    fun encryptFile(file: File): File? {
        checkKey()

        val plainBytes = cryptoEngineFileSource.readFromFile(file) ?: return null
        val cipherBytes = Cipher.encrypt(plainBytes, key)

        val resultName = file.name + CryptoConsts.EXTENSION
        return cryptoEngineFileSource.writeToFile(resultName, cipherBytes)
    }

    /**
     * decrypts content of [file] and saves it to new one
     * @return file in case of success, or null otherwise
     */
    @SuppressLint("CheckResult")
    fun decryptFile(file: File): File? {
        checkKey()

        val cipherBytes = cryptoEngineFileSource.readFromFile(file) ?: return null
        val cipherResult = Cipher.decrypt(cipherBytes, key)

        return cipherResult.ifVerifiedOrNull { plainBytes ->
            val resultName = file.name.replace(CryptoConsts.EXTENSION, "")
            cryptoEngineFileSource.writeToFile(resultName, plainBytes)
        }
    }

    /**
     * returns hash of key as its fingerprint
     */
    fun getFingerPrint(): ByteArray {
        checkKey()
        return CryptoUtils.sha256(key)
    }

    private fun <T> Cipher.Result.ifVerifiedOrNull(runnable: (ByteArray) -> T): T? {
        return when {
            !verified || bytes == null -> null
            else -> runnable(bytes)
        }
    }

    private fun DiffieHellman.Data.serialize(): String =
            "${cryptoEngineEncoder.encode(modulo.toByteArray())}," +
                    "${cryptoEngineEncoder.encode(generator.toByteArray())}," +
                    cryptoEngineEncoder.encode(public.toByteArray())

    private fun String.deserialize(): DiffieHellman.Data {
        val numArr = split(",")
                .map(cryptoEngineEncoder::decode)
                .map(::BigInteger)
                .toTypedArray()
        return DiffieHellman.Data(
                modulo = numArr[0],
                generator = numArr[1],
                public = numArr[2]
        )
    }

    private fun numToStr(num: BigInteger) = cryptoEngineEncoder.encode(num.toByteArray())

    private fun strToNum(str: String) = BigInteger(cryptoEngineEncoder.decode(str))

    private fun wrapData(text: String) = "${CryptoConsts.DATA_PREFIX}$text${CryptoConsts.DATA_POSTFIX}"

    private fun unwrapData(text: String) = when {
        text.startsWith(CryptoConsts.DATA_PREFIX) && text.endsWith(CryptoConsts.DATA_POSTFIX) -> {
            text.substring(CryptoConsts.DATA_PREFIX.length, text.length - CryptoConsts.DATA_POSTFIX.length)
        }
        else -> text
    }

    private fun wrapKey(text: String) = "${CryptoConsts.KEY_PREFIX}$text${CryptoConsts.KEY_POSTFIX}"

    private fun unwrapKey(text: String) = when {
        text.startsWith(CryptoConsts.KEY_PREFIX) && text.endsWith(CryptoConsts.KEY_POSTFIX) -> {
            text.substring(CryptoConsts.KEY_PREFIX.length, text.length - CryptoConsts.KEY_POSTFIX.length)
        }
        else -> text
    }
}