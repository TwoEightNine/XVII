package global.msnthrp.xvii.core.safeprime

import global.msnthrp.xvii.core.safeprime.entity.SafePrime
import java.util.concurrent.TimeUnit

class DefaultSafePrimeUseCase(private val repo: SafePrimeRepo) : SafePrimeUseCase {

    override fun loadSafePrime(): SafePrime? {
        var safePrime = repo.getSafePrime(useCache = true)
        if (safePrime == null || safePrime.isObsolete()) {
            safePrime = repo.getSafePrime(useCache = false)
        }
        return safePrime
    }

    private fun SafePrime.isObsolete() = System.currentTimeMillis() - ts >= LIFE_TIME

    companion object {

        private val LIFE_TIME = TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS)
    }
}