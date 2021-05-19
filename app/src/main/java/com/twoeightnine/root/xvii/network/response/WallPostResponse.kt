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

package com.twoeightnine.root.xvii.network.response

import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WallPost

/**
 * Created by root on 1/27/17.
 */

class WallPostResponse {
    val items: MutableList<WallPost> = mutableListOf()
    val groups: MutableList<Group> = mutableListOf()
    val profiles: MutableList<User> = mutableListOf()
}
