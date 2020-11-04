package global.msnthrp.xvii.data.network

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import global.msnthrp.xvii.data.BuildConfig
import global.msnthrp.xvii.data.safeprime.storage.retrofit.SafePrimeApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Retrofit {

    private const val TIMEOUT = 30L

    val safePrimeApiService by lazy {
        createApiService(SafePrimeApiService::class.java)
    }

    private val defaultLoggingInterceptor = createLoggingInterceptor()
    private val defaultGson = createGson()
    private val defaultOkHttpClient = createOkHttpClient()

    private fun createOkHttpClient(): OkHttpClient =
            OkHttpClient.Builder()
                    .addInterceptor(defaultLoggingInterceptor)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .build()

    private fun createLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = when {
            BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
            else -> HttpLoggingInterceptor.Level.NONE
        }
    }

    private fun createGson(): Gson = GsonBuilder()
            .setExclusionStrategies(object : ExclusionStrategy {
                override fun shouldSkipClass(clazz: Class<*>?) = false
                override fun shouldSkipField(f: FieldAttributes) = false
            }).create()

    private fun createRetrofit() =
            Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(defaultGson))
                    .baseUrl("https://2ton.com.au/")
                    .client(defaultOkHttpClient)
                    .build()

    private fun <T> createApiService(apiServiceClass: Class<T>): T =
            createRetrofit().create(apiServiceClass)

}