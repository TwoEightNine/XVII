package global.msnthrp.xvii.data.session

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import global.msnthrp.xvii.core.crypto.CryptoUtils
import global.msnthrp.xvii.core.session.SessionProvider
import global.msnthrp.xvii.data.sharedpreferences.SharedPreferencesDelegates
import kotlin.reflect.KProperty


class EncryptedSessionProvider(context: Context) : SessionProvider {

    private var masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private var prefs = EncryptedSharedPreferences.create(
            NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override var token: String? by SharedPreferencesDelegates.StringDelegate(prefs, KEY_TOKEN)

    override var userId: Int by SharedPreferencesDelegates.IntDelegate(prefs, KEY_USER_ID)

    override var fullName: String? by SharedPreferencesDelegates.StringDelegate(prefs, KEY_FULL_NAME)

    override var photo: String? by SharedPreferencesDelegates.StringDelegate(prefs, KEY_PHOTO)

    override var pin: String? by SharedPreferencesDelegates.StringDelegate(prefs, KEY_PIN)

    override val encryptionKey256: ByteArray by EncryptionKeyDelegate()

    companion object {
        private const val NAME = "session"

        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_PHOTO = "photo"
        private const val KEY_PIN = "pin"
        private const val KEY_ENCRYPTION_KEY = "encryption_key"
    }

    private inner class EncryptionKeyDelegate {

        operator fun getValue(thisRef: Any?, prop: KProperty<*>): ByteArray {
            var encryptionKey = prefs.getString(KEY_ENCRYPTION_KEY, null)
                    ?.let(CryptoUtils::hexToBytes)
            if (encryptionKey == null) {
                encryptionKey = CryptoUtils.getRandomBytes(32)
                prefs.edit {
                    putString(KEY_ENCRYPTION_KEY, CryptoUtils.bytesToHex(encryptionKey))
                }
            }
            return encryptionKey
        }

    }
}