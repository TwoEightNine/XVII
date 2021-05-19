/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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