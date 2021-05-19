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
                .apply { stackFromEnd = true }
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