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

package com.twoeightnine.root.xvii.crypto

import java.security.MessageDigest

fun md5(plain: String) = md5Raw(plain.toByteArray())
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .joinToString(separator = "") { if (it.length == 2) it else "0$it" }

fun sha256(plain: String) = sha256Raw(plain.toByteArray())
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .joinToString(separator = "") { if (it.length == 2) it else "0$it" }

private fun md5Raw(plain: ByteArray): ByteArray = MessageDigest
        .getInstance("MD5")
        .digest(plain)

private fun sha256Raw(plain: ByteArray): ByteArray = MessageDigest
        .getInstance("SHA-256")
        .digest(plain)
