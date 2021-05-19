/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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