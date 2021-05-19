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

package com.twoeightnine.root.xvii.photoviewer

/**
 * Created by twoeightnine on 1/25/18.
 */
class ActiveTivManager(val count: Int = 3) {

    private val actualTivs by lazy {
        val res = arrayListOf<TouchImageView?>()
        repeat(count) { res.add(null) }
        res
    }

    fun saveTiv(pos: Int, tiv: TouchImageView) {
        actualTivs[pos % count] = tiv
    }

    fun getTiv(pos: Int) = actualTivs[pos % count]

}