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