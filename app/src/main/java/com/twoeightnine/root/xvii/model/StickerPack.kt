package com.twoeightnine.root.xvii.model

class StickerPack(private val startId: Int,
                  val count: Int) {

    val isRecent = startId == 0

    val isAvailable = startId == -1

    fun getSticker(i: Int) = startId + i

    fun getStickerUrl(i: Int) = "https://vk.com/images/stickers/${getSticker(i)}/256b.png"

    companion object {
        var RECENT = StickerPack(0, 0)
            private set
        var AVAILABLE: StickerPack = StickerPack(-1, 0)
            private set
    }
}