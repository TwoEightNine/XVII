package global.msnthrp.xvii.data.crypto.safeprime.storage.retrofit

import com.google.gson.annotations.SerializedName

data class SafePrimeResponse(

        @SerializedName("p")
        val p: NumberResponse,

        @SerializedName("q")
        val q: NumberResponse,

        @SerializedName("g")
        val g: NumberResponse
)

data class NumberResponse(

        @SerializedName("base10")
        val base10: String
)