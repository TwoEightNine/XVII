package com.twoeightnine.root.xvii.dagger.modules

import android.content.Context
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.dagger.TokenAndVersionInterceptor
import com.twoeightnine.root.xvii.utils.ApiUtils
import com.twoeightnine.root.xvii.utils.isOnline
import dagger.Module
import dagger.Provides
import io.realm.RealmObject
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
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
            .baseUrl(App.API_URL)
            .client(client)
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideApiUtils(api: ApiService): ApiUtils = ApiUtils(api)
}