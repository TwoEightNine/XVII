package com.twoeightnine.root.xvii.journal

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.longpoll.models.events.OnlineEvent
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.getTime
import global.msnthrp.xvii.core.journal.model.JournalEvent
import global.msnthrp.xvii.core.journal.model.JournalEventWithPeer
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import global.msnthrp.xvii.uikit.extensions.lowerIf
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.item_journal_event.view.*

class JournalAdapter(
        context: Context,
        private val onClick: (JournalEventWithPeer) -> Unit
) : BaseAdapter<JournalEventWithPeer, JournalAdapter.JournalEventViewHolder>(context) {

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): JournalEventViewHolder = JournalEventViewHolder(inflater.inflate(R.layout.item_journal_event, parent, false))

    override fun onBindViewHolder(holder: JournalEventViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class JournalEventViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(event: JournalEventWithPeer) {
            with(itemView) {
                xaPhoto.load(event.fromPhoto, event.fromName.first().toString(), event.journalEvent.peerId)
                tvName.text = event.fromName
                tvName.lowerIf(Prefs.lowerTexts)

                (event.journalEvent.timeStamp / 1000L).toInt().also { time ->
                    tvDate.text = getTime(time, withSeconds = Prefs.showSeconds)
                }
                tvEvent.text = getEventText(event)

                ivDetails.paint(Munch.color.color)

                val messageText = getMessageText(event.journalEvent)?.let { "\"$it\"" }
                tvMessage.setVisible(messageText != null)
                messageText?.also(tvMessage::setText)

                setOnClickListener { items.getOrNull(adapterPosition)?.also(onClick) }
            }
        }

        private fun getEventText(event: JournalEventWithPeer): String? {
            (event.journalEvent as? JournalEvent.StatusJE.OnlineStatusJE)?.also { onlineEvent ->
                val deviceName = OnlineEvent.getDeviceName(context.resources, onlineEvent.deviceCode)
                return context.getString(R.string.journal_event_online, deviceName)
            }
            return when (event.journalEvent) {
                is JournalEvent.StatusJE.OnlineStatusJE -> R.string.journal_event_online
                is JournalEvent.StatusJE.OfflineStatusJE -> R.string.journal_event_offline
                is JournalEvent.MessageJE.ReadMessageJE -> R.string.journal_event_read
                is JournalEvent.MessageJE.DeletedMessageJE -> R.string.journal_event_delete
                is JournalEvent.MessageJE.NewMessageJE -> R.string.journal_event_new
                is JournalEvent.MessageJE.EditedMessageJE -> R.string.journal_event_edited
                is JournalEvent.ActivityJE.TypingActivityJE -> R.string.journal_event_typing
                is JournalEvent.ActivityJE.RecordingActivityJE -> R.string.journal_event_recording
                else -> null
            }?.let(context::getString)
        }

        private fun getMessageText(event: JournalEvent): String? = when (event) {
            is JournalEvent.MessageJE.NewMessageJE -> event.messageText
            is JournalEvent.MessageJE.EditedMessageJE -> event.messageText
            else -> null
        }
    }
}