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
import kotlinx.android.synthetic.main.item_online_event.view.*

class OnlineEventAdapter(context: Context) : BaseAdapter<OnlineEvent, OnlineEventAdapter.OnlineEventViewHolder>(context) {

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ) = OnlineEventViewHolder(inflater.inflate(R.layout.item_online_event, parent, false))

    override fun onBindViewHolder(holder: OnlineEventViewHolder, position: Int) {
        holder.bind(items[position], items.getOrNull(position - 1))
    }

    inner class OnlineEventViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(event: OnlineEvent, prevEvent: OnlineEvent?) {
            with(itemView) {
                val time = getTime(event.time, withSeconds = Prefs.showSeconds)
                val deviceName = when {
                    event.isOnline -> com.twoeightnine.root.xvii.background.longpoll.models.events
                            .OnlineEvent.getDeviceName(context.resources, event.deviceCode)
                    else -> ""
                }
                tvEvent.text = "$time $deviceName"

                ivDot.paint(Munch.color.color)
                vPastLine.paint(Munch.color.color)
                vFutureLine.paint(Munch.color.color)

                vPastLine.setVisible(!event.isOnline || event.isOnline && prevEvent?.isOnline == true)
                vFutureLine.setVisible(event.isOnline)
            }
        }
    }
}