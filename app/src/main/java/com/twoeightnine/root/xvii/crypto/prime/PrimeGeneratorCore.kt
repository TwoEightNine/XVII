package com.twoeightnine.root.xvii.crypto.prime

import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.KeyStorage
import com.twoeightnine.root.xvii.utils.applySchedulers
import com.twoeightnine.root.xvii.utils.getTime
import com.twoeightnine.root.xvii.utils.time
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

/**
 * Created by fuckyou on 12.12.2017.
 * generates safe prime for DH-2048
 */

class PrimeGeneratorCore {

    private val secureRandom = SecureRandom()

    private val composite = CompositeDisposable()
    private var isCancelled = false

    fun run() {
        if (KeyStorage.isDefault() || KeyStorage.isObsolete()) {
            for (i in 1..2) {
                val s = Flowable.fromCallable { generate(i) }
                        .compose(applySchedulers())
                        .subscribe({
                            l("done")
                            isCancelled = true
                            composite.dispose()
                        }, {
                            lw("error occurred: $it")
                            it.printStackTrace()
                        })
                composite.add(s)
            }
        } else {
            l("already generated at ${getTime(KeyStorage.ts, full = true)}")
        }
    }

    private fun generate(threadNumber: Int) {
        l("start to generate custom primes", threadNumber)
        var q: BigInteger
        var p: BigInteger
        var bp: BigInteger
        val startTime = System.currentTimeMillis()
        var trie = 0
        do {
            p = BigInteger(BITS, 30, Random())
            bp = p.multiply(BigInteger.valueOf(2L)).add(BigInteger.ONE)
            q = p.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2L))
            trie += 2
            if (trie % 10 == 0) {
                l("tries $trie", threadNumber)
            }
        } while (!isPrime(q)  && !isPrime(bp) && !isCancelled)
        if (!isCancelled) {
            if (isPrime(q)) {
                KeyStorage.prime = p.toString()
                KeyStorage.halfPrime = q.toString()
            } else {
                KeyStorage.prime = bp.toString()
                KeyStorage.halfPrime = p.toString()
            }
            l("found for ${(System.currentTimeMillis() - startTime) / 1000}s with $trie tries", threadNumber)
            KeyStorage.ts = time()
        } else {
            l("cancelled", threadNumber)
        }
    }

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
        for (repeat in 0..29) {
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

    private fun isPrime(r: BigInteger) = millerRabin(r)

    private fun l(s: String, i: Int = 0) = Lg.i("[prime${if (i != 0) " $i" else ""}] $s")

    private fun lw(s: String) = Lg.wtf("[prime] $s")

    companion object {
        private const val BITS = 2048
    }
}