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

import android.annotation.SuppressLint
import global.msnthrp.xvii.data.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object OkHttp {

    private const val TIMEOUT = 30L


    private val defaultLoggingInterceptor by lazy {
        createLoggingInterceptor()
    }

    fun createVkAuthorizedOkHttpClient(): OkHttpClient =
            createDefaultBuilder()
                    .addInterceptor(TokenAndVersionInterceptor)
                    .build()

    fun createUnsafeOkHttpClient(): OkHttpClient =
            try {
                // Create a trust manager that does not validate certificate chains
                val trustManager = TrustAllX509TrustManager()

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL").apply {
                    init(null, arrayOf(trustManager), SecureRandom())
                }

                // Create an ssl socket factory with our all-trusting manager
                createDefaultBuilder()
                        .sslSocketFactory(sslContext.socketFactory, trustManager)
                        .hostnameVerifier { _, _ -> true }
                        .build()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

    private fun createDefaultBuilder(): OkHttpClient.Builder =
            OkHttpClient.Builder()
                    .addInterceptor(defaultLoggingInterceptor)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)

    private fun createLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = when {
            BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
            else -> HttpLoggingInterceptor.Level.NONE
        }
    }

    private class TrustAllX509TrustManager : X509TrustManager {

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            // allow all
        }

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            // allow all
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

}