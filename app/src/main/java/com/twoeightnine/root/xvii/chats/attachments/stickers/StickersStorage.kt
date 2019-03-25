package com.twoeightnine.root.xvii.chats.attachments.stickers

import android.content.Context
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.utils.FileStorage

class StickersStorage(context: Context) : FileStorage<ArrayList<Attachment.Sticker>>(context, STICKERS_FILE) {

    override fun serialize(data: ArrayList<Attachment.Sticker>) = data.map { it.id }.joinToString(separator = ",")

    override fun deserialize(s: String) = try {
        ArrayList(s.split(",")
                .map { Attachment.Sticker(it.toInt()) })
    } catch (e: Exception) {
        e.printStackTrace()
        arrayListOf<Attachment.Sticker>()
    }

    companion object {
        const val STICKERS_FILE = "stickers"
    }
}