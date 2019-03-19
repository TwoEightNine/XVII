package com.twoeightnine.root.xvii.dialogs.adapters

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.PaginationAdapter
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.utils.EmojiHelper
import com.twoeightnine.root.xvii.utils.getTime
import com.twoeightnine.root.xvii.utils.load
import com.twoeightnine.root.xvii.utils.setVisible
import kotlinx.android.synthetic.main.item_dialog.view.*

open class DialogsAdapter(
        context: Context,
        loader: (Int) -> Unit,
        protected var clickListener: (Int) -> Unit,
        protected var longClickListener: (Int) -> Boolean
) : PaginationAdapter<Message>(context, loader) {


    override var stubLoadItem: Message? = Message.stubLoad

    override fun isStubLoad(obj: Message) = Message.isStubLoad(obj)

    override var stubTryItem: Message? = Message.stubTry

    override fun isStubTry(obj: Message) = Message.isStubTry(obj)

    override fun createHolder(parent: ViewGroup,
                              viewType: Int) = DialogViewHolder(inflater.inflate(R.layout.item_dialog, parent, false))


    override fun onBindViewHolder(vholder: RecyclerView.ViewHolder, position: Int) {
        (vholder as? DialogViewHolder)?.bind(items[position])
    }

    inner class DialogViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        fun bind(message: Message) {
            with(itemView) {

                civPhoto.load(message.photo ?: App.PHOTO_STUB)

                tvTitle.text = message.title
                tvBody.text = if (message.emoji == 1) {
                    EmojiHelper.getEmojied(
                            context,
                            message.body ?: "",
                            Html.fromHtml(getMessageBody(context, message)) as SpannableStringBuilder
                    )
                } else {
                    Html.fromHtml(getMessageBody(context, message))
                }
                tvDate.text = getTime(message.date)

                ivOnlineDot.setVisible(message.online == 1)
                rlMute.setVisible(message.isMute)
                ivUnreadDotOut.setVisible(!message.isRead && message.isOut)
                rlUnreadCount.setVisible(!message.isRead && !message.isOut && message.unread > 0)

                if (message.unread != 0) {
                    val unread = if (message.unread > 99) context.getString(R.string.unread100) else message.unread.toString()
                    tvUnreadCount.text = unread
                }

                Style.forImageView(ivOnlineDot, Style.MAIN_TAG)
                Style.forImageView(ivUnreadDotOut, Style.MAIN_TAG)
                Style.forViewGroup(rlUnreadCount)
                rlItemContainer.setOnClickListener { clickListener.invoke(adapterPosition) }
                rlItemContainer.setOnLongClickListener { longClickListener.invoke(adapterPosition) }
            }
        }

        private fun getMessageBody(context: Context, message: Message): String {
            val fwdCount = if (message.fwdMessages == null) 0 else (message.fwdMessages as ArrayList).size
            val attCount = if (message.attachments == null) 0 else (message.attachments as ArrayList).size
            if (!message.body.isNullOrEmpty())
                return message.body ?: ""
            if (fwdCount != 0)
                return context.getString(R.string.fwd_message, fwdCount)
            if (attCount != 0) {
                return context.resources.getQuantityString(R.plurals.attach, attCount, attCount)
            }
            if (message.action == Message.OUT_OF_CHAT) {
                return context.getString(R.string.kick_chat)
            }
            if (message.action == Message.IN_CHAT) {
                return context.getString(R.string.invite_chat)
            }
            return context.getString(R.string.error_message)
        }
    }


}