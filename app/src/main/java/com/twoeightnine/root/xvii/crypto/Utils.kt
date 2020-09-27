package com.twoeightnine.root.xvii.crypto

import android.util.Base64
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

const val PRIME_TEST_PROBABILITY = 30

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

fun getRandomBytes(numBytes: Int): ByteArray {
    val sr = SecureRandom()
    sr.setSeed(sr.generateSeed(numBytes))
    val bytes = ByteArray(numBytes)
    sr.nextBytes(bytes)
    return bytes
}

fun toBase64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE)

fun fromBase64(str: String): ByteArray = Base64.decode(str, Base64.NO_WRAP or Base64.URL_SAFE)

fun isPrime(r: BigInteger) = millerRabin(r)

private fun millerRabinPass(a: BigInteger, n: BigInteger): Boolean {
    val nMin1 = n.subtract(BigInteger.ONE)
    var d = nMin1
    val s = d.lowestSetBit
    d = d.shiftRight(s)
    var aToPow = a.modPow(d, n)
    if (aToPow == BigInteger.ONE) return true
    for (i in 0..s - 2) {
        if (aToPow == nMin1) return true
        aToPow = aToPow.multiply(aToPow).mod(n)
    }
    return (aToPow == nMin1)
}

private fun millerRabin(n: BigInteger): Boolean {
    val secureRandom = SecureRandom()
    for (repeat in 0 until PRIME_TEST_PROBABILITY) {
        var a: BigInteger
        do {
            a = BigInteger(n.bitLength(), secureRandom)
        } while (a == BigInteger.ZERO)
        if (!millerRabinPass(a, n)) {
            return false
        }
    }
    return true
}
