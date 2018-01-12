package com.twoeightnine.root.xvii.dagger.modules

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.consts.Api
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.dagger.MusicService
import com.twoeightnine.root.xvii.dagger.TokenAndVersionInterceptor
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.utils.isOnline
import dagger.Module
import dagger.Provides
import io.realm.RealmObject
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {

    val cacheSize = 1024 * 1024 * 30L
    val timeout = 300L
    val offline = {
        chain: Interceptor.Chain ->
        var request = chain.request()
        if (!isOnline()) {
            request = request.newBuilder()
                    .header("Cache_Control", "public, only-if-cached, max-stale=86400")
                    .build()
        }
        chain.proceed(request)
    }

    @Provides
    @Singleton
    fun provideTokenAndVersionInterceptor(): TokenAndVersionInterceptor = TokenAndVersionInterceptor()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val log = HttpLoggingInterceptor()
        log.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        return log
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(context: Context,
                            loggingInterceptor: HttpLoggingInterceptor,
                            tokenAndVersionInterceptor: TokenAndVersionInterceptor): OkHttpClient {
        val file = File(context.cacheDir, "cache")
        val cache = Cache(file, cacheSize)
        return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(tokenAndVersionInterceptor)
                .addInterceptor(offline)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .cache(cache)
                .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setExclusionStrategies(object : ExclusionStrategy {
        override fun shouldSkipClass(clazz: Class<*>?) = false
        override fun shouldSkipField(f: FieldAttributes) = f.declaredClass == RealmObject::class.java
    }).create()

    @Provides
    @Singleton
    fun provideNetwork(client: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(Api.API_URL)
            .client(client)
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideMusicService(loggingInterceptor: HttpLoggingInterceptor, gson: Gson): MusicService = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(Api.KA4KA)
            .client(OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(offline)
                    .addInterceptor(AddCookiesInterceptor())
                    .addInterceptor(ReceivedCookiesInterceptor())
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .build())
            .build()
            .create(MusicService::class.java)


    inner class AddCookiesInterceptor : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val builder = chain.request().newBuilder()
            val cookies = Session.musicCookie
            for (cookie in cookies) {
                builder.addHeader("Cookie", cookie.toString())
                Log.v("OkHttp", "Adding Header: " + cookie) // This is done so I know which headers are being added; this interceptor is used after the normal logging of OkHttp
            }

            return chain.proceed(builder.build())
        }
    }

    inner class ReceivedCookiesInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalResponse = chain.proceed(chain.request())

            if (!originalResponse.headers("Set-Cookie").isEmpty()) {
                val cookies = HashSet<String>()

                for (header in originalResponse.headers("Set-Cookie")) {
                    cookies.add(header)
                }
                Session.musicCookie = cookies
            }

            return originalResponse
        }
    }
}