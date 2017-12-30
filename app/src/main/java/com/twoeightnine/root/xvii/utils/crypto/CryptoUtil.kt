package com.twoeightnine.root.xvii.utils.crypto

import android.content.Context
import android.provider.MediaStore
import android.util.Base64
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.utils.*
import io.reactivex.Flowable
import java.math.BigInteger

class CryptoUtil(private val uid: Int,
                 private val cid: Int) {

    lateinit var diffieHellman: DiffieHellman

    private var key256 = sha256Raw(getDefaultKey(uid, cid).toByteArray())
    private var aesIv = md5Raw(getDefaultKey(uid, cid).toByteArray())

    var isWaiting = false

    /**
     * generates g, p, A
     * returns KeyEx{g, p, A} to send
     */
    fun startKeyExchange(callback: (String) -> Unit) {
        Flowable.fromCallable({ xchg() })
                .compose(applySchedulers())
                .subscribe {
                    callback.invoke(it)
                }
    }

    private fun xchg(): String {
        Lg.i("start xchg")
        diffieHellman = DiffieHellman()
        val common = diffieHellman.getCommonData()
        return "KeyEx{${numToStr(common[0])},${numToStr(common[1])},${numToStr(common[2])}}"
    }

    /**
     * obtains B
     * generates key
     */
    fun finishKeyExchange(str: String) {
        val B = strToNums(str)[0]
        diffieHellman.publicOther = B
        updateKeys()
    }

    /**
     * obtains g, p, A
     * generates B, key
     * returns B
     */
    fun supportKeyExchange(str: String): String {
        val common = strToNums(str)
        diffieHellman = DiffieHellman(common[0], common[1], common[2])
        updateKeys()
        return "KeyEx{${numToStr(diffieHellman.publicOwn)}}"
    }

    fun printKey() {
        Lg.dbg("key ${diffieHellman.key}")
    }

    fun getFingerPrint(): String {
        printKeys()
        val hash = sha256("${bytesToHex(key256)}${bytesToHex(aesIv)}")
        Lg.i("fingerprint $hash")
        return hash
    }

    private fun numToStr(num: BigInteger) = Base64.encodeToString(num.toByteArray(), Base64.DEFAULT)

    private fun strToNums(str: String) = str.substring(6, str.length - 1)
            .split(",")
            .map { Base64.decode(it, Base64.NO_WRAP) }
            .map { BigInteger(it) }
            .toTypedArray()

    private fun getDefaultKey(uid: Int, cid: Int): String {
        if (cid < 0 || cid > 2000000000) {
            return "$cid"
        }
        return "${Math.min(uid, cid)}${Math.max(uid, cid)}"
    }

    private fun updateKeys() {
        printKey()
        val bytes = diffieHellman.key.toByteArray()

        key256 = sha256Raw(bytes)
        aesIv = md5Raw(bytes)
    }

    private fun bytesToString(bytes: ByteArray): String {
        var res = ""
        for (byte in bytes) {
            res += byte.toChar()
        }
        return res
    }

    fun resetKeys() {

        key256 = sha256Raw(getDefaultKey(uid, cid).toByteArray())
        aesIv = md5Raw(getDefaultKey(uid, cid).toByteArray())
    }

    fun printKeys() {
        Lg.dbg(bytesToHex(key256))
        Lg.dbg(bytesToHex(aesIv))
    }

    fun setUserKey(key: String) {
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

    fun encryptFileAsync(context: Context, path: String, callback: (String) -> Unit = {}) {
        val bytes = getBytesFromFile(context, path)
        Lg.i("enc: file size: ${bytes.size}. started ${time()}")
        Flowable.fromCallable { AES256Cipher.encrypt(aesIv, key256, bytes) }
                .compose(applySchedulers())
                .subscribe({
                    val resultName = "${getNameFromUrl(path)}$EXTENSION"
                    Lg.i("enc finished ${time()}")
                    callback.invoke(writeBytesToFile(context, it, resultName))
                })
    }

    fun decryptFileAsync(context: Context, path: String, callback: (String) -> Unit = {}) {
        val bytes = getBytesFromFile(context, path)
        Lg.i("dec: file size: ${bytes.size}. started ${time()}")
        Flowable.fromCallable {
            try {
                AES256Cipher.decrypt(aesIv, key256, bytes)
            } catch (e: Exception) {
                Lg.wtf("decrypting file ${e.message}")
                byteArrayOf()
            }
        }
                .compose(applySchedulers())
                .subscribe({
                    val resultName = getNameFromUrl(path).replace(EXTENSION, "")
                    if (it.isNotEmpty()) {
                        Lg.i("dec finished ${time()}")
                        callback.invoke(writeBytesToFile(context, it, resultName))
                    } else {
                        Lg.i("dec failed ${time()}")
                        callback.invoke("")
                    }
                })
    }

    companion object {

        val PREFIX = "XVII{"
        val POSTFIX = "}"

        val EXTENSION = ".xvii"

    }
}