package global.msnthrp.xvii.data.crypto.safeprime.storage

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import global.msnthrp.xvii.core.crypto.safeprime.entity.SafePrime
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigInteger

@RunWith(AndroidJUnit4::class)
class PreferencesSafePrimeDataSourceTest {

    @Test
    fun emptyPreferences_returnEmpty() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dataSource = PreferencesSafePrimeDataSource(context)
        val safePrime = dataSource.getSafePrime()

        Assert.assertTrue(safePrime.isEmpty)
    }

    @Test
    fun filledPreferences_returnNonEmpty() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dataSource = PreferencesSafePrimeDataSource(context)

        dataSource.saveSafePrime(DEFAULT_SAFE_PRIME)
        val loadedSafePrime = dataSource.getSafePrime()

        Assert.assertEquals(DEFAULT_SAFE_PRIME.p, loadedSafePrime.p)
        Assert.assertEquals(DEFAULT_SAFE_PRIME.q, loadedSafePrime.q)
        Assert.assertEquals(DEFAULT_SAFE_PRIME.g, loadedSafePrime.g)
        Assert.assertEquals(DEFAULT_SAFE_PRIME.ts, loadedSafePrime.ts)
    }

    @Test
    fun overrideWithEmpty_returnNonEmpty() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dataSource = PreferencesSafePrimeDataSource(context)

        dataSource.saveSafePrime(DEFAULT_SAFE_PRIME)
        dataSource.saveSafePrime(SafePrime.EMPTY)
        val loadedSafePrime = dataSource.getSafePrime()

        Assert.assertEquals(DEFAULT_SAFE_PRIME.p, loadedSafePrime.p)
        Assert.assertEquals(DEFAULT_SAFE_PRIME.q, loadedSafePrime.q)
        Assert.assertEquals(DEFAULT_SAFE_PRIME.g, loadedSafePrime.g)
        Assert.assertEquals(DEFAULT_SAFE_PRIME.ts, loadedSafePrime.ts)
    }

    companion object {

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

}