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

package com.twoeightnine.root.xvii.background.longpoll.models.events

/**
 * used to handle deleted messages
 */
class InstallFlagsEvent(
        val id: Int,
        val flags: Int,
        val peerId: Int
) : BaseLongPollEvent() {

    override fun getType() = TYPE_INSTALL_FLAGS

    val isDeleted: Boolean
        get() = (flags and FLAG_DELETED) == FLAG_DELETED


    fun isOut() = (flags and FLAG_OUT) > 0

    companion object {
        const val FLAG_DELETED = 128
        const val FLAG_OUT = 2
    }
}