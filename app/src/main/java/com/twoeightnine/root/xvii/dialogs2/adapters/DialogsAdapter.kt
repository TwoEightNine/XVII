package com.twoeightnine.root.xvii.dialogs2.adapters

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseReachAdapter
import com.twoeightnine.root.xvii.dialogs2.models.Dialog
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.item_dialog_new.view.*

class DialogsAdapter(
        context: Context,
        loader: (Int) -> Unit,
        private val onClick: (Dialog) -> Unit,
        private val onLongClick: (Dialog) -> Unit
) : BaseReachAdapter<Dialog, DialogsAdapter.DialogViewHolder>(context, loader) {


    override fun createStubLoadItem() = Dialog()

    override fun createHolder(parent: ViewGroup, viewType: Int) = DialogViewHolder(inflater.inflate(R.layout.item_dialog_new, null))

    override fun bind(holder: DialogViewHolder, item: Dialog) {
        holder.bind(item)
    }

    inner class DialogViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(dialog: Dialog) {
            with(itemView) {
                civPhoto.load(dialog.photo ?: App.PHOTO_STUB)

                tvTitle.text = dialog.title
                tvBody.text = if (EmojiHelper.hasEmojis(dialog.text)) {
                    EmojiHelper.getEmojied(
                            context,
                            dialog.text,
                            Html.fromHtml(getMessageBody(context, dialog)) as SpannableStringBuilder
                    )
                } else {
                    Html.fromHtml(getMessageBody(context, dialog))
                }
                tvDate.text = getTime(dialog.timeStamp)

                ivMute.setVisible(dialog.isMute)
                tvYou.setVisible(dialog.isOut)
                ivOnlineDot.setVisible(dialog.isOnline)
                rlMute.hide() //setVisible(dialog.isMute)
                ivUnreadDotOut.setVisible(!dialog.isRead && dialog.isOut)
                rlUnreadCount.setVisible(!dialog.isRead && !dialog.isOut && dialog.unreadCount > 0)

                if (dialog.unreadCount != 0) {
                    val unread = if (dialog.unreadCount > 99) {
                        context.getString(R.string.unread100)
                    } else {
                        dialog.unreadCount.toString()
                    }
                    tvUnreadCount.text = unread
                }

                Style.forImageView(ivOnlineDot, Style.MAIN_TAG)
                Style.forImageView(ivUnreadDotOut, Style.MAIN_TAG)
                Style.forImageView(ivMute, Style.DARK_TAG)
                Style.forViewGroup(rlUnreadCount)
                rlItemContainer.setOnClickListener { onClick(items[adapterPosition]) }
                rlItemContainer.setOnLongClickListener {
                    onLongClick(items[adapterPosition])
                    true
                }
            }
        }

        private fun getMessageBody(context: Context, dialog: Dialog): String {
//            val fwdCount = if (message.fwdMessages == null) 0 else (message.fwdMessages as ArrayList).size
//            val attCount = if (message.attachments == null) 0 else (message.attachments as ArrayList).size
//            if (!message.body.isNullOrEmpty())
//                return message.body ?: ""
//            if (fwdCount != 0)
//                return context.getString(R.string.fwd_message, fwdCount)
//            if (attCount != 0) {
//                return context.resources.getQuantityString(R.plurals.attach, attCount, attCount)
//            }
//            if (message.action == Message.OUT_OF_CHAT) {
//                return context.getString(R.string.kick_chat)
//            }
//            if (message.action == Message.IN_CHAT) {
//                return context.getString(R.string.invite_chat)
//            }
            if (dialog.text.isNotEmpty()) {
                return dialog.text
            }
            return context.getString(R.string.error_message)
        }
    }
}