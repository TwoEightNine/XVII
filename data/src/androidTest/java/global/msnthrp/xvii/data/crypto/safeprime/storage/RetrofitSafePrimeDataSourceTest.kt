package global.msnthrp.xvii.data.crypto.safeprime.storage

import androidx.test.runner.AndroidJUnit4
import global.msnthrp.xvii.data.crypto.safeprime.storage.retrofit.RetrofitSafePrimeDataSource
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigInteger

@RunWith(AndroidJUnit4::class)
class RetrofitSafePrimeDataSourceTest {

    @Test
    fun loadSafePrime_returnNonEmpty() {
        val dataSource = RetrofitSafePrimeDataSource()
        val safePrime = dataSource.getSafePrime()

        Assert.assertFalse(safePrime.isEmpty)

        val p = BigInteger(safePrime.p)
        val q = (p - BigInteger.ONE) / BigInteger("2")

        Assert.assertEquals(BigInteger(safePrime.q), q)
    }

}