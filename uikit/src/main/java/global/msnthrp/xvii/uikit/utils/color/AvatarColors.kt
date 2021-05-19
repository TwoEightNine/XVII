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

package global.msnthrp.xvii.uikit.utils.color

import kotlin.math.sign

object AvatarColors {

    private val colors = createColors().map { it or 0xff000000.toInt() }

    fun getColor(any: Any) = colors[any.hashCode() mod colors.size]

    private fun createColors() = listOf(
            0x39375B,
            0x745C97,
            0xDC6ACF,
            0x704E2E,
            0x79745C,
            0x709176,
            0x736CED,
            0x1C0B19,
            0x140D4F,
            0x4EA699,
            0x6D2E46,
            0xA26769,
            0x34312D,
            0x7E7F83,
            0x330036,
            0x2F394D,
            0xF7567C,
            0x5D576B,
            0xCF5C36,
            0xFF6663,
            0x007FFF,
            0x7D83FF,
            0x000F08,
            0x4D4847,
            0xDB3A34,
            0xF58A07,
            0x909CC2
    )

    private infix fun Int.mod(num: Int): Int {
        return (this % num).run { this * sign }
    }
}