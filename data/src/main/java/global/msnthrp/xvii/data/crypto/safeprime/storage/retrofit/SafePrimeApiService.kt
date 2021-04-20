package global.msnthrp.xvii.data.crypto.safeprime.storage.retrofit

import retrofit2.Call
import retrofit2.http.GET


interface SafePrimeApiService {

    @GET("/getprimes/random/2048")
    fun getSafePrime(): Call<SafePrimeResponse>

}