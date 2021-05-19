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

object DocColors {

    fun getColorByExtension(ext: String): Int? = when(ext) {
        "pdf" -> 0xa81e16
        "doc", "docx", "odf", "rtf", "txt" -> 0x6e79ed
        "xls", "xlsx", "csv" -> 0x3e9d5a
        "zip", "rar", "7z" -> 0x92d159
        "ppt", "pptx" -> 0xca4325
        "torrent" -> 0x2c8b3e
        "otf", "ttf" -> 0x313131
        "djvu" -> 0x6c33a4
        else -> null
    }?.let { it or 0xff000000.toInt() }
}