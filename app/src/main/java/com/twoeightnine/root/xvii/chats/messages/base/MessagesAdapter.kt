package com.twoeightnine.root.xvii.chats.messages.base

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseReachAdapter
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.chats.attachments.AttachmentsInflater
import com.twoeightnine.root.xvii.chats.messages.deepforwarded.DeepForwardedFragment
import com.twoeightnine.root.xvii.extensions.getInitials
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.model.messages.WrappedMessage
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.XviiAvatar
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.*
import global.msnthrp.xvii.uikit.extensions.*
import kotlinx.android.synthetic.main.item_message_in_chat.view.*
import kotlinx.android.synthetic.main.item_message_in_chat.view.rlDateSeparator
import kotlinx.android.synthetic.main.item_message_out.view.*
import kotlinx.android.synthetic.main.item_message_replied.view.*
import kotlinx.android.synthetic.main.item_message_wtf.view.*
import kotlinx.android.synthetic.main.item_message_wtf.view.civPhoto
import kotlinx.android.synthetic.main.item_message_wtf.view.llMessage
import kotlinx.android.synthetic.main.item_message_wtf.view.llMessageContainer
import kotlinx.android.synthetic.main.item_message_wtf.view.rlBack
import kotlinx.android.synthetic.main.item_message_wtf.view.tvBody
import kotlinx.android.synthetic.main.item_message_wtf.view.tvDateAttachmentsEmbedded
import kotlinx.android.synthetic.main.item_message_wtf.view.tvDateAttachmentsOverlay
import kotlinx.android.synthetic.main.item_message_wtf.view.tvDateSeparator
import kotlinx.android.synthetic.main.item_message_wtf.view.tvDateText
import kotlinx.android.synthetic.main.item_message_wtf.view.tvDateTextInlined
import kotlinx.android.synthetic.main.item_message_wtf.view.tvName


/**
 * definitely it waits for refactoring
 */
