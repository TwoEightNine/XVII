package com.twoeightnine.root.xvii.chats.messages.base

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseReachAdapter
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.chats.messages.deepforwarded.DeepForwardedActivity
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.WallPost
import com.twoeightnine.root.xvii.model.attachments.*
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.wallpost.WallPostFragment
import kotlinx.android.synthetic.main.container_wall.view.*
import kotlinx.android.synthetic.main.item_message_in_chat.view.*
import kotlinx.android.synthetic.main.item_message_wtf.view.*
import kotlinx.android.synthetic.main.item_message_wtf.view.civPhoto
import kotlinx.android.synthetic.main.item_message_wtf.view.llMessage
import kotlinx.android.synthetic.main.item_message_wtf.view.llMessageContainer
import kotlinx.android.synthetic.main.item_message_wtf.view.rlBack
import kotlinx.android.synthetic.main.item_message_wtf.view.tvBody
import kotlinx.android.synthetic.main.item_message_wtf.view.tvDate
import kotlinx.android.synthetic.main.item_message_wtf.view.tvName


/**
 * definitely it waits for refactoring
 */
class MessagesAdapter(context: Context,
                      loader: (Int) -> Unit,
                      private val callback: Callback,
                      private val settings: Settings
) : BaseReachAdapter<Message, MessagesAdapter.MessageViewHolder>(context, loader) {

    private val mediaWidth = pxFromDp(context, MEDIA_WIDTH)

    private val messageTextSize by lazy {
        Prefs.messageTextSize.toFloat()
    }

    override fun createHolder(parent: ViewGroup, viewType: Int) = MessageViewHolder(inflater.inflate(
            when (viewType) {

                // outgoing. one for all types of chats
                OUT -> R.layout.item_message_out

                // incoming in conversations: with avatars and names
                IN_CHAT -> R.layout.item_message_in_chat

                // incoming in personal chats: no avatars and names
                IN_USER -> R.layout.item_message_in_user

                // system messages. one for all types
                SYSTEM -> R.layout.item_message_system

                // unreachable branch
                else -> R.layout.item_message_in_chat
            }, parent, false))

    override fun bind(holder: MessageViewHolder, item: Message) {
        val position = items.indexOf(item)
        holder.bind(item, items.getOrNull(position - 1))
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

        fun bind(message: Message, prevMessage: Message?, level: Int = 0) {

            if (message.isSystem()) {
                bindSystemMessage(itemView, message)
            } else {
                putViews(itemView, message, prevMessage, level)
                with(itemView) {
                    rlBack.setOnClickListener {
                        items.getOrNull(adapterPosition)?.also(::onClick)
                    }
                    rlBack.setOnLongClickListener {
                        items.getOrNull(adapterPosition)?.let(::onLongClick) == true
                    }
                    tvBody.setOnClickListener {
                        items.getOrNull(adapterPosition)?.also(::onClick)
                    }
                    tvBody.setOnLongClickListener {
                        items.getOrNull(adapterPosition)?.let(::onLongClick) == true
                    }
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

        private fun bindSystemMessage(view: View, message: Message) {
            with(view) {
                tvSystem.text = message.action?.getSystemMessage(context)
                var userId = message.action?.memberId
                if (userId == 0 || userId == null) {
                    userId = message.fromId
                }
                tvSystem.setOnClickListener { callback.onUserClicked(userId) }
            }
        }

        private fun putViews(view: View, message: Message, prevMessage: Message?, level: Int) {
            with(view) {
                //
                // block of common fields
                //
                invalidateBackground(message, this, level)
                llMessage.layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT

                val preparedText = wrapMentions(context, message.text, addClickable = true)
                tvBody.setVisible(message.text.isNotEmpty())
                tvBody.text = when {
                    message.text.isNotEmpty() -> when {
                        EmojiHelper.hasEmojis(message.text) -> EmojiHelper.getEmojied(context, message.text, preparedText)
                        isDecrypted(message.text) -> getWrapped(message.text)
                        else -> preparedText
                    }
                    else -> ""
                }
                tvBody.movementMethod = LinkMovementMethod.getInstance()
                tvBody.setTextSize(TypedValue.COMPLEX_UNIT_SP, messageTextSize)

                val date = getTime(message.date, withSeconds = Prefs.showSeconds)
                val edited = if (message.isEdited()) resources.getString(R.string.edited) else ""
                tvDate.text = "$date $edited"

                //
                // block of optional fields
                //
                val showName = shouldShowName(message, prevMessage)
                rlName?.setVisible(showName)
                if (showName) {
                    tvName?.apply {
                        text = message.name
                        if (Prefs.lowerTexts) lower()
                    }
                    civPhoto?.apply {
                        load(message.photo)
                    }
                    rlName?.setOnClickListener {
                        items.getOrNull(adapterPosition)
                                ?.fromId
                                ?.also(callback::onUserClicked)
                    }
                }
                readStateDot?.apply {
                    stylize(ColorManager.MAIN_TAG, changeStroke = false)
                    setVisibleWithInvis(!message.read && message.isOut())
                }

                llMessage.stylizeAsMessage(
                        level + message.out,
                        hide = message.run { isSticker() || isGraffiti() || isGift() }
                )
                llMessageContainer.removeAllViews()

                if (!message.attachments.isNullOrEmpty()) {
                    llMessage.layoutParams.width = when {
                        message.isSticker() -> pxFromDp(context, 180)
                        message.isGraffiti() -> pxFromDp(context, 220)
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
                                val included = LayoutInflater.from(context).inflate(R.layout.container_sticker, null)
                                included.findViewById<ImageView>(R.id.ivInternal).load(it, placeholder = false)
                                llMessageContainer.addView(included)
                            }

                            Attachment.TYPE_GIFT -> attachment.gift?.also {
                                llMessageContainer.addView(getGift(context, it, message.text))
                                tvBody.hide()
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
                                val audios = arrayListOf<Audio>()
                                items.forEach { message ->
                                    message.attachments?.getAudioMessage()?.apply {
                                        audios.add(Audio(this, context.getString(R.string.voice_message)))
                                    }
                                }
                                llMessageContainer.addView(getAudio(
                                        Audio(audioMessage, context.getString(R.string.voice_message)),
                                        context, audios, audioMessage.transcript
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
                                    doc.isEncrypted -> {
                                        llMessageContainer.addView(getEncrypted(doc, context, callback::onEncryptedFileClicked))
                                    }
                                    else -> {
                                        llMessageContainer.addView(getDoc(doc, context))
                                    }
                                }

                            }

                            Attachment.TYPE_GRAFFITI -> attachment.graffiti?.url?.also { graffiti ->
                                val included = LayoutInflater.from(context).inflate(R.layout.container_sticker, null)
                                included.findViewById<ImageView>(R.id.ivInternal).load(graffiti, placeholder = false)
                                llMessageContainer.addView(included)
                            }

                            Attachment.TYPE_POLL -> attachment.poll?.also {
                                llMessageContainer.addView(getPoll(it, context))
                            }

                            Attachment.TYPE_WALL -> attachment.wall?.also { wallPost ->
                                val postId = wallPost.stringId
                                val included = LayoutInflater.from(context).inflate(R.layout.container_wall, null)
                                included.setOnClickListener {
                                    context?.startFragment<WallPostFragment>(
                                            WallPostFragment.createArgs(postId)
                                    )
                                }
                                bindWallPost(wallPost, included)
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
                    message.fwdMessages.forEachIndexed { index, innerMessage ->
                        val included = inflater.inflate(R.layout.item_message_in_chat, null)
                        included.tag = true
                        with(included.rlBack) {
                            setPadding(paddingLeft, paddingTop, 6, paddingBottom)
                        }
                        if (level < ALLOWED_DEEPNESS || settings.fullDeepness) {
                            putViews(included, innerMessage, message.fwdMessages.getOrNull(index - 1), level + 1)
                        } else {
                            with(included) {
                                tvBody.text = resources.getString(R.string.too_deep_forwarding)
                                rlName?.hide()
                                tvDate.hide()
                                setOnClickListener {
                                    val messageId = items
                                            .getOrNull(adapterPosition)
                                            ?.id ?: return@setOnClickListener
                                    DeepForwardedActivity.launch(context, messageId)
                                }
                            }
                        }
                        llMessageContainer.addView(included)
                    }
                }
                message.replyMessage?.also { message ->
                    llMessage.layoutParams.width = when {
                        message.isReplyingSticker() -> pxFromDp(context, 180)
                        else -> mediaWidth
                    }
                    val included = inflater.inflate(R.layout.item_message_in_chat, null)
                    included.tag = true
                    with(included.rlBack) {
                        setPadding(paddingLeft, paddingTop, 6, paddingBottom)
                    }
                    putViews(included, message, null, level + 1)
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

        private fun shouldShowName(message: Message, prevMessage: Message?) =
                // this message is first (no previous)
                prevMessage == null ||

                        // OR from different users
                        message.fromId != prevMessage.fromId ||

                        // OR previous contains action
                        prevMessage.isSystem() ||

                        // OR there are 2 hours between messages
                        message.date - prevMessage.date > MESSAGES_BETWEEN_DELAY

        private fun bindWallPost(wallPost: WallPost, included: View) {
            val title = wallPost.group?.name ?: wallPost.user?.fullName
            val photo = wallPost.group?.photo100 ?: wallPost.user?.photo100
            if (title == null && photo == null) return

            with(included) {
                tvName.show()
                civPhoto.show()
                tvPlaceHolder.hide()

                civPhoto.load(photo)
                tvName.text = title?.toLowerCase()
                if (!wallPost.text.isNullOrBlank()) {
                    tvText.show()
                    tvText.text = wallPost.text
                }
                try {
                    wallPost.attachments?.getPhotos()?.also { photos ->
                        if (photos.isNotEmpty()) {
                            ivPhoto.show()
                            Picasso.get()
                                    .loadRounded(photos[0].getOptimalPhoto()?.url)
                                    .resize(
                                            resources.getDimensionPixelSize(R.dimen.chat_wall_post_image_width),
                                            resources.getDimensionPixelSize(R.dimen.chat_wall_post_image_height)
                                    )
                                    .centerCrop()
                                    .into(ivPhoto)
                        }
                    }
                } catch (e: Exception) {
                    ivPhoto.hide()
                    L.tag("messages")
                            .throwable(e)
                            .log("binding wall post error")
                }
            }
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

        const val MESSAGES_BETWEEN_DELAY = 60 * 60 * 2 // 2 hours
        const val ALLOWED_DEEPNESS = 2

        const val OUT = 0
        const val IN_CHAT = 1
        const val IN_USER = 2
        const val SYSTEM = 3

        const val MEDIA_WIDTH = 276
    }
}
