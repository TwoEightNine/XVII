package global.msnthrp.xvii.data.crypto.safeprime

import androidx.test.runner.AndroidJUnit4
import global.msnthrp.xvii.core.crypto.safeprime.entity.SafePrime
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigInteger

@RunWith(AndroidJUnit4::class)
class DefaultSafePrimeRepoTest {

    @Test
    fun firstTimeLoad_loadFromNetAndSave() {
        val readWriteDataSource = EmptyReadWriteSafePrimeDataSource()
        val repo = DefaultSafePrimeRepo(UsualReadOnlySafePrimeDataSource(), readWriteDataSource)
        val safePrime = repo.getSafePrime()

        Assert.assertFalse(safePrime.isEmpty)
        Assert.assertEquals(safePrime.ts, TS_READ_ONLY)
        Assert.assertEquals(safePrime, readWriteDataSource.getSafePrime())
    }

    @Test
    fun emptyNet_returnEmpty() {
        val repo = DefaultSafePrimeRepo(EmptyReadOnlySafePrimeDataSource(), NonEmptyReadWriteSafePrimeDataSource())
        val safePrime = repo.getSafePrime()

        Assert.assertTrue(safePrime.isEmpty)
    }

    @Test
    fun forceSaved_returnSaved() {
        val repo = DefaultSafePrimeRepo(UsualReadOnlySafePrimeDataSource(), NonEmptyReadWriteSafePrimeDataSource())
        val safePrime = repo.getSafePrime(useCache = true)

        Assert.assertFalse(safePrime.isEmpty)
        Assert.assertEquals(safePrime.ts, TS_READ_WRITE)
    }

    @Test
    fun firstStartEmptyNet_returnEmpty() {
        val repo = DefaultSafePrimeRepo(EmptyReadOnlySafePrimeDataSource(), EmptyReadWriteSafePrimeDataSource())
        val safePrime = repo.getSafePrime()

        Assert.assertTrue(safePrime.isEmpty)
    }

    companion object {

        // to mark SafePrime
        private const val TS_READ_ONLY = 289L
        private const val TS_READ_WRITE = 1753L

        private const val PRIME = "429960845873088536599738146849398890197656281978746052260302" +
                "647466290912363305996665498753182126120318295110244403964643426486915779918338905631403" +
                "871028184935255084712703423613529366297760543627384218281702559557613931230981025214701" +
                "436292843374882547050410898749274017561380961864641986822934974094546625703373934105581" +
                "804151000069136171694933799628596746360744089987859632744266134003621159571422862980144" +
                "043377717793249604329463406248840895370685965329721512935512704815510998868368597285047" +
                "022910731679048010756286396108128504010975404344672924191827643103234869173733652744217" +
                "51734483875743936086632531541480503"

        private val DEFAULT_SAFE_PRIME = SafePrime(
                p = PRIME,
                q = ((BigInteger(PRIME) - BigInteger.ONE) / BigInteger("2")).toString(10),
                g = "3"
        )
    }

    private class EmptyReadOnlySafePrimeDataSource : DefaultSafePrimeRepo.ReadOnlySafePrimeDataSource {
        override fun getSafePrime(): SafePrime = SafePrime.EMPTY
    }

    private class UsualReadOnlySafePrimeDataSource : DefaultSafePrimeRepo.ReadOnlySafePrimeDataSource {
        override fun getSafePrime(): SafePrime = DEFAULT_SAFE_PRIME.copy(ts = TS_READ_ONLY)
    }

    private class EmptyReadWriteSafePrimeDataSource : DefaultSafePrimeRepo.ReadWriteSafePrimeDataSource {

        private var safePrime: SafePrime = SafePrime.EMPTY

        override fun getSafePrime(): SafePrime = safePrime

        override fun saveSafePrime(safePrime: SafePrime) {
            this.safePrime = safePrime
        }
    }

    private class NonEmptyReadWriteSafePrimeDataSource : DefaultSafePrimeRepo.ReadWriteSafePrimeDataSource {

        private var safePrime: SafePrime = DEFAULT_SAFE_PRIME.copy(ts = TS_READ_WRITE)

        override fun getSafePrime(): SafePrime = safePrime

        override fun saveSafePrime(safePrime: SafePrime) {
            this.safePrime = safePrime
        }
    }

}