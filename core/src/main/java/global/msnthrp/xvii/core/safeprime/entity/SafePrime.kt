package global.msnthrp.xvii.core.safeprime.entity

data class SafePrime(
        val p: String,
        val q: String,
        val g: String,
        val ts: Long = System.currentTimeMillis()
)