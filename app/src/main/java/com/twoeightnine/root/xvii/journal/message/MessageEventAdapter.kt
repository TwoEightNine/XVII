package com.twoeightnine.root.xvii.journal.message

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.journal.message.model.MessageEvent
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.getTime
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.item_message_event.view.*

class MessageEventAdapter(context: Context) : BaseAdapter<MessageEvent, MessageEventAdapter.OnlineEventViewHolder>(context) {

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

                tvMessage.text = event.messageText
                tvMessage.setVisible(event.messageText.isNotBlank())

                tvDeleted.setVisible(event.isDeleted)
            }
        }
    }
}