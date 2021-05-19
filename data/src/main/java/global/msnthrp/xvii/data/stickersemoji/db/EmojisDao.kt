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

package global.msnthrp.xvii.data.stickersemoji.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import global.msnthrp.xvii.data.stickersemoji.model.Emoji
import global.msnthrp.xvii.data.stickersemoji.model.EmojiUsage
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface EmojisDao {

    @Query("SELECT * FROM emojis")
    fun getAllEmojis(): Single<List<Emoji>>

    @Query("SELECT emoji_code FROM emoji_usages WHERE last_used != 0 ORDER BY last_used DESC LIMIT 40")
    fun getRecentEmojis(): Single<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateStickerUsed(emojiUsage: EmojiUsage): Completable

}