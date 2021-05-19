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

package global.msnthrp.xvii.data.network

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import global.msnthrp.xvii.data.crypto.safeprime.storage.retrofit.SafePrimeApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Retrofit {

    val safePrimeApiService by lazy {
        createApiService(Host.SAFE_PRIME, SafePrimeApiService::class.java)
    }


    private val defaultGson by lazy {
        createGson()
    }
    private val defaultOkHttpClient by lazy {
        OkHttp.createOkHttpClient()
    }
    private val unsafeOkHttpClient by lazy {
        OkHttp.createUnsafeOkHttpClient()
    }

    private fun createGson(): Gson = GsonBuilder()
            .setExclusionStrategies(object : ExclusionStrategy {
                override fun shouldSkipClass(clazz: Class<*>?) = false
                override fun shouldSkipField(f: FieldAttributes) = false
            }).create()

    private fun createRetrofit(host: Host) =
            Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(defaultGson))
                    .baseUrl(host.baseUrl)
                    .client(host.clientType.getMatchingClient())
                    .build()

    private fun <T> createApiService(host: Host, apiServiceClass: Class<T>): T =
            createRetrofit(host).create(apiServiceClass)

    private fun ClientType.getMatchingClient() = when (this) {
        ClientType.UNSAFE -> unsafeOkHttpClient
        ClientType.DEFAULT -> defaultOkHttpClient
    }

}