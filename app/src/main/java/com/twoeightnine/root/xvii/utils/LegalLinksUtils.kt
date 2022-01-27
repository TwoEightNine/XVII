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

package com.twoeightnine.root.xvii.utils

import java.util.*

object LegalLinksUtils {

    private const val PRIVACY_WORLD = "https://github.com/TwoEightNine/XVII/blob/master/privacy.md"
    private const val PRIVACY_RU = "https://github.com/TwoEightNine/XVII/blob/master/privacy_ru.md"

    private const val VK_TOS = "https://m.vk.com/terms"

    fun getTermsOfServiceUrl() = VK_TOS

    fun getPrivacyPolicyUrl(): String {
        return when (Locale.getDefault()) {
            Locale("ru") -> PRIVACY_RU
            else -> PRIVACY_WORLD
        }
    }

}