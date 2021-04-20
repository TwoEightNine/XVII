package global.msnthrp.xvii.core.crypto.safeprime

import global.msnthrp.xvii.core.crypto.safeprime.entity.SafePrime
import java.math.BigInteger
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

class DefaultSafePrimeUseCase(private val repo: SafePrimeRepo) : SafePrimeUseCase {

    override fun loadSafePrime(): SafePrime {
        var safePrime = repo.getSafePrime(useCache = true)
        if (safePrime.isEmpty || safePrime.isObsolete()) {
            safePrime = repo.getSafePrime(useCache = false)
        }
        if (safePrime.isEmpty) {
            safePrime = createSafePrime()
        }
        return safePrime
    }

    private fun SafePrime.isObsolete() = System.currentTimeMillis() - ts >= LIFE_TIME

    private fun createSafePrime(): SafePrime {
        val p = BigInteger(PRIME_DEFAULT)
        val q = (p - BigInteger.ONE) / BigInteger("2")

        var x: BigInteger
        do {
            x = BigInteger(32, SecureRandom())
        } while (x == BigInteger.ZERO || !isPrimeRoot(x, q, p))
        val g = x

        return SafePrime(
                p = p.toString(10),
                q = q.toString(10),
                g = g.toString(10),
                ts = System.currentTimeMillis()
        )
    }

    private fun isPrimeRoot(g: BigInteger, q: BigInteger, p: BigInteger): Boolean =
            g.modPow(BigInteger.ONE, p) != BigInteger.ONE
                    && g.modPow(BigInteger.valueOf(2), p) != BigInteger.ONE
                    && g.modPow(q, p) != BigInteger.ONE


    companion object {

        private val LIFE_TIME = TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS)

        private const val PRIME_DEFAULT = "429960845873088536599738146849398890197656281978746052260302" +
                "647466290912363305996665498753182126120318295110244403964643426486915779918338905631403" +
                "871028184935255084712703423613529366297760543627384218281702559557613931230981025214701" +
                "436292843374882547050410898749274017561380961864641986822934974094546625703373934105581" +
                "804151000069136171694933799628596746360744089987859632744266134003621159571422862980144" +
                "043377717793249604329463406248840895370685965329721512935512704815510998868368597285047" +
                "022910731679048010756286396108128504010975404344672924191827643103234869173733652744217" +
                "51734483875743936086632531541480503"
    }
}