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
import com.twoeightnine.root.xvii.activities.RootActivity
import com.twoeightnine.root.xvii.base.BaseReachAdapter
import com.twoeightnine.root.xvii.fragments.WallPostFragment
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.*
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.item_message_wtf.view.*

/**
 * definitely it waits for refactoring
 */
class MessagesAdapter(context: Context,
                      loader: (Int) -> Unit,
                      private val callback: MessagesAdapter.Callback,
                      private val settings: MessagesAdapter.Settings
) : BaseReachAdapter<Message2, MessagesAdapter.MessageViewHolder>(context, loader) {

    private val mediaWidth = pxFromDp(context, MEDIA_WIDTH)

    override fun createHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            OUT -> MessageViewHolder(inflater.inflate(R.layout.item_message_out, null))
            IN_CHAT -> MessageViewHolder(inflater.inflate(R.layout.item_message_in_chat, null))
            IN_USER -> MessageViewHolder(inflater.inflate(R.layout.item_message_in_user, null))
            else -> MessageViewHolder(inflater.inflate(R.layout.item_message_in_chat, null))
        }
    }

    override fun bind(holder: MessageViewHolder, item: Message2) {
        holder.bind(item)
    }

    override fun createStubLoadItem() = Message2()

    override fun getItemViewType(position: Int): Int {
        val message = items[position]
        val superType = super.getItemViewType(position)
        return when {
            superType != NO_STUB -> superType
            message.isOut() -> OUT
            message.isChat() || settings.isImportant -> IN_CHAT
            else -> IN_USER
        }
    }

    inner class MessageViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        fun bind(message: Message2, level: Int = 0) {
            putViews(itemView, message, level)
            with(itemView) {
                rlBack.setOnClickListener { onClick(items[adapterPosition]) }
                rlBack.setOnLongClickListener { onLongClick(items[adapterPosition]) }
                tvBody.setOnClickListener { onClick(items[adapterPosition]) }
                tvBody.setOnLongClickListener { onLongClick(items[adapterPosition]) }
            }
        }

        private fun onClick(message: Message2) {
            if (multiSelectMode) {
                multiSelect(message)
                invalidateBackground(message)
            } else {
                callback.onClicked(message)
            }
        }

        private fun onLongClick(message: Message2): Boolean {
            if (!multiSelectMode) {
                multiSelectMode = true
                multiSelect(message)
                invalidateBackground(message)
                return true
            }
            return false
        }

        private fun invalidateBackground(message: Message2, view: View = itemView, level: Int = 0) {
            with(view) {
                rlBack.setBackgroundColor(if (level == 0 && message in multiSelect) {
                    ContextCompat.getColor(context, R.color.selected_mess)
                } else {
                    Color.TRANSPARENT
                })
            }
        }

        private fun putViews(view: View, message: Message2, level: Int) {
            with(view) {
                invalidateBackground(message, this, level)
                llMessage.layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT
                tvName?.text = message.name
                tvBody.setVisible(message.text.isNotEmpty() || !message.action.isNullOrEmpty())
                tvBody.text = when {
                    message.text.isNotEmpty() -> when {
                        EmojiHelper.hasEmojis(message.text) -> EmojiHelper.getEmojied(context, message.text)
                        isDecrypted(message.text) -> getWrapped(message.text)
                        else -> message.text
                    }
                    !message.action.isNullOrEmpty() -> getAction(message)
                    else -> ""
                }
                tvDate.text = getTime(message.date, full = true)
                civPhoto?.apply {
                    load(message.photo)
                    setOnClickListener { callback.onUserClicked(message.fromId) }
                }
                readStateDot?.apply {
                    Style.forImageView(this, Style.MAIN_TAG, changeStroke = false)
                    visibility = if (!message.read && message.isOut()) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                }
                Style.forMessage(llMessage, level + message.out)
                rlImportant.hide()
                llMessageContainer.removeAllViews()

                if (!message.attachments.isNullOrEmpty()) {
                    llMessage.layoutParams.width = when {
                        message.isSticker() -> pxFromDp(context, 180)
//                        message.isSinglePhoto() -> LinearLayout.LayoutParams.WRAP_CONTENT
                        else -> mediaWidth
                    }
                    message.attachments.forEach { attachment ->
                        when (attachment.type) {

                            Attachment.TYPE_PHOTO -> attachment.photo?.also {
                                llMessageContainer.addView(getPhoto(it, context, callback::onPhotoClicked))
                            }

                            Attachment.TYPE_STICKER -> attachment.sticker?.photoMax?.also {
                                val included = LayoutInflater.from(context).inflate(R.layout.container_sticker, null, false)
                                included.findViewById<ImageView>(R.id.ivInternal).load(it, placeholder = false)
                                llMessageContainer.addView(included)
                            }

                            Attachment.TYPE_GRAFFITI -> attachment.graffiti?.url?.also {
                                val included = LayoutInflater.from(context).inflate(R.layout.container_photo, null, false)
                                included.findViewById<ImageView>(R.id.ivInternal).load(it, placeholder = false)
                                llMessageContainer.addView(included)
                            }

                            Attachment.TYPE_GIFT -> attachment.gift?.thumb256?.also {
                                val included = LayoutInflater.from(context).inflate(R.layout.container_photo, null, false)
                                included.findViewById<ImageView>(R.id.ivInternal).load(it)
                                llMessageContainer.addView(included)
                            }

                            Attachment.TYPE_AUDIO -> attachment.audio?.also {
                                llMessageContainer.addView(getAudio(it, context))
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

                            Attachment.TYPE_WALL -> attachment.wall?.stringId?.also { postId ->
                                val included = LayoutInflater.from(context).inflate(R.layout.container_wall, null, false)
                                included.setOnClickListener {
                                    (context as? RootActivity)?.loadFragment(WallPostFragment.newInstance(postId))
                                }
                                llMessageContainer.addView(included)
                            }
                        }
                    }
                }

                if (!message.fwdMessages.isNullOrEmpty()) {
                    llMessage.layoutParams.width = mediaWidth
                    message.fwdMessages.forEach {
                        val included = inflater.inflate(R.layout.item_message_in_chat, null)
                        included.tag = true
                        putViews(included, it, level + 1)
                        llMessageContainer.addView(included)
                    }
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

        private fun getAction(message: Message2) = when (message.action) {
            Message.IN_CHAT -> context.getString(R.string.invite_chat_full, "${message.actionMid}")
            Message.OUT_OF_CHAT -> context.getString(R.string.kick_chat_full, "${message.actionMid}")
            Message.TITLE_UPDATE -> context.getString(R.string.chat_title_updated, message.actionText)
            Message.CREATE -> context.getString(R.string.chat_created)
            else -> ""
        }
    }

    interface Callback {
        fun onClicked(message: Message2)
        fun onUserClicked(userId: Int)
        fun onEncryptedFileClicked(doc: Doc)
        fun onPhotoClicked(photo: Photo)
        fun onVideoClicked(video: Video)
    }

    data class Settings(
            val isImportant: Boolean
    )

    companion object {

        const val OUT = 0
        const val IN_CHAT = 1
        const val IN_USER = 2

        const val MEDIA_WIDTH = 276
    }
}
