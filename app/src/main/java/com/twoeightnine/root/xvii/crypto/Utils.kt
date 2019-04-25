package com.twoeightnine.root.xvii.crypto

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

fun md5Raw(plain: ByteArray) = MessageDigest
        .getInstance("MD5")
        .digest(plain)

fun md5(plain: String) = md5Raw(plain.toByteArray())
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .map { if (it.length == 2) it else "0$it" }
        .joinToString(separator = "")

fun sha256Raw(plain: ByteArray) = MessageDigest
        .getInstance("SHA-256")
        .digest(plain)

fun sha256(plain: String) = sha256Raw(plain.toByteArray())
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .map { if (it.length == 2) it else "0$it" }
        .joinToString(separator = "")

fun bytesToHex(bytes: ByteArray) = bytes
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .map { if (it.length == 2) it else "0$it" }
        .joinToString(separator = "")

fun getUiFriendlyHash(hash: String) = hash
        .mapIndexed { index, c -> if (index % 2 == 0) c.toString() else "$c " } // spaces
        .mapIndexed { index, s -> if (index % 16 == 15) "$s\n" else s } // new lines
        .joinToString(separator = "")

fun getRandomBytes(numBytes: Int): ByteArray {
    val sr = SecureRandom()
    sr.setSeed(sr.generateSeed(numBytes))
    val bytes = ByteArray(numBytes)
    sr.nextBytes(bytes)
    return bytes
}

fun toBase64(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.DEFAULT)

fun fromBase64(str: String) = Base64.decode(str, Base64.DEFAULT)