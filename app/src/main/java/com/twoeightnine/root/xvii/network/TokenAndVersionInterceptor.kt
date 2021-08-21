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

package com.twoeightnine.root.xvii.network

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.storage.SessionProvider
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Created by twoeightnine on 1/12/18.
 */
class TokenAndVersionInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val builder = request.url().newBuilder()
        if (needParameters(request)) {
            builder.addQueryParameter(ACCESS_TOKEN, SessionProvider.token)
                    .addQueryParameter(VERSION, App.VERSION)
        }
        val url = builder.build()
        request = request.newBuilder()
                .url(url)
                .removeHeader(ApiService.NO_TOKEN_HEADER_KEY)
                .build()
        return chain.proceed(request)
    }

    private fun needParameters(request: Request) = request.header(ApiService.NO_TOKEN_HEADER_KEY).isNullOrEmpty()

    companion object {

        private const val ACCESS_TOKEN = "access_token"
        private const val VERSION = "v"
    }
}