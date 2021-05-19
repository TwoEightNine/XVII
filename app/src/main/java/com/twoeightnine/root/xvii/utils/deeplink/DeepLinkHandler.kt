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

package com.twoeightnine.root.xvii.utils.deeplink

import android.content.Intent
import com.twoeightnine.root.xvii.utils.deeplink.cases.ChatOwnerCase

class DeepLinkHandler {

    private val cases = listOf(
        ChatOwnerCase
    )

    fun handle(intent: Intent): Result {
        for (case in cases) {
            val result = case.getResult(intent)
            if (result != Result.Unknown) {
                return result
            }
        }
        return Result.Unknown
    }

    sealed class Result {

        data class ChatOwner(val peerId: Int) : Result()

        object Unknown : Result()
    }

    interface Case<T : Any> {
        fun parseIntent(intent: Intent): T?

        fun getResult(intent: Intent): Result
    }

}