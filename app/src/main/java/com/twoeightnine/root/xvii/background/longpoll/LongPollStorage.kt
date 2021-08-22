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

package com.twoeightnine.root.xvii.background.longpoll

import com.twoeightnine.root.xvii.background.longpoll.models.LongPollServer

// just a bridge between old implementation (was here) and new one (Encrypted..)
// TODO to be removed
object LongPollStorage {

    fun saveLongPoll(longPollServer: LongPollServer) {
        EncryptedLongPollStorage.longPollServer = global.msnthrp.xvii.core.longpoll.LongPollServer(
                key = longPollServer.key,
                server = longPollServer.server,
                ts = longPollServer.ts
        )
    }

    fun clear() {
        EncryptedLongPollStorage.clear()
    }

    fun getLongPollServer(): LongPollServer? {
        val internal = EncryptedLongPollStorage.longPollServer ?: return null

        return LongPollServer(
                key = internal.key,
                server = internal.server,
                ts = internal.ts
        )
    }
}