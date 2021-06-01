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

package global.msnthrp.xvii.uikit.utils

import global.msnthrp.xvii.uikit.extensions.lower

object ExtensionUtils {

    private val imageExtensions = listOf("jpg", "jpeg", "png")
    private val videoExtensions = listOf("mp4", "3gp", "mov")

    fun isImage(path: String) = matches(path, imageExtensions)

    fun isVideo(path: String) = matches(path, videoExtensions)

    private fun matches(path: String, extensions: List<String>): Boolean {
        val lowerPath = path.lower()
        for (ext in extensions) {
            if (lowerPath.endsWith(".$ext")) {
                return true
            }
        }
        return false
    }

}