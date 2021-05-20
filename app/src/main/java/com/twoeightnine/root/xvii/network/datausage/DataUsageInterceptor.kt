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

package com.twoeightnine.root.xvii.network.datausage

import com.twoeightnine.root.xvii.utils.time
import okhttp3.Interceptor
import okhttp3.Response

class DataUsageInterceptor(
        private val type: DataUsageEvent.Type = DataUsageEvent.Type.API
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestUrl = with(request.url) { host + encodedPath }
        var requestSize = request.body?.contentLength() ?: 0L
        requestSize += request.headers.byteCount()
        requestSize += request.url.toString().length
        requestSize += request.method.length
        requestSize += 8 //protocol

        val response = chain.proceed(request)
        val source = response.body?.source()
        source?.request(Long.MAX_VALUE)
        var responseSize = source?.buffer?.size ?: 0L
        responseSize += response.headers.byteCount()
        responseSize += response.code.toString().length

        events.add(DataUsageEvent(requestUrl, requestSize, responseSize, time(), type))
        return response
    }

    companion object {

        val events = arrayListOf<DataUsageEvent>()
    }
}