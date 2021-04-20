package global.msnthrp.xvii.data.stickersemoji.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "emoji_usages")
@Parcelize
data class EmojiUsage(

        @PrimaryKey
        @ColumnInfo(name = "emoji_code")
        val emojiCode: String,

        @ColumnInfo(name = "last_used")
        val lastUsed: Int
) : Parcelable