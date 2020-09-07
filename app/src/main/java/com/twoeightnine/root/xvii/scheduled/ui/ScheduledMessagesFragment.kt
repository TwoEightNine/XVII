package com.twoeightnine.root.xvii.scheduled.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.scheduled.core.ScheduledMessage
import com.twoeightnine.root.xvii.utils.setBottomInsetPadding
import com.twoeightnine.root.xvii.utils.showConfirm
import kotlinx.android.synthetic.main.fragment_scheduled_messages.*

class ScheduledMessagesFragment : BaseFragment() {

    private val viewModel by lazy {
        ViewModelProviders.of(this)[ScheduledMessagesViewModel::class.java]
    }
    private val adapter by lazy {
        ScheduledMessagesAdapter(contextOrThrow, ::onClicked)
    }

    override fun getLayoutId(): Int = R.layout.fragment_scheduled_messages

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvMessages.layoutManager = LinearLayoutManager(context)
        rvMessages.adapter = adapter
        rvMessages.setBottomInsetPadding()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTitle(getString(R.string.scheduled_messages_title))
        viewModel.scheduledMessages
                .observe(viewLifecycleOwner, Observer(this::onScheduledMessagesLoaded))
        viewModel.peersMap
                .observe(viewLifecycleOwner, Observer(adapter::updatePeersMap))
        viewModel.loadScheduledMessages()
    }

    private fun onScheduledMessagesLoaded(messages: List<ScheduledMessage>) {
        adapter.update(messages)
    }

    private fun onClicked(scheduledMessage: ScheduledMessage) {
        showConfirm(context, getString(R.string.scheduled_messages_cancel_prompt)) { yes ->
            if (yes) viewModel.cancelScheduledMessage(requireContext(), scheduledMessage)
        }
    }

    companion object {
        fun newInstance() = ScheduledMessagesFragment()
    }
}