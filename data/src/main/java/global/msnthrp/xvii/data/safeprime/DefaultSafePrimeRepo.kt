package global.msnthrp.xvii.data.safeprime

import global.msnthrp.xvii.core.safeprime.SafePrimeRepo
import global.msnthrp.xvii.core.safeprime.entity.SafePrime

class DefaultSafePrimeRepo(
        private val networkDataSource: ReadOnlySafePrimeDataSource,
        private val storageDataSource: ReadWriteSafePrimeDataSource
) : SafePrimeRepo {

    override fun getSafePrime(useCache: Boolean): SafePrime? {
        var safePrime: SafePrime? = null
        if (useCache) {
            safePrime = storageDataSource.getSafePrime()
        }
        return safePrime ?: getFromNetwork()
    }

    private fun getFromNetwork(): SafePrime? {
        val safePrime = networkDataSource.getSafePrime()
        safePrime?.let(storageDataSource::saveSafePrime)
        return safePrime
    }

    interface ReadOnlySafePrimeDataSource {
        fun getSafePrime(): SafePrime?
    }

    interface ReadWriteSafePrimeDataSource : ReadOnlySafePrimeDataSource {
        fun saveSafePrime(safePrime: SafePrime)
    }
}