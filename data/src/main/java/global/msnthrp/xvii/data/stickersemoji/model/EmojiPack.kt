package global.msnthrp.xvii.data.stickersemoji.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EmojiPack(

        /**
         * name of pack or null if recent emojis
         */
        val name: String?,

        val emojis: List<Emoji>
) : Parcelable