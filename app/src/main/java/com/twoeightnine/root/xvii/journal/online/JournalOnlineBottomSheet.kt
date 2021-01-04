package com.twoeightnine.root.xvii.journal.online

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseBottomSheet
import com.twoeightnine.root.xvii.journal.online.model.OnlineEvent
import com.twoeightnine.root.xvii.managers.Prefs
import global.msnthrp.xvii.uikit.extensions.lowerIf
import kotlinx.android.synthetic.main.fragment_journal_online.*

class JournalOnlineBottomSheet private constructor(): BaseBottomSheet() {

    private val name by lazy {
        arguments?.getString(ARG_NAME)
    }
    private val events by lazy {
        arguments?.getParcelableArrayList<OnlineEvent>(ARG_EVENTS) ?: listOf()
    }
    private val adapter by lazy {
        OnlineEventAdapter(requireContext())
    }

    override fun getLayout(): Int = R.layout.fragment_journal_online

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvName.text = name
        tvName.lowerIf(Prefs.lowerTexts)

        rvEvents.layoutManager = LinearLayoutManager(requireContext())
        rvEvents.adapter = adapter
        adapter.update(events)
    }

    companion object {

        private const val ARG_NAME = "name"
        private const val ARG_EVENTS = "events"

        fun newInstance(name: String, events: List<OnlineEvent>) = JournalOnlineBottomSheet().apply {
            arguments = bundleOf(
                    ARG_NAME to name,
                    ARG_EVENTS to events
            )
        }
    }
}