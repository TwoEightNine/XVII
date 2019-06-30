package com.twoeightnine.root.xvii.network.datausage

import com.twoeightnine.root.xvii.utils.time
import okhttp3.Interceptor
import okhttp3.Response

class DataUsageInterceptor(
        private val type: DataUsageEvent.Type = DataUsageEvent.Type.API
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestUrl = with(request.url()) { host() + encodedPath() }
        var requestSize = request.body()?.contentLength() ?: 0L
        requestSize += request.headers().byteCount()
        requestSize += request.url().toString().length
        requestSize += request.method().length
        requestSize += 8 //protocol

        val response = chain.proceed(request)
        val source = response.body()?.source()
        source?.request(Long.MAX_VALUE)
        var responseSize = source?.buffer()?.size() ?: 0L
        responseSize += response.headers().byteCount()
        responseSize += response.code().toString().length

        events.add(DataUsageEvent(requestUrl, requestSize, responseSize, time(), type))
        return response
    }

    companion object {

        val events = arrayListOf<DataUsageEvent>()
    }
}