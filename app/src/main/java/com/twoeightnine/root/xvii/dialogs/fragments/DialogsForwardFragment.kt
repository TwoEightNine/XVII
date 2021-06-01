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

package com.twoeightnine.root.xvii.dialogs.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import global.msnthrp.xvii.data.dialogs.Dialog
import kotlinx.android.synthetic.main.fragment_poll.*

class DialogsForwardFragment : DialogsFragment() {

    private val forwarded by lazy { arguments?.getString(ARG_FORWARDED) }
    private val shareText by lazy { arguments?.getString(ARG_SHARE_TEXT) }
    private val shareImages by lazy { arguments?.getStringArrayList(ARG_SHARE_IMAGE) ?: emptyList() }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        xviiToolbar.title = getString(R.string.choose_dialog)
        xviiToolbar.showLogo = false
    }

    override fun onClick(dialog: Dialog) {
        ChatActivity.launch(context, dialog, forwarded, shareText, shareImages)
        activity?.finish()
    }

    override fun onLongClick(dialog: Dialog) {}

    companion object {
        const val ARG_FORWARDED = "forwarded"
        const val ARG_SHARE_TEXT = "shareText"
        const val ARG_SHARE_IMAGE = "shareImage"

        fun createArgs(forwarded: String? = null, shareText: String? = null, shareImage: List<String> = emptyList()): Bundle {
            return Bundle().apply {
                if (!forwarded.isNullOrEmpty()) {
                    putString(ARG_FORWARDED, forwarded)
                }
                if (!shareText.isNullOrEmpty()) {
                    putString(ARG_SHARE_TEXT, shareText)
                }
                if (shareImage.isNotEmpty()) {
                    putStringArrayList(ARG_SHARE_IMAGE, ArrayList(shareImage))
                }
            }
        }

        fun newInstance(forwarded: String? = null, shareText: String? = null, shareImage: String? = null): DialogsForwardFragment {
            val fragment = DialogsForwardFragment()
            fragment.arguments = Bundle().apply {
                if (!forwarded.isNullOrEmpty()) {
                    putString(ARG_FORWARDED, forwarded)
                }
                if (!shareText.isNullOrEmpty()) {
                    putString(ARG_SHARE_TEXT, shareText)
                }
                if (!shareImage.isNullOrEmpty()) {
                    putString(ARG_SHARE_IMAGE, shareImage)
                }
            }
            return fragment
        }
    }
}