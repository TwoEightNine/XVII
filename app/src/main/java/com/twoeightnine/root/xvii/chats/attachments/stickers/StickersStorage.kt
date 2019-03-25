package com.twoeightnine.root.xvii.chats.attachments.stickers

import android.content.Context
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.utils.FileStorage

class StickersStorage(
        context: Context,
        type: Type
) : FileStorage<ArrayList<Attachment.Sticker>>(context, type.name) {

    override fun serialize(data: ArrayList<Attachment.Sticker>) = data.map { it.id }.joinToString(separator = ",")

    override fun deserialize(s: String) = try {
        ArrayList(s.split(",")
                .map { Attachment.Sticker(it.toInt()) })
    } catch (e: Exception) {
        e.printStackTrace()
        arrayListOf<Attachment.Sticker>()
    }

    enum class Type {
        AVAILABLE,
        RECENT
    }
}