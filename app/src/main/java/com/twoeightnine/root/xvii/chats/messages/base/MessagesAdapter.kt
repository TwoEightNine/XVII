package com.twoeightnine.root.xvii.chats.messages.base

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseReachAdapter
import com.twoeightnine.root.xvii.chats.messages.deepforwarded.DeepForwardedActivity
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.attachments.*
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.wallpost.WallPostActivity
import kotlinx.android.synthetic.main.item_message_wtf.view.*

/**
 * definitely it waits for refactoring
 */
class MessagesAdapter(context: Context,
                      loader: (Int) -> Unit,
                      private val callback: Callback,
                      private val settings: Settings
) : BaseReachAdapter<Message, MessagesAdapter.MessageViewHolder>(context, loader) {

    private val mediaWidth = pxFromDp(context, MEDIA_WIDTH)

    override fun createHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            OUT -> MessageViewHolder(inflater.inflate(R.layout.item_message_out, null))
            IN_CHAT -> MessageViewHolder(inflater.inflate(R.layout.item_message_in_chat, null))
            IN_USER -> MessageViewHolder(inflater.inflate(R.layout.item_message_in_user, null))
            SYSTEM -> MessageViewHolder(inflater.inflate(R.layout.item_message_system, null))
            else -> MessageViewHolder(inflater.inflate(R.layout.item_message_in_chat, null))
        }
    }

    override fun bind(holder: MessageViewHolder, item: Message) {
        holder.bind(item)
    }

    override fun createStubLoadItem() = Message()

    override fun getItemViewType(position: Int): Int {
        val message = items[position]
        val superType = super.getItemViewType(position)
        return when {
            superType != NO_STUB -> superType
            message.isSystem() -> SYSTEM
            message.isOut() -> OUT
            message.isChat() || settings.isImportant -> IN_CHAT
            else -> IN_USER
        }
    }

    inner class MessageViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        fun bind(message: Message, level: Int = 0) {
            putViews(itemView, message, level)
            if (!message.isSystem()) {
                with(itemView) {
                    rlBack.setOnClickListener { onClick(items[adapterPosition]) }
                    rlBack.setOnLongClickListener { onLongClick(items[adapterPosition]) }
                    tvBody.setOnClickListener { onClick(items[adapterPosition]) }
                    tvBody.setOnLongClickListener { onLongClick(items[adapterPosition]) }
                }
            }
        }

        private fun onClick(message: Message) {
            if (multiSelectMode) {
                multiSelect(message)
                invalidateBackground(message)
            } else {
                callback.onClicked(message)
            }
        }

        private fun onLongClick(message: Message): Boolean {
            if (!multiSelectMode) {
                multiSelectMode = true
                multiSelect(message)
                invalidateBackground(message)
                return true
            }
            return false
        }

        private fun invalidateBackground(message: Message, view: View = itemView, level: Int = 0) {
            with(view) {
                rlBack.setBackgroundColor(if (level == 0 && message in multiSelect) {
                    ContextCompat.getColor(context, R.color.selected_mess)
                } else {
                    Color.TRANSPARENT
                })
            }
        }

        private fun putViews(view: View, message: Message, level: Int) {
            with(view) {
                if (message.isSystem()) {
                    tvSystem.text = message.action?.getSystemMessage(context)
                    var userId = message.action?.memberId
                    if (userId == 0 || userId == null) {
                        userId = message.fromId
                    }
                    tvSystem.setOnClickListener { callback.onUserClicked(userId) }
                    return
                }
                invalidateBackground(message, this, level)
                llMessage.layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT
                tvName?.text = message.name
                if (Prefs.lowerTexts) tvName?.lower()
                tvBody.setVisible(message.text.isNotEmpty())
                tvBody.text = when {
                    message.text.isNotEmpty() -> when {
                        EmojiHelper.hasEmojis(message.text) -> EmojiHelper.getEmojied(context, message.text)
                        isDecrypted(message.text) -> getWrapped(message.text)
                        else -> message.text
                    }
                    else -> ""
                }
                val date = getTime(message.date, withSeconds = Prefs.showSeconds)
                val edited = if (message.isEdited()) resources.getString(R.string.edited) else ""
                tvDate.text = "$date $edited"
                civPhoto?.apply {
                    load(message.photo)
                    setOnClickListener { callback.onUserClicked(message.fromId) }
                }
                readStateDot?.apply {
                    stylize(ColorManager.MAIN_TAG, changeStroke = false)
                    visibility = if (!message.read && message.isOut()) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                }
                llMessage.stylizeAsMessage(level + message.out, hide = message.isSticker())
                rlImportant.hide()
                llMessageContainer.removeAllViews()

                if (!message.attachments.isNullOrEmpty()) {
                    llMessage.layoutParams.width = when {
                        message.isSticker() -> pxFromDp(context, 180)
                        else -> mediaWidth
                    }
                    message.attachments.forEach { attachment ->
                        when (attachment.type) {

                            Attachment.TYPE_PHOTO -> attachment.photo?.also {
                                llMessageContainer.addView(getPhoto(it, context) { photo ->
                                    val photos = message.attachments.getPhotos()
                                    callback.onPhotoClicked(photos.indexOf(photo), photos)
                                })
                            }

                            Attachment.TYPE_STICKER -> attachment.sticker?.photo512?.also {
                                val included = LayoutInflater.from(context).inflate(R.layout.container_sticker, null, false)
                                included.findViewById<ImageView>(R.id.ivInternal).load(it, placeholder = false)
                                llMessageContainer.addView(included)
                            }

                            Attachment.TYPE_GIFT -> attachment.gift?.thumb256?.also {
                                val included = LayoutInflater.from(context).inflate(R.layout.container_photo, null, false)
                                included.findViewById<ImageView>(R.id.ivInternal).load(it)
                                llMessageContainer.addView(included)
                            }

                            Attachment.TYPE_AUDIO -> attachment.audio?.also {
                                val audios = arrayListOf<Audio>()
                                items.forEach { message ->
                                    message.attachments?.getAudios()?.apply {
                                        audios.addAll(filterNotNull())
                                    }
                                }
                                llMessageContainer.addView(getAudio(it, context, audios))
                            }

                            Attachment.TYPE_AUDIO_MESSAGE -> attachment.audioMessage?.also { audioMessage ->
                                val audios = items.mapNotNull { it.attachments?.getAudioMessage() }
                                        .map { Audio(it, context.getString(R.string.voice_message)) }
                                llMessageContainer.addView(getAudio(
                                        Audio(audioMessage, context.getString(R.string.voice_message)),
                                        context, audios
                                ))
                            }

                            Attachment.TYPE_LINK -> attachment.link?.also {
                                llMessageContainer.addView(getLink(it, context))
                            }

                            Attachment.TYPE_VIDEO -> attachment.video?.also {
                                llMessageContainer.addView(getVideo(it, context, callback::onVideoClicked))
                            }

                            Attachment.TYPE_DOC -> attachment.doc?.also { doc ->
                                when {
                                    doc.isVoiceMessage -> doc.preview?.audioMsg?.also {
                                        llMessageContainer.addView(getAudio(
                                                Audio(it, context.getString(R.string.voice_message)),
                                                context))
                                    }
                                    doc.isGif -> {
                                        llMessageContainer.addView(getGif(doc, context))
                                    }
                                    doc.isGraffiti -> doc.preview?.graffiti?.src?.also {
                                        val included = LayoutInflater.from(context).inflate(R.layout.container_photo, null, false)
                                        included.findViewById<ImageView>(R.id.ivInternal).load(it, placeholder = false)
                                        llMessageContainer.addView(included)
                                    }
                                    doc.isEncrypted -> {
                                        llMessageContainer.addView(getEncrypted(doc, context, callback::onEncryptedFileClicked))
                                    }
                                    else -> {
                                        llMessageContainer.addView(getDoc(doc, context))
                                    }
                                }

                            }

                            Attachment.TYPE_POLL -> attachment.poll?.also {
                                llMessageContainer.addView(getPoll(it, context))
                            }

                            Attachment.TYPE_WALL -> attachment.wall?.stringId?.also { postId ->
                                val included = LayoutInflater.from(context).inflate(R.layout.container_wall, null, false)
                                included.setOnClickListener {
                                    WallPostActivity.launch(context
                                            ?: return@setOnClickListener, postId)
                                }
                                llMessageContainer.addView(included)
                            }
                        }
                    }
                }

                if (!message.fwdMessages.isNullOrEmpty()) {
                    llMessage.layoutParams.width = if (settings.fullDeepness) {
                        ViewGroup.LayoutParams.MATCH_PARENT
                    } else {
                        mediaWidth
                    }
                    rlBack.setPadding(rlBack.paddingLeft, rlBack.paddingTop, 6, rlBack.paddingBottom)
                    message.fwdMessages.forEach {
                        val included = inflater.inflate(R.layout.item_message_in_chat, null)
                        included.tag = true
                        with(included.rlBack) {
                            setPadding(paddingLeft, paddingTop, 6, paddingBottom)
                        }
                        if (level < ALLOWED_DEEPNESS || settings.fullDeepness) {
                            putViews(included, it, level + 1)
                        } else {
                            with(included) {
                                tvBody.text = resources.getString(R.string.too_deep_forwarding)
                                civPhoto.hide()
                                tvName.hide()
                                tvDate.hide()
                                setOnClickListener { DeepForwardedActivity.launch(context, items[adapterPosition].id) }
                            }
                        }
                        llMessageContainer.addView(included)
                    }
                }
                message.replyMessage?.also {
                    llMessage.layoutParams.width = when {
                        message.isReplyingSticker() -> pxFromDp(context, 180)
                        else -> mediaWidth
                    }
                    val included = inflater.inflate(R.layout.item_message_in_chat, null)
                    included.tag = true
                    with(included.rlBack) {
                        setPadding(paddingLeft, paddingTop, 6, paddingBottom)
                    }
                    putViews(included, it, level + 1)
                    llMessageContainer.addView(included)
                }
            }
        }

        private fun isDecrypted(body: String?): Boolean {
            val prefix = context.getString(R.string.decrypted, "")
            return body?.startsWith(prefix) == true
        }

        private fun getWrapped(text: String?): Spanned {
            if (text.isNullOrEmpty()) return Html.fromHtml("")

            val prefix = context.getString(R.string.decrypted, "")
            val color = String.format("%X", ContextCompat.getColor(context, R.color.minor_text)).substring(2)
            val result = "<font color=\"#$color\"><i>$prefix</i></font>${text.substring(prefix.length)}"
            return Html.fromHtml(result)
        }
    }

    interface Callback {
        fun onClicked(message: Message)
        fun onUserClicked(userId: Int)
        fun onEncryptedFileClicked(doc: Doc)
        fun onPhotoClicked(position: Int, photos: ArrayList<Photo>)
        fun onVideoClicked(video: Video)
    }

    data class Settings(
            val isImportant: Boolean,

            /**
             * if true forwarded messages shown as is. used in [DeepForwardedFragment]
             */
            val fullDeepness: Boolean = false
    )

    companion object {
        const val ALLOWED_DEEPNESS = 2

        const val OUT = 0
        const val IN_CHAT = 1
        const val IN_USER = 2
        const val SYSTEM = 3

        const val MEDIA_WIDTH = 276
    }
}
