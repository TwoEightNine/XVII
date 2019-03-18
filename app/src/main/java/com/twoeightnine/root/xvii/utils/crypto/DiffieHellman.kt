package com.twoeightnine.root.xvii.utils.crypto

import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.KeyStorage
import java.math.BigInteger
import java.util.*

class DiffieHellman(generate: Boolean = true) {

    private val bits = 2048

    private var privateOwn = BigInteger.ONE
    var generator = BigInteger.ONE
    var modulo = BigInteger.ONE
    var publicOwn = BigInteger.ONE
    var publicOther = BigInteger.ONE
        set(value) {
            field = value
            key = value.modPow(privateOwn, modulo)
        }
    var key = BigInteger.ONE
        private set

    init {
        privateOwn = BigInteger(bits - 1, Random())

        if (generate) {
            modulo = BigInteger(KeyStorage.prime)
            val q2048 = BigInteger(KeyStorage.halfPrime)

            var x: BigInteger
            do {
                x = BigInteger(32, Random())
            } while (x == BigInteger.ZERO || !isPrimeRoot(x, q2048, modulo))
            generator = x
            publicOwn = generator.modPow(privateOwn, modulo)
        }
        Lg.dbg("DHE-$bits: p: $modulo\n\t\t\t g: $generator\n\t\t\t a: $privateOwn\n\t\t\t A: $publicOwn")
    }

    constructor(generator: BigInteger, modulo: BigInteger, publicOther: BigInteger) : this(false) {
        this.generator = generator
        this.modulo = modulo
        this.publicOther = publicOther
        publicOwn = generator.modPow(privateOwn, modulo)
        key = publicOther.modPow(privateOwn, modulo)
    }

    fun getCommonData() = arrayOf(generator, modulo, publicOwn)

    private fun isPrimeRoot(g: BigInteger, q: BigInteger, p: BigInteger): Boolean {
        return g.modPow(BigInteger.ONE, p) != BigInteger.ONE &&
                g.modPow(BigInteger.valueOf(2), p) != BigInteger.ONE &&
                g.modPow(q, p) != BigInteger.ONE

    }

}