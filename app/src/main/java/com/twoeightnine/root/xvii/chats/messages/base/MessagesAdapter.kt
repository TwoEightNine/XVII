package com.twoeightnine.root.xvii.chats.messages.base

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
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
import com.twoeightnine.root.xvii.storage.SessionProvider
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.*
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.lowerIf
import global.msnthrp.xvii.uikit.extensions.setVisible
import global.msnthrp.xvii.uikit.extensions.setVisibleWithInvis
import kotlinx.android.synthetic.main.item_message_in_chat.view.*
import kotlinx.android.synthetic.main.item_message_in_chat.view.rlDateSeparator
import kotlinx.android.synthetic.main.item_message_out.view.*
import kotlinx.android.synthetic.main.item_message_wtf.view.*
import kotlinx.android.synthetic.main.item_message_wtf.view.civPhoto
import kotlinx.android.synthetic.main.item_message_wtf.view.llMessage
import kotlinx.android.synthetic.main.item_message_wtf.view.llMessageContainer
import kotlinx.android.synthetic.main.item_message_wtf.view.readStateDot
import kotlinx.android.synthetic.main.item_message_wtf.view.rlBack
import kotlinx.android.synthetic.main.item_message_wtf.view.tvBody
import kotlinx.android.synthetic.main.item_message_wtf.view.tvDateOutside
import kotlinx.android.synthetic.main.item_message_wtf.view.tvDateSeparator
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

    private val userId = SessionProvider.userId

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

                val dateOnlyTime = getTime(message.date, noDate = true, withSeconds = Prefs.showSeconds)
                val dateOnlyDay = getDate(message.date)
                val dateOnlyDayPrev = prevMessage?.date?.let(::getDate)

                val edited = if (message.isEdited()) resources.getString(R.string.edited) else ""
                val dateMessage = "$dateOnlyTime $edited"

                val zeroLevel = level == 0
                val dateChanged = dateOnlyDayPrev == null || dateOnlyDayPrev != dateOnlyDay
                tvDateOutside.setVisibleWithInvis(zeroLevel)
                rlDateSeparator.setVisible(dateChanged && zeroLevel)

                tvDateSeparator.text = dateOnlyDay
                tvDateOutside.text = dateMessage

                //
                // block of optional fields
                //
                val showName = shouldShowName(message, prevMessage)
                rlName?.setVisible(showName)
                if (showName) {
                    tvName?.apply {
                        text = message.name
                        lowerIf(Prefs.lowerTexts)
                    }
                    civPhoto?.apply {
                        load(message.photo, message.name?.getInitials(), id = message.fromId)
                    }
                    rlName?.setOnClickListener {
                        items.getOrNull(adapterPosition)
                                ?.message
                                ?.fromId
                                ?.also(messageCallback::onUserClicked)
                    }
                }
                readStateDot?.apply {
                    stylize(ColorManager.MAIN_TAG, changeStroke = false)
                    setVisibleWithInvis(!message.read && message.isOut())
                }
                pbSending?.setVisible(isNotSent && !hasAttachmentsOrForwarded)
                pbSending?.setIndicatorColor(Munch.color.color)

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
                if (!message.attachments.isNullOrEmpty()) {
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
                                tvDateOutside.hide()
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
                message.replyMessage?.also { message ->
                    val included = inflater.inflate(R.layout.item_message_in_chat, null)
                    with(included.rlBack) {
                        setPadding(paddingLeft, paddingTop, 6, paddingBottom)
                    }
                    putViews(included, WrappedMessage(message), null, level + 1, isOutgoingStack)
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

        private fun ViewGroup.stylizeAsMessage(level: Int, hide: Boolean = false) {
            (background as GradientDrawable).setColor(
                    when {
                        hide -> Color.TRANSPARENT
                        level % 2 == 0 -> Munch.color.color20
                        else -> Munch.color.color10
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
