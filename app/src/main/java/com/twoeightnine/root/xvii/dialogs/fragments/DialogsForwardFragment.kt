package com.twoeightnine.root.xvii.dialogs.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.utils.show
import kotlinx.android.synthetic.main.toolbar.*

class DialogsForwardFragment : DialogsFragment() {

    private val forwarded by lazy { arguments?.getString(ARG_FORWARDED) }
    private val shareText by lazy { arguments?.getString(ARG_SHARE_TEXT) }
    private val shareImage by lazy { arguments?.getString(ARG_SHARE_IMAGE) }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.show()
        updateTitle(getString(R.string.choose_dialog))
    }

    override fun onClick(dialog: Dialog) {
        ChatActivity.launch(context, dialog, forwarded, shareText, shareImage)
        activity?.finish()
    }

    override fun onLongClick(dialog: Dialog) {}

    companion object {
        const val ARG_FORWARDED = "forwarded"
        const val ARG_SHARE_TEXT = "shareText"
        const val ARG_SHARE_IMAGE = "shareImage"

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