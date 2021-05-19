/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.features.datausage

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.network.datausage.DataUsageEvent
import com.twoeightnine.root.xvii.utils.getSize
import com.twoeightnine.root.xvii.utils.getTime
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import kotlinx.android.synthetic.main.item_data_usage_event.view.*

class DataUsageAdapter(context: Context) : BaseAdapter<DataUsageEvent, DataUsageAdapter.DataUsageViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = DataUsageViewHolder(inflater.inflate(R.layout.item_data_usage_event, null))

    override fun onBindViewHolder(holder: DataUsageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class DataUsageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(dataUsageEvent: DataUsageEvent) {
            with(itemView) {
                var name = dataUsageEvent.name
                name = name.replace("api.vk.com/method/", "")
                tvName.text = name
                tvTime.text = getTime(dataUsageEvent.timeStamp, withSeconds = true)
                tvOutgoing.text = getSize(resources, dataUsageEvent.requestSize.toInt())
                tvIncoming.text = getSize(resources, dataUsageEvent.responseSize.toInt())
            }
        }
    }
}