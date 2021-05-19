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

package global.msnthrp.xvii.core.crypto.safeprime

import global.msnthrp.xvii.core.crypto.safeprime.entity.SafePrime
import org.junit.Assert
import org.junit.Test
import java.math.BigInteger


class DefaultSafePrimeUseCaseTest {

    @Test
    fun safePrime_validCache() {
        val useCase = DefaultSafePrimeUseCase(RepoWithValidCache())
        val safePrime = useCase.loadSafePrime()
        assertSafePrimeDefault(safePrime)
    }

    @Test
    fun safePrime_emptyCache() {
        val useCase = DefaultSafePrimeUseCase(RepoWithoutCache())
        val safePrime = useCase.loadSafePrime()
        assertSafePrimeDefault(safePrime)
    }

    @Test
    fun safePrime_obsoleteCache() {
        val useCase = DefaultSafePrimeUseCase(RepoWithObsoleteCache())
        val safePrime = useCase.loadSafePrime()
        assertSafePrimeDefault(safePrime)
    }

    @Test
    fun safePrime_emptyRepo() {
        val useCase = DefaultSafePrimeUseCase(RepoWithObsoleteCache())
        val safePrime = useCase.loadSafePrime()

        Assert.assertNotEquals(safePrime, SafePrime.EMPTY)
    }

    private fun assertSafePrimeDefault(safePrime: SafePrime) {
        Assert.assertEquals(safePrime.p, DEFAULT_SAFE_PRIME.p)
        Assert.assertEquals(safePrime.q, DEFAULT_SAFE_PRIME.q)
        Assert.assertEquals(safePrime.g, DEFAULT_SAFE_PRIME.g)
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

    private class RepoWithValidCache : SafePrimeRepo {
        override fun getSafePrime(useCache: Boolean): SafePrime =
                DEFAULT_SAFE_PRIME.copy(ts = System.currentTimeMillis())

    }

    private class RepoWithObsoleteCache : SafePrimeRepo {
        override fun getSafePrime(useCache: Boolean): SafePrime =
                DEFAULT_SAFE_PRIME.copy(ts = if (useCache) 1753 else System.currentTimeMillis())

    }

    private class RepoWithoutCache : SafePrimeRepo {
        override fun getSafePrime(useCache: Boolean): SafePrime =
                if (useCache) {
                    SafePrime.EMPTY
                } else {
                    DEFAULT_SAFE_PRIME.copy(ts = System.currentTimeMillis())
                }
    }

}