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

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by twoeightnine on 1/12/18.
 */
object TokenAndVersionInterceptor: Interceptor {

    private const val API_VERSION = "5.131"

    private const val ACCESS_TOKEN = "access_token"
    private const val VERSION = "v"

    var tokenProvider: TokenProvider? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val builder = request.url.newBuilder()
        builder.addQueryParameter(ACCESS_TOKEN, tokenProvider?.token.orEmpty())
                .addQueryParameter(VERSION, API_VERSION)
        val url = builder.build()
        request = request.newBuilder()
                .url(url)
                .build()
        return chain.proceed(request)
    }

    interface TokenProvider {
        val token: String
    }
}