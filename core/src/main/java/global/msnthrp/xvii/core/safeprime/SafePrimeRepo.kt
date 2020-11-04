package global.msnthrp.xvii.core.safeprime

import global.msnthrp.xvii.core.safeprime.entity.SafePrime

interface SafePrimeRepo {

    fun getSafePrime(useCache: Boolean = false): SafePrime?
}