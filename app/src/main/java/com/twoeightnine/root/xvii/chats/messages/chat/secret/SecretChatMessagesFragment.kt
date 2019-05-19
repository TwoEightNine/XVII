package com.twoeightnine.root.xvii.chats.messages.chat.secret

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.messages.chat.base.BaseChatMessagesFragment
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.FingerPrintAlertDialog
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
import kotlinx.android.synthetic.main.chat_input_panel.*

class SecretChatMessagesFragment : BaseChatMessagesFragment<SecretChatViewModel>() {

    override fun getViewModelClass() = SecretChatViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    override fun onEncryptedDocClicked(doc: Doc) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rlNoKeys.setVisible(viewModel.isKeyRequired())
        if (viewModel.isKeyRequired()) {
            showKeysDialog()
        }
        rlNoKeys.setOnClickListener {
            showKeysDialog()
        }

        viewModel.getKeysSet().observe(this, Observer {
            rlNoKeys.setVisible(!it)
            viewModel.loadMessages()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_secret_chat, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.menu_fingerprint -> {
            if (viewModel.isKeyRequired()) {
                showKeysDialog()
            } else {
                val fingerprint = viewModel.getFingerprint()
                val keyType = viewModel.getKeyType()
                context?.let {
                    FingerPrintAlertDialog(it, fingerprint, keyType).show()
                }
            }
            true
        }
        R.id.menu_keys -> {
            showKeysDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showKeysDialog() {
        getContextPopup(context ?: return, R.layout.popup_keys) {
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
        }.show()
    }

    private fun showKeyInputDialog() {
        TextInputAlertDialog(
                context ?: return,
                getString(R.string.user_key), "") { userKey ->
            if (userKey.isEmpty()) {
                showError(context, R.string.empty_user_key)
            } else {
                viewModel.setKey(userKey)
                showToast(activity, getString(R.string.key_set))
                rlNoKeys.hide()
                viewModel.loadMessages()
            }
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