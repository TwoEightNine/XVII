package com.twoeightnine.root.xvii.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import androidx.collection.LruCache
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.StickersEmojiRepository
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.Emoji
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs

object EmojiHelper {

    private val emojis = arrayListOf<Emoji>()
    private val beginnings = mutableSetOf<Char>()
    private val cache: LruCache<String, Bitmap> = LruCache(10)

    /**
     * we need to call this in [App] achieve earlier initialization
     */
    fun init() {
        Lg.i("[emoji] init started")
        StickersEmojiRepository().loadRawEmojis { emojis ->
            this.emojis.addAll(emojis)
            emojis.forEach { beginnings.add(it.code[0]) }
            Lg.i("[emoji] successfully initialized")
        }
    }

    fun hasEmojis(text: String): Boolean {
        for (chr in text) {
            if (chr in beginnings) {
                return true
            }
        }
        return false
    }

    fun getEmojisCount(text: String): Int {
        var count = 0
        for (chr in text) {
            if (chr in beginnings) {
                count++
            }
        }
        return count
    }

    fun getEmojied(
            context: Context,
            text: String,
            builder: SpannableStringBuilder = SpannableStringBuilder(text),
            ignorePref: Boolean = false
    ): SpannableStringBuilder {
        if (!Prefs.appleEmojis && !ignorePref) return builder

        val size = context.resources.getDimensionPixelSize(R.dimen.emoji_size)
        try {
            for ((key, res, _) in emojis) {
                var index = 0
                while (true) {
                    index = text.indexOf(key, index)
                    if (index == -1) {
                        break
                    }
                    var bmp: Bitmap? = cache.get(res)
                    if (bmp == null) {
                        val inputStream = context.assets.open("emoji/$res")
                        bmp = BitmapFactory.decodeStream(inputStream)
                        inputStream.close()
                        cache.put(res, bmp)
                    }

                    val span = ImageSpan(BitmapDrawable(context.resources, bmp).apply {
                        setBounds(0, 0, size, size)
                    })

                    builder.setSpan(span, index, index + key.length, 33)
                    index += key.length
                }
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            Lg.wtf("error in emoji: $ex")
        }
        return builder
    }
}