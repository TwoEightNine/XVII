package global.msnthrp.xvii.data.crypto.engine

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import global.msnthrp.xvii.core.crypto.engine.CryptoEngineEncoder
import global.msnthrp.xvii.core.crypto.engine.CryptoEngineRepo

class PreferencesCryptoEngineRepo(
        context: Context,
        private val cryptoEngineEncoder: CryptoEngineEncoder
) : CryptoEngineRepo {

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    override fun getKeyOrNull(peerId: Int): ByteArray? =
            try {
                cryptoEngineEncoder.decode(preferences.getString(getPeerKey(peerId), null) ?: "")
            } catch (e: Exception) {
                null
            }

    override fun setKey(peerId: Int, key: ByteArray) {
        preferences.edit {
            putString(getPeerKey(peerId), cryptoEngineEncoder.encode(key))
        }
    }

    override fun clearAll() {
        preferences.edit {
            clear()
        }
    }

    private fun getPeerKey(peerId: Int) = "peer$peerId"

    companion object {
        private const val NAME = "cryptoStorage"
    }
}