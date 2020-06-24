package com.twoeightnine.root.xvii.pin.fake.alarm

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.BaseAdapter
import com.twoeightnine.root.xvii.utils.secToTime
import kotlinx.android.synthetic.main.item_alarm.view.*

class AlarmAdapter(
        context: Context,
        private val onAllEnabled: () -> Unit
) : BaseAdapter<Alarm, AlarmAdapter.AlarmViewHolder>(context) {

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ) = AlarmViewHolder(inflater.inflate(R.layout.item_alarm, parent, false))

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(items[position])
    }

    private fun notifyEnabledChanged() {
        items.forEach { alarm ->
            if (!alarm.enabled) return
        }
        // every alarm is enabled here
        onAllEnabled()
    }

    inner class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(alarm: Alarm) {
            with(itemView) {
                tvTime.text = secToTime(alarm.time)
                swEnabled.isChecked = alarm.enabled
                cbEveryDay.isChecked = !alarm.onlyOnce

                swEnabled.setOnCheckedChangeListener { _, isChecked ->
                    alarm.enabled = isChecked
                    notifyEnabledChanged()
                }
            }
        }
    }
}