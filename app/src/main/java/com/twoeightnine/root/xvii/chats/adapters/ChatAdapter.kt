package com.twoeightnine.root.xvii.chats.adapters

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
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.RootActivity
import com.twoeightnine.root.xvii.adapters.PaginationAdapter
import com.twoeightnine.root.xvii.fragments.WallPostFragment
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.*
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.item_message_wtf.view.*

/**
 * definitely it waits for refactoring
 */
class ChatAdapter(context: Context,
                  loader: (Int) -> Unit,
                  private val callback: ChatAdapterCallback,
                  private val settings: ChatAdapterSettings
) : PaginationAdapter<Message>(context, loader) {

    var isAtEnd: Boolean = false
        private set

    init {
        MEDIA_WIDTH = pxFromDp(context, 276) // paddings
        isAtEnd = true
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            OUT -> ChatViewHolder(inflater.inflate(R.layout.item_message_out, null))
            IN_CHAT -> ChatViewHolder(inflater.inflate(R.layout.item_message_in_chat, null))
            IN_USER -> ChatViewHolder(inflater.inflate(R.layout.item_message_in_user, null))
            else -> ChatViewHolder(inflater.inflate(R.layout.item_message_in_chat, null))
        }
    }

    override var stubLoadItem: Message? = Message.stubLoad

    override fun isStubLoad(obj: Message) = Message.isStubLoad(obj)

    override var stubTryItem: Message? = Message.stubTry

    override fun isStubTry(obj: Message) = Message.isStubTry(obj)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = items[position]
        if (holder is ChatViewHolder) {
            holder.bind(message)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        //        super.onAttachedToRecyclerView(recyclerView);
        layoutManager = recyclerView.layoutManager
        recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isAtEnd = lastVisiblePosition() == items.size - 1
                if (dy >= 0)
                    return

                val total = itemCount
                val first = firstVisiblePosition()
                if (!isDone && !isLoading && first <= PaginationAdapter.THRESHOLD) {
                    loader.invoke(total)
                    startLoading()
                }
            }
        })
    }

    override fun addStubLoad() {
        if (!isLoaderAdded) {
            add(stubLoadItem!!, 0)
            isLoaderAdded = true
        }
    }

    override fun addStubTry() {
        if (!isTrierAdded) {
            add(stubTryItem!!, 0)
            isTrierAdded = true
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = items[position]
        val superType = super.getItemViewType(position)
        return when {
            superType != NOSTUB -> superType
            message.isOut -> OUT
            message.chatId != 0 || settings.isImportant -> IN_CHAT
            else -> IN_USER
        }
    }

    override fun add(item: Message) {
        super.add(item)
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        fun bind(message: Message, level: Int = 0) {
            putViews(itemView, message, level)
            with(itemView) {
                rlBack.setOnClickListener { callback.onClicked(items[adapterPosition]) }
                rlBack.setOnLongClickListener { callback.onLongClicked(items[adapterPosition]) }
                tvBody.setOnClickListener { callback.onClicked(items[adapterPosition]) }
                tvBody.setOnLongClickListener { callback.onLongClicked(items[adapterPosition]) }
            }
        }

        private fun putViews(view: View, message: Message, level: Int) {
            with(view) {
                rlBack.setBackgroundColor(if (level == 0 && message in multiSelect) {
                    ContextCompat.getColor(context, R.color.selected_mess)
                } else {
                    Color.TRANSPARENT
                })
                llMessage.layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT
                tvName?.text = message.title
                tvBody.setVisible(!message.body.isNullOrEmpty() || !message.action.isNullOrEmpty())
                tvBody.text = when {
                    !message.body.isNullOrEmpty() -> when {
                        message.emoji == 1 -> EmojiHelper.getEmojied(context, message.body ?: "")
                        isDecrypted(message.body) -> getWrapped(message.body)
                        else -> message.body
                    }
                    !message.action.isNullOrEmpty() -> getAction(message)
                    else -> ""
                }
                tvDate.text = getTime(message.date, full = true)
                civPhoto?.apply {
                    load(message.photo)
                    setOnClickListener { callback.onUserClicked(message.userId) }
                }
                readStateDot?.apply {
                    Style.forImageView(this, Style.MAIN_TAG)
                    visibility = if (!message.isRead && message.isOut) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                }
                Style.forMessage(llMessage, level + message.out)
                rlImportant.setVisible(message.isImportant)
                llMessageContainer.removeAllViews()

                if (!message.attachments.isNullOrEmpty()) {
                    llMessage.layoutParams.width = when {
                        message.attachments?.get(0)?.type == Attachment.TYPE_STICKER ->
                            pxFromDp(context, 180)
//                        message.attachments?.size ?: 0 == 1
//                                && message.attachments?.get(0)?.type == Attachment.TYPE_PHOTO ->
//                            LinearLayout.LayoutParams.WRAP_CONTENT
                        else -> MEDIA_WIDTH
                    }
                    message.attachments?.forEach { attachment ->
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
                                        llMessageContainer.addView(getEncrypted(doc, context, callback::onDocClicked))
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
                    llMessage.layoutParams.width = MEDIA_WIDTH
                    message.fwdMessages?.forEach {
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

        private fun getAction(message: Message) = when (message.action) {
            Message.IN_CHAT -> context.getString(R.string.invite_chat_full, "${message.actionMid}")
            Message.OUT_OF_CHAT -> context.getString(R.string.kick_chat_full, "${message.actionMid}")
            Message.TITLE_UPDATE -> context.getString(R.string.chat_title_updated, message.actionText)
            Message.CREATE -> context.getString(R.string.chat_created)
            else -> ""
        }
    }

    interface ChatAdapterCallback {
        fun onClicked(message: Message)
        fun onLongClicked(message: Message): Boolean
        fun onUserClicked(userId: Int)
        fun onDocClicked(doc: Doc)
        fun onPhotoClicked(photo: Photo)
        fun onVideoClicked(video: Video)
    }

    data class ChatAdapterSettings(
            val isImportant: Boolean
    )

    companion object {

        const val OUT = 0
        const val IN_CHAT = 1
        const val IN_USER = 2

        var MEDIA_WIDTH: Int = 0
    }
}
