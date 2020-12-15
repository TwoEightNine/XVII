package global.msnthrp.xvii.data.crypto.safeprime

import global.msnthrp.xvii.core.crypto.safeprime.SafePrimeRepo
import global.msnthrp.xvii.core.crypto.safeprime.entity.SafePrime

class DefaultSafePrimeRepo(
        private val networkDataSource: ReadOnlySafePrimeDataSource,
        private val storageDataSource: ReadWriteSafePrimeDataSource
) : SafePrimeRepo {

    override fun getSafePrime(useCache: Boolean): SafePrime {
        var safePrime: SafePrime = SafePrime.EMPTY
        if (useCache) {
            safePrime = storageDataSource.getSafePrime()
        }
        return safePrime.takeIf { safePrime != SafePrime.EMPTY } ?: getFromNetwork()
    }

    private fun getFromNetwork(): SafePrime {
        val safePrime = networkDataSource.getSafePrime()
        if (!safePrime.isEmpty) {
            safePrime.let(storageDataSource::saveSafePrime)
        }
        return safePrime
    }

    interface ReadOnlySafePrimeDataSource {
        fun getSafePrime(): SafePrime
    }

    interface ReadWriteSafePrimeDataSource : ReadOnlySafePrimeDataSource {
        fun saveSafePrime(safePrime: SafePrime)
    }
}