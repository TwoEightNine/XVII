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