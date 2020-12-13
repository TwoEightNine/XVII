package global.msnthrp.xvii.core.crypto.safeprime.entity

data class SafePrime(
        val p: String,
        val q: String,
        val g: String,
        val ts: Long = System.currentTimeMillis()
) {

    val isEmpty: Boolean
        get() = this == EMPTY

    companion object {

        val EMPTY = SafePrime(p = "", q = "", g = "")
    }
}