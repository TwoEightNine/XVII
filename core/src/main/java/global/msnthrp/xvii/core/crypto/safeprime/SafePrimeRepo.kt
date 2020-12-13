package global.msnthrp.xvii.core.crypto.safeprime

import global.msnthrp.xvii.core.crypto.safeprime.entity.SafePrime

interface SafePrimeRepo {

    fun getSafePrime(useCache: Boolean = false): SafePrime
}