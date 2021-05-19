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

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.network.datausage.DataUsageInterceptor
import com.twoeightnine.root.xvii.utils.getSize
import kotlinx.android.synthetic.main.fragment_data_usage.*

class DataUsageFragment : BaseFragment() {

    private val adapter by lazy {
        DataUsageAdapter(requireContext())
    }

    override fun getLayoutId() = R.layout.fragment_data_usage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val events = DataUsageInterceptor.events
        rvUsage.layoutManager = LinearLayoutManager(context)
        rvUsage.adapter = adapter
        adapter.addAll(events)

        context?.resources?.also {
            tvOutgoing.text = getSize(it, events.map { it.requestSize }.sum().toInt())
            tvIncoming.text = getSize(it, events.map { it.responseSize }.sum().toInt())
        }
    }

    companion object {
        fun newInstance() = DataUsageFragment()
    }
}