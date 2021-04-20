package com.twoeightnine.root.xvii.journal.online

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.journal.online.model.OnlineEvent
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.getTime
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.item_offline_event.view.*
import kotlinx.android.synthetic.main.item_online_event.view.*

class OnlineEventAdapter(context: Context) : BaseAdapter<OnlineEvent, OnlineEventAdapter.OnlineEventViewHolder>(context) {

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ) = OnlineEventViewHolder(inflater.inflate(
            when (viewType) {
                TYPE_OFFLINE -> R.layout.item_offline_event
                else -> R.layout.item_online_event
            }, parent, false))

    override fun onBindViewHolder(holder: OnlineEventViewHolder, position: Int) {
        val current = items[position]
        val prev = items.getOrNull(position - 1)
        when {
            current.isOnline -> holder.bindOnline(current, prev)
            else -> holder.bindOffline(current)
        }
    }

    override fun getItemViewType(position: Int): Int = when {
        items[position].isOnline -> TYPE_ONLINE
        else -> TYPE_OFFLINE
    }

    companion object {
        private const val TYPE_ONLINE = 1312
        private const val TYPE_OFFLINE = 1313
    }

    inner class OnlineEventViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bindOnline(event: OnlineEvent, prevEvent: OnlineEvent?) {
            with(itemView) {
                val time = getTime(event.time, withSeconds = Prefs.showSeconds)
                val deviceName = com.twoeightnine.root.xvii.background.longpoll.models.events
                        .OnlineEvent.getDeviceName(context.resources, event.deviceCode)
                        .takeIf(String::isNotBlank)
                        ?.takeIf { event.isOnline }
                        ?.let { "\n$it" }
                        ?: ""
                tvTime.text = "$time$deviceName"

                vPastLine.paint(Munch.color.color)
                ivDot.paint(Munch.color.color)
                vFutureLine.paint(Munch.color.color)

                vPastLine.setVisible(!event.isOnline || event.isOnline && prevEvent?.isOnline == true)
                vFutureLine.setVisible(event.isOnline)
            }
        }

        fun bindOffline(event: OnlineEvent) {
            with(itemView) {
                val time = getTime(event.lastSeen, withSeconds = Prefs.showSeconds)
                val timeOffline = getTime(event.time, withSeconds = Prefs.showSeconds)
                tvTimeLastAction.text = time
                tvTimeOffline.text = timeOffline

                vPastLineLastAction.paint(Munch.color.color)
                ivDotLastAction.paint(Munch.color.color)

                vPastLineOffline.paint(Munch.color.color50)
                ivDotOffline.paint(Munch.color.color50)
                vFutureLineLastAction.paint(Munch.color.color50)
            }
        }
    }
}