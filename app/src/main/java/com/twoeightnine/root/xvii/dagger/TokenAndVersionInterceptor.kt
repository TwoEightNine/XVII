package com.twoeightnine.root.xvii.dagger

import com.twoeightnine.root.xvii.consts.Api
import com.twoeightnine.root.xvii.managers.Session
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
            builder.addQueryParameter(ACCESS_TOKEN, Session.token)
                    .addQueryParameter(VERSION, Api.VERSION)
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
        private val ACCESS_TOKEN = "access_token"
        private val VERSION = "v"
    }
}