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