package com.twoeightnine.root.xvii.chats.adapters

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
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
                  private val clickListener: (Int) -> Unit,
                  private val longClickListener: (Int) -> Boolean,
                  private val userClickListener: (Int) -> Unit,
                  private val decryptCallback: (Doc) -> Unit = {},
                  private val onPhotoClick: (Photo) -> Unit = {},
                  private val onVideoClick: (Video) -> Unit = {},
                  private val isImportant: Boolean = false) : PaginationAdapter<Message>(context, loader) {
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
        return when {
            super.getItemViewType(position) != NOSTUB -> super.getItemViewType(position)
            message.isOut -> OUT
            message.chatId != 0 || isImportant -> IN_CHAT
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
                rlBack.setOnClickListener { clickListener.invoke(adapterPosition) }
                rlBack.setOnLongClickListener { longClickListener.invoke(adapterPosition) }
                tvBody.setOnClickListener { clickListener.invoke(adapterPosition) }
                tvBody.setOnLongClickListener { longClickListener.invoke(adapterPosition) }
            }
        }

        private fun putViews(view: View, message: Message, level: Int) {
            with(view) {
                if (level == 0) {
                    if (multiSelectRaw.contains(message.id)) {
                        rlBack.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_mess))
                    } else {
                        rlBack.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    }
                } else {
                    rlBack.setBackgroundColor(Color.TRANSPARENT)
                }

                llMessage.layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT

                if (tvName is TextView) {
                    tvName?.text = message.title
                }

                if (TextUtils.isEmpty(message.body) && TextUtils.isEmpty(message.action)) {
                    tvBody.visibility = View.GONE
                } else {
                    tvBody.visibility = View.VISIBLE
                    if (TextUtils.isEmpty(message.action)) {
                        if (message.emoji == 1) {
                            tvBody.text = EmojiHelper.getEmojied(context, message.body
                                    ?: "")
                        } else if (message.body != null && isDecrypted(message.body!!)) {
                            tvBody.text = getWrapped(message.body!!)
                        } else {
                            tvBody.text = message.body
                        }
                    } else {
                        var body = ""
                        if (message.action == Message.IN_CHAT) {
                            body = context.getString(R.string.invite_chat_full, "" + message.actionMid!!)
                        }
                        if (message.action == Message.OUT_OF_CHAT) {
                            body = context.getString(R.string.kick_chat_full, "" + message.actionMid!!)
                        }
                        if (message.action == Message.TITLE_UPDATE) {
                            body = context.getString(R.string.chat_title_updated, message.actionText)
                        }
                        if (message.action == Message.CREATE) {
                            body = context.getString(R.string.chat_created)
                        }
                        tvBody.text = body
                    }
                }

                tvDate.text = getTime(message.date, full = true)

                if (civPhoto != null) {
                    val photoAva = message.photo
                    if (photoAva != null) {
                        Picasso.get()
                                .load(photoAva)
                                .placeholder(R.drawable.placeholder)
                                .into(civPhoto)
                    } else {
                        Picasso.get()
                                .load(R.drawable.placeholder)
                                .into(civPhoto)
                    }
                    civPhoto!!.setOnClickListener { userClickListener.invoke(message.userId) }
                }

                if (readStateDot != null) {
                    val d = ContextCompat.getDrawable(context, R.drawable.unread_dot_shae)
                    Style.forDrawable(d, Style.MAIN_TAG)
                    if (!message.isRead && message.isOut) {
                        (readStateDot as ImageView).setImageDrawable(d)
                    } else {
                        (readStateDot as ImageView).setImageDrawable(null)
                    }
                }

                Style.forMessage(llMessage, level + message.out)

                if (message.isImportant) {
                    rlImportant.visibility = View.VISIBLE
                } else {
                    rlImportant.visibility = View.GONE
                }

                llMessageContainer.removeAllViews()

                if (message.attachments != null && message.attachments!!.size > 0) {
                    if (message.attachments?.get(0)?.type == Attachment.TYPE_STICKER) {
                        llMessage.layoutParams.width = pxFromDp(context, 180)
                    } else {
                        llMessage.layoutParams.width = MEDIA_WIDTH
                    }
                    val atts = message.attachments
                    for (i in atts!!.indices) {
                        val included: View

                        when (atts[i].type) {

                            Attachment.TYPE_PHOTO -> {
                                val photo = atts[i].photo
                                llMessageContainer.addView(getPhoto(photo!!, context, onPhotoClick))
                            }

                            Attachment.TYPE_STICKER -> {
                                included = LayoutInflater.from(context).inflate(R.layout.container_sticker, null, false)
                                val stickPath = atts[i].sticker!!.photoMax
                                if (stickPath.isNotEmpty()) {
                                    Picasso.get()
                                            .load(stickPath)
                                            .into(included.findViewById<ImageView>(R.id.ivInternal))
                                }
                                llMessageContainer.addView(included)
                            }

                            Attachment.TYPE_GRAFFITI -> {
                                included = LayoutInflater.from(context).inflate(R.layout.container_photo, null, false)
                                Picasso.get()
                                        .load(atts[i].graffiti!!.url)
                                        .into(included.findViewById<ImageView>(R.id.ivInternal))
                                llMessageContainer.addView(included)
                            }

                            Attachment.TYPE_GIFT -> {
                                included = LayoutInflater.from(context).inflate(R.layout.container_photo, null, false)
                                Picasso.get()
                                        .load(atts[i].gift!!.thumb256)
                                        .into(included.findViewById<ImageView>(R.id.ivInternal))
                                llMessageContainer.addView(included)
                            }

                            Attachment.TYPE_AUDIO -> {
                                val audio = atts[i].audio
                                llMessageContainer.addView(getAudio(audio!!, context))
                            }

                            Attachment.TYPE_LINK -> {
                                val link = atts[i].link
                                llMessageContainer.addView(getLink(link!!, context))
                            }

                            Attachment.TYPE_VIDEO -> {
                                val video = atts[i].video
                                llMessageContainer.addView(getVideo(video!!, context, onVideoClick))
                            }

                            Attachment.TYPE_DOC -> {
                                val doc = atts[i].doc ?: return
                                when {
                                    doc.isVoiceMessage -> {
                                        llMessageContainer.addView(
                                                getAudio(Audio(
                                                        doc.preview?.audioMsg ?: return,
                                                        context.getString(R.string.voice_message)
                                                ), context))
                                    }
                                    doc.isGif -> {
                                        llMessageContainer.addView(getGif(doc, context))
                                    }
                                    doc.isGraffiti -> {
                                        included = LayoutInflater.from(context).inflate(R.layout.container_photo, null, false)
                                        Picasso.get()
                                                .load(doc.preview!!.graffiti!!.src)
                                                .into(included.findViewById<ImageView>(R.id.ivInternal))
                                        llMessageContainer.addView(included)
                                    }
                                    doc.isEncrypted -> {
                                        llMessageContainer.addView(getEncrypted(doc, context, decryptCallback))
                                    }
                                    else -> {
                                        llMessageContainer.addView(getDoc(doc, context))
                                    }
                                }
                            }

                            Attachment.TYPE_WALL -> {
                                val post = atts[i].wall
                                val postId = post!!.stringId
                                included = LayoutInflater.from(context).inflate(R.layout.container_wall, null, false)
                                included.setOnClickListener {
                                    (context as RootActivity).loadFragment(WallPostFragment.newInstance(postId))
                                }
                                llMessageContainer.addView(included)
                            }
                        }
                    }
                }

                if (message.fwdMessages != null && message.fwdMessages!!.size > 0) {
                    llMessage.layoutParams.width = MEDIA_WIDTH
                    val fwdMesses = message.fwdMessages
                    for (i in fwdMesses!!.indices) {
                        val included = inflater.inflate(R.layout.item_message_in_chat, null)
                        included.tag = true
                        putViews(included, fwdMesses[i], level + 1)
                        llMessageContainer.addView(included)
                    }
                }
            }

        }

        private fun isDecrypted(body: String): Boolean {
            val prefix = context.getString(R.string.decrypted, "")
            return body.startsWith(prefix)
        }

        private fun getWrapped(text: String): Spanned {
            val prefix = context.getString(R.string.decrypted, "")
            val color = String.format("%X", ContextCompat.getColor(context, R.color.minor_text)).substring(2)
            val result = "<font color=\"#$color\"><i>$prefix</i></font>${text.substring(prefix.length)}"
            return Html.fromHtml(result)
        }
    }

    companion object {

        const val OUT = 0
        const val IN_CHAT = 1
        const val IN_USER = 2

        var MEDIA_WIDTH: Int = 0
    }
}