class MessagesAdapter(context: Context,
                      loader: (Int) -> Unit,
                      private val messageCallback: Callback,
                      private val attachmentsCallback: AttachmentsInflater.Callback,
                      private val settings: Settings
) : BaseReachAdapter<WrappedMessage, MessagesAdapter.MessageViewHolder>(context, loader) {

    private val messageInflater = AttachmentsInflater(context, attachmentsCallback)

    private val messageTextSize by lazy {
        Prefs.messageTextSize.toFloat()
    }

    private val textWidthInlineFittness by lazy {
        context.resources.getDimensionPixelSize(R.dimen.chat_message_inline_fitness_width)
    }
    private val dateTextExtraPadding by lazy {
        context.resources.getDimensionPixelSize(R.dimen.chat_date_text_margin_end)
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

    override fun bind(holder: MessageViewHolder, item: WrappedMessage) {
        val position = items.indexOf(item)
        holder.bind(item, items.getOrNull(position - 1))
    }

    override fun createStubLoadItem() = WrappedMessage(Message())

    override fun getItemViewType(position: Int): Int {
        val message = items[position].message
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

        fun bind(wrappedMessage: WrappedMessage, prevWrappedMessage: WrappedMessage?, level: Int = 0) {
            val message = wrappedMessage.message

            if (message.isSystem()) {
                bindSystemMessage(itemView, message)
            } else {
                val isOutgoingStack = wrappedMessage.message.isOut() || !wrappedMessage.sent
                putViews(itemView, wrappedMessage, prevWrappedMessage, level, isOutgoingStack)
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

        private fun onClick(message: WrappedMessage) {
            if (multiSelectMode) {
                multiSelect(message)
                invalidateBackground(message)
            } else {
                messageCallback.onClicked(message.message)
            }
        }

        private fun onLongClick(message: WrappedMessage): Boolean {
            if (!multiSelectMode) {
                multiSelectMode = true
                multiSelect(message)
                invalidateBackground(message)
                return true
            }
            return false
        }

        private fun invalidateBackground(message: WrappedMessage, view: View = itemView, level: Int = 0) {
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
                tvSystem.setOnClickListener { messageCallback.onUserClicked(userId) }
            }
        }

        private fun putViews(
                view: View,
                wrappedMessage: WrappedMessage,
                prevWrappedMessage: WrappedMessage?,
                level: Int,
                isOutgoingStack: Boolean
        ) {
            val message = wrappedMessage.message
            val prevMessage = prevWrappedMessage?.message
            val isNotSent = !wrappedMessage.sent
            val hasAttachmentsOrForwarded = wrappedMessage.hasAttachmentsOrForwarded

            with(view) {
                //
                // block of common fields
                //
                invalidateBackground(wrappedMessage, this, level)

                bindMessageText(tvBody, message.text)
                bindMessageDate(message, prevMessage, level, rlDateSeparator, tvDateSeparator)
                bindMessageTime(
                        context,
                        message,
                        tvDateAttachmentsOverlay,
                        tvDateAttachmentsEmbedded,
                        tvDateTextInlined,
                        tvDateText,
                        tvBody
                )

                //
                // block of optional fields
                //
                bindName(message, prevMessage, rlName, tvName, civPhoto)

                ivSendingIcon?.setVisible(isNotSent)
                ivSendingIcon?.paint(Munch.color.color)

                ivReadDot?.apply {
                    paint(Munch.color.color)
                    setVisibleWithInvis(!message.read && message.isOut() && !isNotSent)
                }

                val paintDelta = if (isOutgoingStack) 1 else 0
                llMessage.stylizeAsMessage(
                        level + paintDelta,
                        hide = message.run { isSticker() || isGraffiti() || isGift() }
                )
                llMessageContainer.removeAllViews()

                if (isNotSent && hasAttachmentsOrForwarded) {
                    llMessageContainer.addView(messageInflater.getViewLoader())
                }

                llMessage.layoutParams.width = messageInflater.getMessageWidth(message, settings.fullDeepness, level)
                val hasAttachments = !message.attachments.isNullOrEmpty()
                val hasForwarded = !message.fwdMessages.isNullOrEmpty()
                val hasReplied = message.replyMessage != null
                val hasContent = hasAttachments || hasForwarded || hasReplied
                llMessageContainer.setVisible(hasContent)

                message.replyMessage
                        ?.let(messageInflater::getRepliedMessageView)
                        ?.also(llMessageContainer::addView)
                        ?.also { it.llRepliedMessage.stylizeAsMessage(level + paintDelta + 1) }

                if (hasAttachments) {
                    messageInflater
                            .createViewsFor(message, level)
                            .forEach(llMessageContainer::addView)
                }

                if (!message.fwdMessages.isNullOrEmpty()) {
                    rlBack.setPadding(rlBack.paddingLeft, rlBack.paddingTop, 6, rlBack.paddingBottom)
                    message.fwdMessages.forEachIndexed { index, innerMessage ->
                        val included = inflater.inflate(R.layout.item_message_in_chat, llMessageContainer, false)
                        val maxWidth = messageInflater.getMessageMaxWidth(settings.fullDeepness, level + 1)
                        (included.llMessage.layoutParams as? ConstraintLayout.LayoutParams)
                                ?.matchConstraintMaxWidth = maxWidth
                        with(included.rlBack) {
                            setPadding(paddingLeft, paddingTop, 6, paddingBottom)
                        }
                        if (level < ALLOWED_DEEPNESS || settings.fullDeepness) {
                            val wrappedInnerMessage = WrappedMessage(innerMessage)
                            val wrappedPrevInnerMessage = message.fwdMessages
                                    .getOrNull(index - 1)
                                    ?.let(::WrappedMessage)
                            putViews(included, wrappedInnerMessage, wrappedPrevInnerMessage, level + 1, isOutgoingStack)
                        } else {
                            with(included) {
                                tvBody.text = resources.getString(R.string.too_deep_forwarding)
                                tvBody.paint(Munch.color.color)
                                tvBody.paintFlags = tvBody.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                                rlName?.hide()
                                setOnClickListener {
                                    val messageId = items
                                            .getOrNull(adapterPosition)
                                            ?.message?.id
                                            ?: return@setOnClickListener
                                    context.startFragment<DeepForwardedFragment>(
                                            DeepForwardedFragment.createArgs(messageId)
                                    )
                                }
                            }
                        }
                        llMessageContainer.addView(included)
                    }
                }
            }
        }

        private fun bindMessageText(textView: TextView, messageText: String) {
            val isNotEmpty = messageText.isNotEmpty()
            textView.setVisible(isNotEmpty)

            if (isNotEmpty) {
                val preparedText = wrapMentions(context, messageText, addClickable = true)
                textView.text = when {
                    EmojiHelper.hasEmojis(messageText) -> EmojiHelper.getEmojied(context, messageText, preparedText)
                    else -> preparedText

                }

                // TODO move to one-time setup
                textView.movementMethod = LinkMovementMethod.getInstance()
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, messageTextSize)
            }
        }

        private fun bindMessageDate(
                message: Message,
                prevMessage: Message?,
                level: Int,
                dateContainer: View,
                dateTextView: TextView
        ) {
            val dateOnlyDay = getDate(message.date)
            val dateOnlyDayPrev = prevMessage?.date?.let(::getDate)

            val zeroLevel = level == 0
            val dateChanged = dateOnlyDayPrev == null || dateOnlyDayPrev != dateOnlyDay

            dateContainer.setVisible(dateChanged && zeroLevel)
            if (dateContainer.isVisible()) {
                dateTextView.text = dateOnlyDay
            }
        }

        private fun bindMessageTime(
                context: Context,
                message: Message,
                textViewOverlayed: TextView,
                textViewEmbedded: TextView,
                textViewInlined: TextView,
                textViewUnderlayed: TextView,
                textViewMessage: TextView
        ) {
            val dateOnlyTime = getTime(message.date, noDate = true, withSeconds = Prefs.showSeconds)

            val edited = when {
                message.isEdited() -> context.resources.getString(R.string.edited)
                else -> ""
            }
            val dateMessage = "$dateOnlyTime $edited"

            val dateToBeShown = when (messageInflater.getTimeStyle(message)) {

                AttachmentsInflater.TimeStyle.ATTACHMENTS_OVERLAYED -> {
                    textViewOverlayed
                }

                AttachmentsInflater.TimeStyle.ATTACHMENTS_EMBEDDED -> {
                    textViewEmbedded
                }

                AttachmentsInflater.TimeStyle.TEXT -> {
                    val bodyWidth = textViewMessage.paint.measureText(textViewMessage.text.toString())
                    val timeWidth = textViewUnderlayed.paint.measureText(dateMessage)

                    val freeSpace = textWidthInlineFittness - bodyWidth
                    val canBeInlined = freeSpace > timeWidth + dateTextExtraPadding

                    when {
                        canBeInlined -> textViewInlined
                        else -> textViewUnderlayed
                    }
                }
            }
            dateToBeShown.apply {
                text = dateMessage
                show()
            }
            listOf(textViewOverlayed,
                    textViewEmbedded,
                    textViewInlined,
                    textViewUnderlayed)
                    .filter { it != dateToBeShown }
                    .forEach { it.hide() }
        }

        private fun bindName(
                message: Message,
                prevMessage: Message?,
                nameContainer: View?,
                nameTextView: TextView?,
                photoImageView: XviiAvatar?
        ) {
            val showName = shouldShowName(message, prevMessage)
            nameContainer?.setVisible(showName)
            if (showName) {
                nameTextView?.apply {
                    text = message.name
                    lowerIf(Prefs.lowerTexts)
                }
                photoImageView?.apply {
                    load(message.photo, message.name?.getInitials(), id = message.fromId)
                }
                nameContainer?.setOnClickListener {
                    items.getOrNull(adapterPosition)
                            ?.message
                            ?.fromId
                            ?.also(messageCallback::onUserClicked)
                }
            }
        }

        private fun shouldShowName(message: Message, prevMessage: Message?) =
                // this message is first (no previous)
                prevMessage == null

                        // OR from different users
                        || message.fromId != prevMessage.fromId

                        // OR previous contains action
                        || prevMessage.isSystem()

                        // OR there are 2 hours between messages
                        || message.date - prevMessage.date > MESSAGES_BETWEEN_DELAY

        private fun ViewGroup.stylizeAsMessage(level: Int, hide: Boolean = false) {
            (background as GradientDrawable).setColor(
                    when {
                        hide -> Color.TRANSPARENT
                        level % 2 == 0 -> Munch.color.color(Munch.UseCase.MESSAGES_IN)
                        else -> Munch.color.color(Munch.UseCase.MESSAGES_OUT)
                    })
        }
    }

    interface Callback {
        fun onClicked(message: Message)
        fun onUserClicked(userId: Int)
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
    }
}
