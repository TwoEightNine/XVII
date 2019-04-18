package com.twoeightnine.root.xvii.chats.attachments.stickers

import android.content.Context
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.utils.FileStorage

class StickersStorage(
        context: Context,
        type: Type
) : FileStorage<ArrayList<Sticker>>(context, type.name) {

    override fun serialize(data: ArrayList<Sticker>) = data.map { it.id }.joinToString(separator = ",")

    override fun deserialize(s: String) = try {
        ArrayList(s.split(",")
                .map { Sticker(it.toInt()) })
    } catch (e: Exception) {
        e.printStackTrace()
        arrayListOf<Sticker>()
    }

    enum class Type {
        AVAILABLE,
        RECENT
    }
}