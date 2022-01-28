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

package global.msnthrp.xvii.data.crypto.safeprime.storage.retrofit

import global.msnthrp.xvii.core.crypto.safeprime.entity.SafePrime
import global.msnthrp.xvii.data.crypto.safeprime.DefaultSafePrimeRepo
import global.msnthrp.xvii.data.network.Retrofit

class RetrofitSafePrimeDataSource : DefaultSafePrimeRepo.ReadOnlySafePrimeDataSource {

    private val apiService by lazy { Retrofit.safePrimeApiService }

    override fun getSafePrime(): SafePrime {
        val safePrimeResponse = apiService.getSafePrime().execute().body()
        return safePrimeResponse?.toSafePrime() ?: SafePrime.EMPTY
    }

    private fun SafePrimeResponse.toSafePrime() =
            SafePrime(
                    p = p.base10,
                    q = q.base10,
                    g = g.base10
            )
}