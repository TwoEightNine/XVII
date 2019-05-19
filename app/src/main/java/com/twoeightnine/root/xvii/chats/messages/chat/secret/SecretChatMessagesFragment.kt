package com.twoeightnine.root.xvii.chats.messages.chat.secret

import android.os.Bundle
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.messages.chat.base.BaseChatMessagesFragment
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.TextInputAlertDialog

class SecretChatMessagesFragment : BaseChatMessagesFragment<SecretChatViewModel>() {

    override fun getViewModelClass() = SecretChatViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.isKeyRequired()) {
            showKeysDialog()
        }
    }

    private fun showKeysDialog() {
        val popup = getContextPopup(context ?: return, R.layout.popup_keys) {
            when (it.id) {
                R.id.llRandomKey -> {
                    if (peerId.matchesUserId()) {
                        showAlert(context, getString(R.string.generation_dh_hint)) {
                            viewModel.startExchange()
                        }
                    } else {
                        showError(activity, R.string.no_exchg_in_chats)
                    }
                }
                R.id.llUserKey -> showKeyInputDialog()
            }
        }
        popup.setCancelable(false)
        popup.show()
    }

    private fun showKeyInputDialog() {
        TextInputAlertDialog(
                context ?: return,
                getString(R.string.user_key), "") {
            viewModel.setKey(it)
            showToast(activity, getString(R.string.key_set))
        }.show()
    }

    companion object {

        fun newInstance(dialog: Dialog): SecretChatMessagesFragment {
            val fragment = SecretChatMessagesFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, dialog.peerId)
                putString(ARG_TITLE, dialog.alias ?: dialog.title)
                putString(ARG_PHOTO, dialog.photo)
            }
            return fragment
        }
    }
}