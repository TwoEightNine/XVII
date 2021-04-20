package com.twoeightnine.root.xvii.network

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
            val version = if (isNewVersion(request)) VERSION_NEW else VERSION_OLD
            builder.addQueryParameter(ACCESS_TOKEN, SessionProvider.token)
                    .addQueryParameter(VERSION, version)
        }
        val url = builder.build()
        request = request.newBuilder()
                .url(url)
                .removeHeader(ApiService.NO_TOKEN_HEADER_KEY)
                .build()
        return chain.proceed(request)
    }

    private fun isNewVersion(request: Request) = !request.header(ApiService.NEW_VERSION_HEADER_KEY).isNullOrEmpty()

    private fun needParameters(request: Request) = request.header(ApiService.NO_TOKEN_HEADER_KEY).isNullOrEmpty()

    companion object {
        private const val VERSION_OLD = "5.63"
        private const val VERSION_NEW = "5.92"

        private const val ACCESS_TOKEN = "access_token"
        private const val VERSION = "v"
    }
}