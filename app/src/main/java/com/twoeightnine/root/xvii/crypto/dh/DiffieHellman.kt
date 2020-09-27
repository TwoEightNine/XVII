package com.twoeightnine.root.xvii.crypto.dh

import com.twoeightnine.root.xvii.lg.L
import java.math.BigInteger
import java.util.*

/**
 * usage:
 * <code>
 *      // as generator
 *      val dh = DiffieHellman()
 *      val dhData = dh.getDhData()
 *      // send dhData to other
 *      // receive other public nonce
 *      dh.publicOther = publicOther
 *      val sharedKey = dh.key
 *
 *
 *      // as receiver
 *      // other dhData received
 *      val dh = DiffeHellman(dhData)
 *      val publicOwn = dh.publicOwn
 *      // send publicOwn to other
 *      val key = dh.key
 *      // shared key
 *
 * </code>
 */
class DiffieHellman {

    private var privateOwn = BigInteger.ONE
    private var generator = BigInteger.ONE
    private var modulo = BigInteger.ONE

    var publicOwn: BigInteger = BigInteger.ONE
        private set

    /**
     * sets other public nonce, computes shared key
     * used in case of own generating
     */
    var publicOther: BigInteger = BigInteger.ONE
        set(value) {
            field = value
            key = value.modPow(privateOwn, modulo)
        }

    var key: BigInteger = BigInteger.ONE
        private set

    /**
     * generates own private nonce, finds generator, creates own public nonce
     */
    constructor(modulo: BigInteger) {
        this.modulo = modulo
        val halfModulo = (modulo - BigInteger.ONE) / BigInteger("2")
        privateOwn = BigInteger(BITS - 1, Random())

        var x: BigInteger
        do {
            x = BigInteger(32, Random())
        } while (x == BigInteger.ZERO || !isPrimeRoot(x, halfModulo, modulo))
        generator = x
        publicOwn = generator.modPow(privateOwn, modulo)

        L.tag(TAG).debug().log("DHE-$BITS: p: $modulo\n\t\t\t g: $generator\n\t\t\t a: $privateOwn\n\t\t\t A: $publicOwn")
    }

    /**
     * gets generator and other public nonce, generates own private and public nonces,
     * creates shared key
     */
    constructor(otherData: DhData) {
        this.generator = otherData.generator
        this.modulo = otherData.modulo
        this.publicOther = otherData.public

        privateOwn = BigInteger(BITS - 1, Random())
        publicOwn = generator.modPow(privateOwn, modulo)
        key = publicOther.modPow(privateOwn, modulo)

        L.tag(TAG).debug().log("DHE-$BITS: p: $modulo\n\t\t\t g: $generator\n\t\t\t a: $privateOwn\n\t\t\t A: $publicOwn")
    }

    fun getDhData() = DhData(generator, modulo, publicOwn)

    private fun isPrimeRoot(g: BigInteger, q: BigInteger, p: BigInteger): Boolean {
        return g.modPow(BigInteger.ONE, p) != BigInteger.ONE &&
                g.modPow(BigInteger.valueOf(2), p) != BigInteger.ONE &&
                g.modPow(q, p) != BigInteger.ONE

    }

    companion object {
        private const val TAG = "diffie-hellman"
        private const val BITS = 2048
    }

}