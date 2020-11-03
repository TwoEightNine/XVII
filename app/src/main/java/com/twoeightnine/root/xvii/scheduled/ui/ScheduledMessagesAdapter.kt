package com.twoeightnine.root.xvii.scheduled.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.BaseAdapter
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.scheduled.core.ScheduledMessage
import com.twoeightnine.root.xvii.utils.getTime
import com.twoeightnine.root.xvii.utils.lower
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.item_scheduled_message.view.*

class ScheduledMessagesAdapter(
        context: Context,
        private val onClick: (ScheduledMessage) -> Unit
) : BaseAdapter<ScheduledMessage, ScheduledMessagesAdapter.ScheduledMessageViewHolder>(context) {

    private var peersMap: Map<Int, String> = hashMapOf()

    fun updatePeersMap(peersMap: Map<Int, String>) {
        this.peersMap = peersMap
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ) = ScheduledMessageViewHolder(inflater.inflate(R.layout.item_scheduled_message, parent, false))

    override fun onBindViewHolder(holder: ScheduledMessageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ScheduledMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(scheduledMessage: ScheduledMessage) {
            with(itemView) {
                tvPeer.text = peersMap[scheduledMessage.peerId] ?: "id${scheduledMessage.peerId}"
                if (Prefs.lowerTexts) {
                    tvPeer.lower()
                }

                tvText.setVisible(scheduledMessage.text.isNotBlank())
                tvText.text = scheduledMessage.text

                tvWhen.text = getTime((scheduledMessage.whenMs / 1000).toInt(), withSeconds = Prefs.showSeconds)

                val info = getInfo(scheduledMessage.attachments, scheduledMessage.forwardedMessages)
                tvInfo.setVisible(info != null)
                tvInfo.text = info

                setOnClickListener { onClick(items[adapterPosition]) }
            }
        }

        private fun getInfo(attachments: String?, forwardedMessages: String?): String? {
            if (attachments.isNullOrBlank() && forwardedMessages.isNullOrBlank()) {
                return null
            }
            val attachmentsCount = attachments?.split(",")?.size ?: 0
            val hasForwardedMessages = forwardedMessages?.isNotBlank() == true
            val info = StringBuilder()
            if (attachmentsCount != 0) {
                info.append(context.resources.getQuantityString(R.plurals.attachments, attachmentsCount, attachmentsCount))
                if (hasForwardedMessages) {
                    info.append(", ")
                }
            }
            if (hasForwardedMessages) {
                info.append(context.getString(R.string.scheduled_messages_has_fwd_messages))
            }
            return info.toString()
        }
    }
}