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
import android.net.Uri
import com.twoeightnine.root.xvii.utils.deeplink.cases.ChatCaseParser
import com.twoeightnine.root.xvii.utils.deeplink.cases.ChatOwnerCaseParser
import com.twoeightnine.root.xvii.utils.deeplink.cases.ShareCaseParser

class DeepLinkParser {

    private val cases = listOf(
            ChatOwnerCaseParser,
            ChatCaseParser,
            ShareCaseParser
    )

    private var lastHandledIntent: Intent? = null

    fun parse(intent: Intent): Result {
        if (lastHandledIntent == intent) {
            return Result.Unknown
        }
        lastHandledIntent = intent

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
        data class Chat(val peerId: Int) : Result()
        data class Share(val shareText: String? = null, val shareMediaUris: List<Uri> = emptyList()) : Result()
        object Unknown : Result()
    }

    interface CaseParser {

        fun getResult(intent: Intent): Result
    }

}