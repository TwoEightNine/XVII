package com.twoeightnine.root.xvii.journal.message

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseBottomSheet
import com.twoeightnine.root.xvii.journal.message.model.MessageInfo
import com.twoeightnine.root.xvii.managers.Prefs
import global.msnthrp.xvii.uikit.extensions.lowerIf
import kotlinx.android.synthetic.main.fragment_journal_online.*

class JournalMessageBottomSheet private constructor(): BaseBottomSheet() {

    private val messageInfo by lazy {
        arguments?.getParcelable<MessageInfo>(ARG_DATA)
    }
    private val adapter by lazy {
        MessageEventAdapter(requireContext())
    }

    override fun getLayout(): Int = R.layout.fragment_journal_message

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvName.text = messageInfo?.fromName
        tvName.lowerIf(Prefs.lowerTexts)

        rvEvents.layoutManager = LinearLayoutManager(requireContext())
        rvEvents.adapter = adapter
        messageInfo?.events?.also(adapter::update)
    }

    companion object {

        private const val ARG_DATA = "data"

        fun newInstance(messageInfo: MessageInfo) = JournalMessageBottomSheet().apply {
            arguments = bundleOf(
                ARG_DATA to messageInfo
            )
        }
    }
}