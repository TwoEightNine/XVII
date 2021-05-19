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

package global.msnthrp.xvii.data.stickersemoji.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "stickers")
@Parcelize
data class Sticker(

        @PrimaryKey
        val id: Int,

        @ColumnInfo(name = "key_words")
        val keyWords: String,

        @ColumnInfo(name = "key_words_custom")
        val keyWordsCustom: String,

        @ColumnInfo(name = "pack_name")
        val packName: String

) : Parcelable {

    val keyWordsList: List<String>
        get() = if (keyWords.isNotBlank()) keyWords.split(',') else listOf()

    override fun equals(other: Any?) =
            (other as? Sticker)?.id == id && id != 0

    override fun hashCode() = id

    val photo128: String
        get() = String.format(URL_128_FMT, id)

    val photo256: String
        get() = String.format(URL_256_FMT, id)

    val photo512: String
        get() = String.format(URL_512_FMT, id)

    companion object {

        const val URL_512_FMT = "https://vk.com/sticker/1-%d-512b"
        const val URL_256_FMT = "https://vk.com/sticker/1-%d-256b"
        const val URL_128_FMT = "https://vk.com/sticker/1-%d-128b"
    }

}