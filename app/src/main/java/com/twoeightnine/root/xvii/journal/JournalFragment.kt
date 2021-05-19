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

package com.twoeightnine.root.xvii.journal

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.journal.message.JournalMessageBottomSheet
import com.twoeightnine.root.xvii.journal.message.model.MessageInfo
import com.twoeightnine.root.xvii.journal.online.JournalOnlineBottomSheet
import com.twoeightnine.root.xvii.journal.online.model.OnlineInfo
import com.twoeightnine.root.xvii.utils.AppBarLifter
import com.twoeightnine.root.xvii.utils.contextpopup.ContextPopupItem
import com.twoeightnine.root.xvii.utils.contextpopup.createContextPopup
import global.msnthrp.xvii.core.journal.model.JournalEvent
import global.msnthrp.xvii.core.journal.model.JournalEventWithPeer
import global.msnthrp.xvii.core.journal.model.JournalFilter
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import kotlinx.android.synthetic.main.fragment_journal.*

class JournalFragment : BaseFragment() {

    private val viewModel by viewModels<JournalViewModel>()

    private val adapter by lazy {
        JournalAdapter(requireContext(), ::onClick)
    }

    override fun getLayoutId(): Int = R.layout.fragment_journal

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.loadEvents()

        viewModel.events.observe(::onEventsLoaded)
        viewModel.onlineEvents.observe(::openOnlineBottomSheet)
        viewModel.messageEvents.observe(::openMessageBottomSheet)
    }

    override fun getMenu(): Int = R.menu.menu_journal

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_filter -> {
                createContextPopup(requireContext(), listOf(
                        ContextPopupItem(0, R.string.journal_filter_all) {
                            viewModel.loadEvents(JournalFilter.ALL)
                        },
                        ContextPopupItem(0, R.string.journal_filter_deleted_messages) {
                            viewModel.loadEvents(JournalFilter.DELETED_MESSAGES)
                        },
                        ContextPopupItem(0, R.string.journal_filter_edited_messages) {
                            viewModel.loadEvents(JournalFilter.EDITED_MESSAGES)
                        },
                        ContextPopupItem(0, R.string.journal_filter_statuses) {
                            viewModel.loadEvents(JournalFilter.STATUSES)
                        }
                )).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun onEventsLoaded(events: List<JournalEventWithPeer>) {
        adapter.update(events)
        rvEvents.scrollToPosition(events.size - 1)
    }

    private fun initRecyclerView() {
        rvEvents.layoutManager = LinearLayoutManager(requireContext())
                .apply { stackFromEnd = true }
        rvEvents.adapter = adapter
        rvEvents.addOnScrollListener(AppBarLifter(xviiToolbar))
        tvDisclaimer.post {
            rvEvents.updatePadding(top = tvDisclaimer.height)
        }
        rvEvents.applyBottomInsetPadding()
    }

    private fun onClick(event: JournalEventWithPeer) {
        when (event.journalEvent) {
            is JournalEvent.StatusJE -> viewModel.loadOnlineEvents(event)
            is JournalEvent.MessageJE -> viewModel.loadMessageEvents(event)
            else -> {}
        }
    }

    private fun openOnlineBottomSheet(onlineInfo: OnlineInfo) {
        JournalOnlineBottomSheet
                .newInstance(onlineInfo)
                .show(childFragmentManager)
    }

    private fun openMessageBottomSheet(messageInfo: MessageInfo) {
        JournalMessageBottomSheet
                .newInstance(messageInfo)
                .show(childFragmentManager)
    }
}