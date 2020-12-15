package com.twoeightnine.root.xvii.chats.messages.chat.secret

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.messages.chat.base.BaseChatMessagesFragment
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.utils.contextpopup.ContextPopupItem
import com.twoeightnine.root.xvii.utils.contextpopup.createContextPopup
import com.twoeightnine.root.xvii.utils.matchesUserId
import com.twoeightnine.root.xvii.utils.showAlert
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.utils.showToast
import com.twoeightnine.root.xvii.views.FingerPrintAlertDialog
import com.twoeightnine.root.xvii.views.LoadingDialog
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.setVisible
import global.msnthrp.xvii.uikit.extensions.show
import kotlinx.android.synthetic.main.chat_input_panel.*
import kotlinx.android.synthetic.main.fragment_chat.*

class SecretChatMessagesFragment : BaseChatMessagesFragment<SecretChatViewModel>() {

    override fun getViewModelClass() = SecretChatViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    override fun onEncryptedDocClicked(doc: Doc) {
        val dialogLoading = LoadingDialog(
                context ?: return,
                getString(R.string.decrypting_image)
        )
        dialogLoading.show()
        viewModel.decryptDoc(doc) { verified, path ->
            dialogLoading.dismiss()
            if (!path.isNullOrEmpty() && verified) {
                ImageViewerActivity.viewImage(context, "file://$path")
            } else {
                showError(context, R.string.invalid_file)
            }
        }
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
        ivKeyPattern.show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getKeysSet().observe(viewLifecycleOwner) {
            rlNoKeys.setVisible(!it)
            viewModel.loadMessages()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_secret_chat, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_fingerprint -> {
            if (viewModel.isKeyRequired()) {
                showKeysDialog()
            } else {
                val fingerprint = viewModel.getFingerprint()
                context?.let {
                    FingerPrintAlertDialog(it, fingerprint).show()
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
        val canMakeExchange = peerId.matchesUserId() && peerId != Session.uid
        if (canMakeExchange) {
            createContextPopup(context ?: return, arrayListOf(
                    ContextPopupItem(R.drawable.ic_edit_popup, R.string.user_key) {
                        showKeyInputDialog()
                    },
                    ContextPopupItem(R.drawable.ic_key_exchange, R.string.random_key) {
                        showAlert(context, getString(R.string.generation_dh_hint)) {
                            viewModel.startExchange()
                        }
                    })).show()
        } else {
            showKeyInputDialog()
        }
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
                putString(ARG_TITLE, dialog.aliasOrTitle)
                putString(ARG_PHOTO, dialog.photo)
            }
            return fragment
        }
    }
}