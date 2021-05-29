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

package com.twoeightnine.root.xvii.utils.deeplink.cases

import android.content.Intent
import com.twoeightnine.root.xvii.utils.asChatPeerId
import com.twoeightnine.root.xvii.utils.deeplink.DeepLinkHandler

object ChatCase : DeepLinkHandler.Case<Int> {

    override fun parseIntent(intent: Intent): Int? {
        return when (intent.action) {
            Intent.ACTION_VIEW -> {
                val peerIdRaw = intent.data?.getQueryParameter("sel")
                val peerId = when {
                    peerIdRaw == null -> null
                    peerIdRaw.startsWith('c') -> peerIdRaw.drop(1).toIntOrNull()?.asChatPeerId()
                    else -> peerIdRaw.toIntOrNull()
                }

                peerId
            }
            else -> null
        }
    }

    override fun getResult(intent: Intent): DeepLinkHandler.Result =
            when (val peerId = parseIntent(intent)) {
                null -> DeepLinkHandler.Result.Unknown
                else -> DeepLinkHandler.Result.Chat(peerId)
            }
}