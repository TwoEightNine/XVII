package com.twoeightnine.root.xvii.chats.adapters

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.RootActivity
import com.twoeightnine.root.xvii.adapters.PaginationAdapter
import com.twoeightnine.root.xvii.fragments.WallPostFragment
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.Audio
import com.twoeightnine.root.xvii.model.Doc
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.utils.*
import de.hdodenhof.circleimageview.CircleImageView

/**
 * definitely it waits for refactoring
 */
class ChatAdapter(context: Context,
                  loader: (Int) -> Unit,
                  private val clickListener: (Int) -> Unit,
                  private val longClickListener: (Int) -> Boolean,
                  private val userClickListener: (Int) -> Unit,
                  private val decryptCallback: (Doc) -> Unit = {},
                  private val isImportant: Boolean = false) : PaginationAdapter<Message>(context, loader) {
    var isAtEnd: Boolean = false
        private set

    init {
        MEDIA_WIDTH = pxFromDp(context, 276) // paddings
        isAtEnd = true
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
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
            putViews(holder, message, 0, position)
        }
    }

    private fun putViews(viewHolder: ChatViewHolder, message: Message, level: Int, position: Int) {

        if (level == 0) {
            if (multiSelectRaw.contains(message.id)) {
                viewHolder.rlBack.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_mess))
            } else {
                viewHolder.rlBack.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            }
        } else {
            viewHolder.rlBack.setBackgroundColor(Color.TRANSPARENT)
        }

        viewHolder.llMessage.layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT

        if (viewHolder.tvName is TextView) {
            viewHolder.tvName?.text = message.title
        }

        if (TextUtils.isEmpty(message.body) && TextUtils.isEmpty(message.action)) {
            viewHolder.tvBody.visibility = View.GONE
        } else {
            viewHolder.tvBody.visibility = View.VISIBLE
            //            Util.setUbuntu(mContext, viewHolder.tvBody);
            if (TextUtils.isEmpty(message.action)) {
                if (message.emoji == 1) {
                    viewHolder.tvBody.text = EmojiHelper.getEmojied(context, message.body ?: "")
                } else if (message.body != null && isDecrypted(message.body!!)) {
                    viewHolder.tvBody.text = getWrapped(message.body!!)
                } else {
                    viewHolder.tvBody.text = message.body
                }
            } else {
                var body = ""
                if (message.action == Message.INCHAT) {
                    body = context.getString(R.string.invite_chat_full, "" + message.actionMid!!)
                }
                if (message.action == Message.OUTOFCHAT) {
                    body = context.getString(R.string.kick_chat_full, "" + message.actionMid!!)
                }
                if (message.action == Message.TITLE_UPDATE) {
                    body = context.getString(R.string.chat_title_updated, message.actionText)
                }
                if (message.action == Message.CREATE) {
                    body = context.getString(R.string.chat_created)
                }
                viewHolder.tvBody.text = body
            }
        }

        viewHolder.tvDate.text = getTime(message.date, true)

        if (viewHolder.civPhoto != null) {
            val photoAva = message.photo
            if (photoAva != null) {
                Picasso.with(context)
                        .load(photoAva)
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.civPhoto)
            } else {
                Picasso.with(context)
                        .load(R.drawable.placeholder)
                        .into(viewHolder.civPhoto)
            }
            viewHolder.civPhoto!!.setOnClickListener { userClickListener.invoke(message.userId) }
        }

        if (viewHolder.readStateDot != null) {
            val d = ContextCompat.getDrawable(context, R.drawable.unread_dot_shae)
            Style.forDrawable(d, Style.MAIN_TAG)
            if (!message.isRead && message.isOut) {
                (viewHolder.readStateDot as ImageView).setImageDrawable(d)
            } else {
                (viewHolder.readStateDot as ImageView).setImageDrawable(null)
            }
        }

        Style.forMessage(viewHolder.llMessage, level + message.out)

        if (message.isImportant) {
            viewHolder.rlImportant.visibility = View.VISIBLE
        } else {
            viewHolder.rlImportant.visibility = View.GONE
        }

        viewHolder.llMessageContainer.removeAllViews()

        if (message.attachments != null && message.attachments!!.size > 0) {
            viewHolder.llMessage.layoutParams.width = MEDIA_WIDTH
            val atts = message.attachments
            for (i in atts!!.indices) {
                val included: View

                when (atts[i].type) {

                    Attachment.TYPE_PHOTO -> {
                        val photo = atts[i].photo
                        viewHolder.llMessageContainer.addView(getPhoto(photo!!, context))
                    }

                    Attachment.TYPE_STICKER -> {
                        included = LayoutInflater.from(context).inflate(R.layout.container_photo, null, false)
                        Picasso.with(context)
                                .load(atts[i].sticker!!.photoMax)
                                .into(included.findViewById<ImageView>(R.id.ivInternal))
                        viewHolder.llMessageContainer.addView(included)
                    }

                    Attachment.TYPE_GIFT -> {
                        included = LayoutInflater.from(context).inflate(R.layout.container_photo, null, false)
                        Picasso.with(context)
                                .load(atts[i].gift!!.thumb256)
                                .into(included.findViewById<ImageView>(R.id.ivInternal))
                        viewHolder.llMessageContainer.addView(included)
                    }

                    Attachment.TYPE_AUDIO -> {
                        val audio = atts[i].audio
                        viewHolder.llMessageContainer.addView(getAudio(audio!!, context))
                    }

                    Attachment.TYPE_LINK -> {
                        val link = atts[i].link
                        viewHolder.llMessageContainer.addView(getLink(link!!, context))
                    }

                    Attachment.TYPE_VIDEO -> {
                        val video = atts[i].video
                        viewHolder.llMessageContainer.addView(getVideo(video!!, context))
                    }

                    Attachment.TYPE_DOC -> {
                        val doc = atts[i].doc
                        if (doc!!.isVoiceMessage) {
                            viewHolder.llMessageContainer.addView(
                                    getAudio(Audio(doc, context.getString(R.string.voice_message)), context))
                        } else if (doc.isGif) {
                            viewHolder.llMessageContainer.addView(getGif(doc, context))
                        } else if (doc.isEncrypted) {
                            viewHolder.llMessageContainer.addView(getEncrypted(doc, context, decryptCallback))
                        } else {
                            viewHolder.llMessageContainer.addView(getDoc(doc, context))
                        }
                    }

                    Attachment.TYPE_WALL -> {
                        val post = atts[i].wall
                        val postId = post!!.stringId
                        included = LayoutInflater.from(context).inflate(R.layout.container_wall, null, false)
                        included.setOnClickListener {
                            (context as RootActivity).loadFragment(WallPostFragment.newInstance(postId))
                        }
                        viewHolder.llMessageContainer.addView(included)
                    }
                }
            }
        }

        if (message.fwdMessages != null && message.fwdMessages!!.size > 0) {
            viewHolder.llMessage.layoutParams.width = MEDIA_WIDTH
            val fwdMesses = message.fwdMessages
            for (i in fwdMesses!!.indices) {
                val included = inflater.inflate(R.layout.item_message_in_chat, null)
                included.tag = true
                putViews(ChatViewHolder(included), fwdMesses[i], level + 1, position)
                viewHolder.llMessageContainer.addView(included)
            }
        }

    }

    private fun isDecrypted(body: String): Boolean{
        val prefix = context.getString(R.string.decrypted, "")
        return body.startsWith(prefix)
    }

    private fun getWrapped(text: String): Spanned {
        val prefix = context.getString(R.string.decrypted, "")
        val color = String.format("%X", ContextCompat.getColor(context, R.color.minor_text)).substring(2)
        val result = "<font color=\"#$color\"><i>$prefix</i></font>${text.substring(prefix.length)}"
        return Html.fromHtml(result)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        //        super.onAttachedToRecyclerView(recyclerView);
        layoutManager = recyclerView!!.layoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isAtEnd = lastVisiblePosition() == items.size - 1
                if (dy >= 0)
                    return

                val total = itemCount
                val first = firstVisiblePosition()
                if (!isDone && !isLoading && first <= PaginationAdapter.Companion.THRESHOLD) {
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

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        @BindView(R.id.rlBack)
        lateinit var rlBack: RelativeLayout
        @BindView(R.id.tvBody)
        lateinit var tvBody: TextView
        @BindView(R.id.llMessage)
        lateinit var llMessage: LinearLayout
        @BindView(R.id.tvDate)
        lateinit var tvDate: TextView
        @BindView(R.id.readStateDot) @JvmField
        var readStateDot: ImageView? = null
        @BindView(R.id.civPhoto) @JvmField
        var civPhoto: CircleImageView? = null
        @BindView(R.id.tvName) @JvmField
        var tvName: TextView? = null
        @BindView(R.id.llMessageContainer)
        lateinit var llMessageContainer: LinearLayout
        @BindView(R.id.rlImportant)
        lateinit var rlImportant: RelativeLayout

        init {
            ButterKnife.bind(this, itemView)
            rlBack.setOnClickListener { clickListener.invoke(adapterPosition) }
            rlBack.setOnLongClickListener { longClickListener.invoke(adapterPosition) }
            tvBody.setOnClickListener { clickListener.invoke(adapterPosition) }
            tvBody.setOnLongClickListener { longClickListener.invoke(adapterPosition) }
        }
    }

    companion object {

        val OUT = 0
        val IN_CHAT = 1
        val IN_USER = 2

        var MEDIA_WIDTH: Int = 0
    }
}
