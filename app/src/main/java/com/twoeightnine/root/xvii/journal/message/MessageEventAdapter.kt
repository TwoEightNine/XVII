package com.twoeightnine.root.xvii.journal.message

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.backgroundColor
import androidx.core.text.color
import androidx.core.text.strikeThrough
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.journal.message.model.Change
import com.twoeightnine.root.xvii.journal.message.model.ChangeType
import com.twoeightnine.root.xvii.journal.message.model.MessageEvent
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.getTime
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.item_message_event.view.*

class MessageEventAdapter(context: Context) : BaseAdapter<MessageEvent, MessageEventAdapter.OnlineEventViewHolder>(context) {

    private val colorInsertText by lazy {
        ContextCompat.getColor(context, R.color.text_diff_insert)
    }
    private val colorRemoveText by lazy {
        ContextCompat.getColor(context, R.color.text_diff_remove)
    }
    private val colorInsertBackground by lazy {
        ContextCompat.getColor(context, R.color.text_diff_insert_background)
    }
    private val colorRemoveBackground by lazy {
        ContextCompat.getColor(context, R.color.text_diff_remove_background)
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ) = OnlineEventViewHolder(inflater.inflate(R.layout.item_message_event, parent, false))

    override fun onBindViewHolder(holder: OnlineEventViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class OnlineEventViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(event: MessageEvent) {
            with(itemView) {
                val time = getTime(event.time, withSeconds = Prefs.showSeconds)
                tvDate.text = time

                tvMessage.setText(event.difference?.let(::createTextWithDiff) ?: event.messageText, TextView.BufferType.SPANNABLE)
                tvMessage.setVisible(event.messageText.isNotBlank())

                tvDeleted.setVisible(event.isDeleted)
            }
        }

        private fun createTextWithDiff(difference: List<Change>): SpannableStringBuilder {
            return SpannableStringBuilder().apply {
                for (change in difference) {
                    when (change.type) {
                        ChangeType.KEEP -> append(change.word)
                        ChangeType.INSERT -> appendColored(colorInsertText, colorInsertBackground, change.word)
                        ChangeType.REMOVE -> strikeThrough { appendColored(colorRemoveText, colorRemoveBackground, change.word) }
                    }
                    append(' ')
                }
            }
        }

        private fun SpannableStringBuilder.appendColored(textColor: Int, backColor: Int, word: String) =
                color(textColor) { backgroundColor(backColor) { append(word) } }
    }
}