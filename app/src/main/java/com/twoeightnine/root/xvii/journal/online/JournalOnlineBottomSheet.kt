package com.twoeightnine.root.xvii.journal.online

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseBottomSheet
import com.twoeightnine.root.xvii.journal.online.model.OnlineInfo
import com.twoeightnine.root.xvii.managers.Prefs
import global.msnthrp.xvii.uikit.extensions.lowerIf
import kotlinx.android.synthetic.main.fragment_journal_online.*

class JournalOnlineBottomSheet private constructor(): BaseBottomSheet() {

    private val onlineInfo by lazy {
        arguments?.getParcelable<OnlineInfo>(ARG_DATA)
    }
    private val adapter by lazy {
        OnlineEventAdapter(requireContext())
    }

    override fun getLayout(): Int = R.layout.fragment_journal_online

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvName.text = onlineInfo?.userName
        tvName.lowerIf(Prefs.lowerTexts)

        rvEvents.layoutManager = LinearLayoutManager(requireContext())
                .apply { stackFromEnd = true }
        rvEvents.adapter = adapter
        onlineInfo?.events?.also(adapter::update)
    }

    companion object {

        private const val ARG_DATA = "data"

        fun newInstance(onlineInfo: OnlineInfo) = JournalOnlineBottomSheet().apply {
            arguments = bundleOf(
                    ARG_DATA to onlineInfo
            )
        }
    }
}