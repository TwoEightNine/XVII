package global.msnthrp.xvii.data.safeprime.storage

import android.content.Context
import androidx.core.content.edit
import global.msnthrp.xvii.core.safeprime.entity.SafePrime
import global.msnthrp.xvii.data.safeprime.DefaultSafePrimeRepo

class PreferencesSafePrimeDataSource(context: Context)
    : DefaultSafePrimeRepo.ReadWriteSafePrimeDataSource {

    private val preferences by lazy {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    override fun getSafePrime() = when {
        !preferences.contains(KEY_P) -> SafePrime.EMPTY
        else -> SafePrime(
                p = preferences.getString(KEY_P, "") ?: "",
                q = preferences.getString(KEY_Q, "") ?: "",
                g = preferences.getString(KEY_G, "") ?: "",
                ts = preferences.getLong(KEY_TS, 0L)
        )
    }

    override fun saveSafePrime(safePrime: SafePrime) {
        if (safePrime.isEmpty) return

        preferences.edit {
            putString(KEY_P, safePrime.p)
            putString(KEY_Q, safePrime.q)
            putString(KEY_G, safePrime.g)
            putLong(KEY_TS, safePrime.ts)
        }
    }

    companion object {
        private const val NAME = "safePrime"
        private const val KEY_P = "p"
        private const val KEY_Q = "q"
        private const val KEY_G = "g"
        private const val KEY_TS = "ts"
    }
}