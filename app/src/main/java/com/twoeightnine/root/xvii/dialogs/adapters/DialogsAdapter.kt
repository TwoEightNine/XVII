package com.twoeightnine.root.xvii.dialogs.adapters

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseReachAdapter
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.item_dialog.view.*

class DialogsAdapter(
        context: Context,
        loader: (Int) -> Unit,
        private val onClick: (Dialog) -> Unit,
        private val onLongClick: (Dialog) -> Unit
) : BaseReachAdapter<Dialog, DialogsAdapter.DialogViewHolder>(context, loader) {


    override fun createStubLoadItem() = Dialog()

    override fun createHolder(parent: ViewGroup, viewType: Int)
            = DialogViewHolder(inflater.inflate(R.layout.item_dialog, null))

    override fun bind(holder: DialogViewHolder, item: Dialog) {
        holder.bind(item)
    }

    inner class DialogViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(dialog: Dialog) {
            with(itemView) {
                civPhoto.load(dialog.photo)

                tvTitle.text = dialog.alias ?: dialog.title
                if (Prefs.lowerTexts) tvTitle.lower()
                tvBody.text = if (EmojiHelper.hasEmojis(dialog.text)) {
                    EmojiHelper.getEmojied(
                            context,
                            dialog.text,
                            Html.fromHtml(getMessageBody(context, dialog)) as SpannableStringBuilder
                    )
                } else {
                    Html.fromHtml(getMessageBody(context, dialog))
                }
                tvDate.text = getTime(dialog.timeStamp, shortened = true, withSeconds = Prefs.showSeconds)

                ivMute.setVisible(dialog.isMute)
                tvYou.setVisible(dialog.isOut)
                ivPinned.setVisible(dialog.isPinned)
                ivOnlineDot.setVisible(dialog.isOnline)
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

                ivOnlineDot.stylize(ColorManager.MAIN_TAG)
                ivUnreadDotOut.stylize(ColorManager.MAIN_TAG, changeStroke = false)
//                ivMute.stylize(ColorManager.LIGHT_TAG)
//                ivPinned.stylize(ColorManager.LIGHT_TAG)
                rlUnreadCount.stylize()
                rlItemContainer.setOnClickListener { onClick(items[adapterPosition]) }
                rlItemContainer.setOnLongClickListener {
                    onLongClick(items[adapterPosition])
                    true
                }
            }
        }

        private fun getMessageBody(context: Context, dialog: Dialog): String {
            if (dialog.text.isNotEmpty()) {
                return dialog.text
            }
            return context.getString(R.string.error_message)
        }
    }
}