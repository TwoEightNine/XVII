package com.twoeightnine.root.xvii.journal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.journal.message.JournalMessageBottomSheet
import com.twoeightnine.root.xvii.journal.message.model.MessageInfo
import com.twoeightnine.root.xvii.journal.online.JournalOnlineBottomSheet
import com.twoeightnine.root.xvii.journal.online.model.OnlineInfo
import com.twoeightnine.root.xvii.utils.AppBarLifter
import global.msnthrp.xvii.core.journal.model.JournalEvent
import global.msnthrp.xvii.core.journal.model.JournalEventWithPeer
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

        viewModel.events.observe(adapter::update)
        viewModel.onlineEvents.observe(::openOnlineBottomSheet)
        viewModel.messageEvents.observe(::openMessageBottomSheet)
    }

    private fun initRecyclerView() {
        rvEvents.layoutManager = LinearLayoutManager(requireContext())
                .apply { stackFromEnd = true }
        rvEvents.adapter = adapter
        rvEvents.addOnScrollListener(AppBarLifter(xviiToolbar))
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