package com.twoeightnine.root.xvii.utils.crypto

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.KeyStorage
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.*
import io.reactivex.Flowable
import java.math.BigInteger

/**
 * @param userId current user
 * @param chatId peerId of chat
 */
class CryptoUtil(
        private val userId: Int,
        private val chatId: Int
) {

    private lateinit var dh: DiffieHellman

    private var key256 = sha256Raw(getStartingKey(userId, chatId).toByteArray())
    private var aesIv = md5Raw(getStartingKey(userId, chatId).toByteArray())

    var isWaiting = false
    lateinit var keyType: KeyType
        private set

    /**
     * generates g, p, A
     * returns KeyEx{g, p, A} to send
     */
    @SuppressLint("CheckResult")
    fun startKeyExchange(callback: (String) -> Unit) {
        Flowable.fromCallable { xchg() }
                .compose(applySchedulers())
                .subscribe {
                    callback.invoke(it)
                }
    }

    private fun xchg(): String {
        l("start exchange")
        dh = DiffieHellman()
        val common = dh.getCommonData()
        return "KeyEx{${numToStr(common[0])},${numToStr(common[1])},${numToStr(common[2])}}"
    }

    /**
     * obtains B
     * generates key
     */
    fun finishKeyExchange(str: String) {
        val B = strToNums(str)[0]
        dh.publicOther = B
        updateKeys()
        keyType = KeyType.RANDOM
    }

    /**
     * obtains g, p, A
     * generates B, key
     * returns B
     */
    fun supportKeyExchange(str: String): String {
        val common = strToNums(str)
        dh = DiffieHellman(common[0], common[1], common[2])
        updateKeys()
        keyType = KeyType.RANDOM
        return "KeyEx{${numToStr(dh.publicOwn)}}"
    }

    fun printKey() {
        ld("key = ${dh.key}")
    }

    fun getFingerPrint(): String {
        printKeys()
        val hash = sha256("${bytesToHex(key256)}${bytesToHex(aesIv)}")
        l("fingerprint $hash")
        return hash
    }

    private fun numToStr(num: BigInteger) = Base64.encodeToString(num.toByteArray(), Base64.DEFAULT)

    private fun strToNums(str: String) = str.substring(6, str.length - 1)
            .split(",")
            .map { Base64.decode(it, Base64.NO_WRAP) }
            .map { BigInteger(it) }
            .toTypedArray()

    /**
     * @param uid id of current user
     * @param cid peerId of chat
     */
    private fun getDefaultKey(uid: Int, cid: Int): String {
        if (cid < 0 || cid > 2000000000) {
            return "$cid"
        }
        return "${Math.min(uid, cid)}${Math.max(uid, cid)}"
    }

    private fun getStartingKey(uid: Int, cid: Int): String {
        val savedKey = KeyStorage.getCustomKey(cid)
        return if (savedKey == null) {
            keyType = KeyType.DEFAULT
            getDefaultKey(uid, cid)
        } else {
            keyType = KeyType.CUSTOM
            savedKey
        }
    }

    private fun updateKeys() {
        printKey()
        val bytes = dh.key.toByteArray()

        key256 = sha256Raw(bytes)
        aesIv = md5Raw(bytes)
    }

    fun resetKeys() {
        keyType = KeyType.DEFAULT
        KeyStorage.removeCustomKey(chatId)
        key256 = sha256Raw(getDefaultKey(userId, chatId).toByteArray())
        aesIv = md5Raw(getDefaultKey(userId, chatId).toByteArray())
    }

    fun printKeys() {
        ld("key256 = ${bytesToHex(key256)}")
        ld("iv = ${bytesToHex(aesIv)}")
    }

    fun setUserKey(key: String) {
        if (Prefs.storeCustomKeys) {
            KeyStorage.saveCustomKey(chatId, key)
        } else {
            KeyStorage.removeCustomKey(chatId)
        }
        keyType = KeyType.CUSTOM
        key256 = sha256Raw(key.toByteArray())
        aesIv = md5Raw(key.toByteArray())
    }

    fun encrypt(text: String) = "$PREFIX${Base64.encodeToString(AES256Cipher.encrypt(aesIv, key256, text.toByteArray()), Base64.NO_WRAP)}$POSTFIX"

    fun decrypt(cipher: String): String {
        val prepared = if (cipher.matchesXviiKey()) {
            cipher.substring(PREFIX.length, cipher.length - POSTFIX.length)
        } else {
            cipher
        }
        return try {
            String(AES256Cipher.decrypt(aesIv, key256, Base64.decode(prepared, Base64.NO_WRAP)))
        } catch (e: Exception) {
            ""
        }
    }

    @SuppressLint("CheckResult")
    fun encryptFileAsync(context: Context, path: String, callback: (String) -> Unit = {}) {
        val bytes = getBytesFromFile(context, path)
        l("enc: file size: ${bytes.size}. started ${time()}")
        Flowable.fromCallable { AES256Cipher.encrypt(aesIv, key256, bytes) }
                .compose(applySchedulers())
                .subscribe {
                    val resultName = "${getNameFromUrl(path)}$EXTENSION"
                    l("enc finished ${time()}")
                    callback.invoke(writeBytesToFile(context, it, resultName))
                }
    }

    @SuppressLint("CheckResult")
    fun decryptFileAsync(context: Context, path: String, callback: (String) -> Unit = {}) {
        val bytes = getBytesFromFile(context, path)
        l("dec: file size: ${bytes.size}. started ${time()}")
        Flowable.fromCallable {
            try {
                AES256Cipher.decrypt(aesIv, key256, bytes)
            } catch (e: Exception) {
                lw("dec error ${e.message}")
                byteArrayOf()
            }
        }
                .compose(applySchedulers())
                .subscribe {
                    val resultName = getNameFromUrl(path).replace(EXTENSION, "")
                    if (it.isNotEmpty()) {
                        l("dec finished ${time()}")
                        callback.invoke(writeBytesToFile(context, it, resultName))
                    } else {
                        lw("dec failed ${time()}")
                        callback.invoke("")
                    }
                }
    }

    private fun l(s: String) {
        Lg.i("[crypto] $s")
    }

    private fun lw(s: String) {
        Lg.wtf("[crypto] $s")
    }

    private fun ld(s: String) {
        Lg.dbg("[crypto] $s")
    }

    companion object {

        const val PREFIX = "XVII{"
        const val POSTFIX = "}"

        const val EXTENSION = ".xvii"

    }

    enum class KeyType(val stringRes: Int) {
        DEFAULT(R.string.default_key_type),
        CUSTOM(R.string.custom_key_type),
        RANDOM(R.string.random_key_type)
    }
}