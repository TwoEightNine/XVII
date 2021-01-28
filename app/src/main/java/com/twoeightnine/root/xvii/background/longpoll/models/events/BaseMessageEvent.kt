package com.twoeightnine.root.xvii.background.longpoll.models.events

import android.content.Context
import com.google.gson.internal.LinkedTreeMap
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.utils.matchesUserId

/**
 * for handling [BaseLongPollEvent.TYPE_NEW_MESSAGE] and [BaseLongPollEvent.TYPE_EDIT_MESSAGE]
 *
 * if has the same format but two different meanings
 */
abstract class BaseMessageEvent(
        val id: Int,
        private val flags: Int,
        val peerId: Int,
        val timeStamp: Int,
        val text: String,
        val info: MessageInfo,
        val randomId: Int = 0
) : BaseLongPollEvent() {

    companion object {
        const val FLAG_UNREAD = 1
        const val FLAG_OUT = 2
    }

    val title
        get() = info.title

    fun isUnread() = (flags and FLAG_UNREAD) > 0

    fun isOut() = (flags and FLAG_OUT) > 0

    fun hasMedia() = info.attachments != null || info.getForwardedCount() > 0

    fun isUser() = peerId.matchesUserId()

    fun hasEmoji() = info.emoji

    fun getResolvedMessage(context: Context?, hideContent: Boolean = false): String = when {
        context == null -> text
        text.isNotBlank() && hideContent -> context.getString(R.string.hidden_message)
        text.isNotBlank() -> text
        info.attachments != null -> {
            val count = info.attachments.count
            when {
                info.attachments.isSticker -> context.getString(R.string.sticker)
                info.attachments.isGraffiti -> context.getString(R.string.graffiti)
                info.attachments.isGift -> context.getString(R.string.gift_for_you)
                info.attachments.isAudioMessage -> context.getString(R.string.voice_message)
                info.attachments.isPoll -> context.getString(R.string.poll)
                info.attachments.isLink -> context.getString(R.string.link)
                info.attachments.isWallPost -> context.getString(R.string.wall_post)

                info.attachments.photosCount != 0 ->
                    context.resources.getQuantityString(R.plurals.attachments_photos, count, count)

                info.attachments.videosCount != 0 ->
                    context.resources.getQuantityString(R.plurals.attachments_videos, count, count)

                info.attachments.audiosCount != 0 ->
                    context.resources.getQuantityString(R.plurals.attachments_audios, count, count)

                info.attachments.docsCount != 0 ->
                    context.resources.getQuantityString(R.plurals.attachments_docs, count, count)

                else -> context.resources.getQuantityString(R.plurals.attachments, count, count)
            }
        }
        info.getForwardedCount() > 0 -> {
            val count = info.getForwardedCount()
            context.resources.getQuantityString(R.plurals.messages, count, count)
        }
        else -> text
    }

    data class MessageInfo(
            val title: String = "",
            val from: Int = 0,
            val emoji: Boolean = false,
            val forwarded: String = "",
            val attachments: AttachmentsInfo? = null
    ) {
        companion object {

            private const val TITLE = "title"
            private const val FROM = "from"
            private const val EMOJI = "emoji"
            private const val FWD = "fwd"

            fun fromLinkedTreeMap(data: LinkedTreeMap<String, Any>): MessageInfo {
                return MessageInfo(
                        title = (data[TITLE] as? String) ?: "",
                        from = (data[FROM] as? String)?.toInt() ?: 0,
                        emoji = (data[EMOJI] as? String) == "1",
                        forwarded = (data[FWD] as? String) ?: "",
                        attachments = AttachmentsInfo.fromLinkedTreeMap(data)
                )
            }
        }

        fun getForwardedCount() = if (forwarded.isEmpty()) 0 else forwarded.split(",").size
    }

    data class AttachmentsInfo(
            val isSticker: Boolean = false,
            val isAudioMessage: Boolean = false,
            val isGraffiti: Boolean = false,
            val isLink: Boolean = false,
            val isPoll: Boolean = false,
            val isGift: Boolean = false,
            val isWallPost: Boolean = false,

            val photosCount: Int = 0,
            val videosCount: Int = 0,
            val audiosCount: Int = 0,
            val docsCount: Int = 0
    ) {

        val count: Int
            get() = photosCount + videosCount + audiosCount + docsCount

        companion object {

            fun fromLinkedTreeMap(data: LinkedTreeMap<String, Any>): AttachmentsInfo? {
                var photosCount = 0
                var videosCount = 0
                var docsCount = 0
                var audiosCount = 0
                for (i in 1..10) {
                    val key = "attach${i}_type"
                    if (key in data) {
                        when(data[key]) {
                            Attachment.TYPE_STICKER -> return AttachmentsInfo(isSticker = true)
                            Attachment.TYPE_AUDIO_MESSAGE -> return AttachmentsInfo(isAudioMessage = true)
                            Attachment.TYPE_POLL -> return AttachmentsInfo(isPoll = true)
                            Attachment.TYPE_GIFT -> return AttachmentsInfo(isGift = true)
                            Attachment.TYPE_LINK -> return AttachmentsInfo(isLink = true)
                            Attachment.TYPE_GRAFFITI -> return AttachmentsInfo(isGraffiti = true)
                            Attachment.TYPE_WALL -> return AttachmentsInfo(isWallPost = true)

                            Attachment.TYPE_PHOTO -> photosCount++
                            Attachment.TYPE_VIDEO -> videosCount++
                            Attachment.TYPE_AUDIO -> audiosCount++
                            Attachment.TYPE_DOC -> {
                                val kind = "attach${i}_kind"
                                if (kind in data) {
                                    when(data[kind]) {
                                        Attachment.TYPE_AUDIO_MSG -> return AttachmentsInfo(isAudioMessage = true)
                                        else -> docsCount++
                                    }
                                } else {
                                    docsCount++
                                }
                            }
                        }
                    }
                }
                val info = AttachmentsInfo(
                        photosCount = photosCount,
                        videosCount = videosCount,
                        audiosCount = audiosCount,
                        docsCount = docsCount
                )
                return if (info.count == 0) null else info
            }
        }
    }
}