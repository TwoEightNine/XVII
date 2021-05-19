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

package com.twoeightnine.root.xvii.utils

import android.content.Context
import java.io.*

abstract class FileStorage<T>(context: Context, name: String) {

    private val file = File(context.cacheDir, name)

    init {
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    protected abstract fun serialize(data: T): String

    protected abstract fun deserialize(s: String): T

    fun writeToFile(data: T) {
        val writer = BufferedWriter(FileWriter(file))
        writer.write(serialize(data))
        writer.close()
    }

    fun readFromFile(): T {
        val br = BufferedReader(FileReader(file))
        val sb = StringBuilder()
        var str: String?
        do {
            str = br.readLine()
            if (str != null) sb.append(str)
        } while (str != null)
        return deserialize(sb.toString())
    }

}