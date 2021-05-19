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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import androidx.collection.LruCache
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.StickersEmojiRepository
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.managers.Prefs
import global.msnthrp.xvii.data.stickersemoji.model.Emoji

object EmojiHelper {

    private const val TAG = "emoji"

    private val emojis = arrayListOf<Emoji>()
    private val beginnings = mutableSetOf<Char>()
    private val cache: LruCache<String, Bitmap> = LruCache(10)

    /**
     * we need to call this in [App] achieve earlier initialization
     */
    fun init() {
        L.tag(TAG).log("init started")
        StickersEmojiRepository().loadRawEmojis { emojis ->
            this.emojis.addAll(emojis)
            emojis.forEach { beginnings.add(it.code[0]) }
            L.tag(TAG).log("successfully initialized")
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
            L.tag(TAG).throwable(ex).log("getEmojied error")
        }
        return builder
    }
}