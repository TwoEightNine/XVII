package com.twoeightnine.root.xvii.crypto

import android.util.Base64
import java.security.MessageDigest

fun md5Raw(plain: ByteArray): ByteArray = MessageDigest
        .getInstance("MD5")
        .digest(plain)

fun md5(plain: String) = md5Raw(plain.toByteArray())
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .joinToString(separator = "") { if (it.length == 2) it else "0$it" }

fun sha256Raw(plain: ByteArray): ByteArray = MessageDigest
        .getInstance("SHA-256")
        .digest(plain)

fun sha256(plain: String) = sha256Raw(plain.toByteArray())
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .joinToString(separator = "") { if (it.length == 2) it else "0$it" }

fun bytesToHex(bytes: ByteArray) = bytes
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .joinToString(separator = "") { if (it.length == 2) it else "0$it" }

fun getUiFriendlyHash(hash: String) = hash
        .mapIndexed { index, c -> if (index % 2 == 0) c.toString() else "$c " } // spaces
        .mapIndexed { index, s -> if (index % 16 == 15) "$s\n" else s } // new lines
        .joinToString(separator = "")

fun toBase64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE)

fun fromBase64(str: String): ByteArray = Base64.decode(str, Base64.NO_WRAP or Base64.URL_SAFE)