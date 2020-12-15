package com.twoeightnine.root.xvii.crypto

import java.security.MessageDigest

fun md5(plain: String) = md5Raw(plain.toByteArray())
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .joinToString(separator = "") { if (it.length == 2) it else "0$it" }

fun sha256(plain: String) = sha256Raw(plain.toByteArray())
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .joinToString(separator = "") { if (it.length == 2) it else "0$it" }

private fun md5Raw(plain: ByteArray): ByteArray = MessageDigest
        .getInstance("MD5")
        .digest(plain)

private fun sha256Raw(plain: ByteArray): ByteArray = MessageDigest
        .getInstance("SHA-256")
        .digest(plain)
