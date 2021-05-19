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

package com.twoeightnine.root.xvii.network.datausage

data class DataUsageEvent(

        /**
         * name of request
         */
        val name: String,

        /**
         * size of request content in bytes
         */
        val requestSize: Long,

        /**
         * size of response content in bytes
         */
        val responseSize: Long,

        /**
         * time when the request was finished
         */
        val timeStamp: Int,

        val type: Type
) {
    /**
     * type of request
     */
    enum class Type {

        /**
         * usual call to vk api
         */
        API,

        /**
         * loading photo
         */
        PHOTO
    }
}