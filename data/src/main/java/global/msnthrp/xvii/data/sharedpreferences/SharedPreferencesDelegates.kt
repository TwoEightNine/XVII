package global.msnthrp.xvii.data.sharedpreferences

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.reflect.KProperty

object SharedPreferencesDelegates {

    class StringDelegate(prefs: SharedPreferences, key: String) : BaseDelegate<String?>(prefs, key) {

        override fun SharedPreferences.getter(key: String): String? = getString(key, null)

        override fun SharedPreferences.Editor.setter(key: String, value: String?) {
            putString(key, value)
        }
    }

    class IntDelegate(prefs: SharedPreferences, key: String) : BaseDelegate<Int>(prefs, key) {

        override fun SharedPreferences.getter(key: String): Int = getInt(key, 0)

        override fun SharedPreferences.Editor.setter(key: String, value: Int) {
            putInt(key, value)
        }
    }

    abstract class BaseDelegate<T>(
            private val prefs: SharedPreferences,
            private val key: String
    ) {

        private var cachedValue: T? = null

        abstract fun SharedPreferences.getter(key: String): T

        abstract fun SharedPreferences.Editor.setter(key: String, value: T)

        operator fun getValue(thisRef: Any?, prop: KProperty<*>): T =
                cachedValue ?: prefs.getter(key).also { cachedValue = it }

        operator fun setValue(thisRef: Any?, prop: KProperty<*>, value: T) {
            prefs.edit { setter(key, value) }
            cachedValue = value
        }
    }

}