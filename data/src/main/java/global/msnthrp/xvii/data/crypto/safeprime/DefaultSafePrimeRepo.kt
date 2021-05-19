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