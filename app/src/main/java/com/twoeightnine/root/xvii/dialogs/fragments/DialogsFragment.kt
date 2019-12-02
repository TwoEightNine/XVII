package com.twoeightnine.root.xvii.dialogs.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.messages.chat.secret.SecretChatActivity
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.dialogs.adapters.DialogsAdapter
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.dialogs.viewmodels.DialogsViewModel
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.utils.contextpopup.ContextPopupItem
import com.twoeightnine.root.xvii.utils.contextpopup.createContextPopup
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
import kotlinx.android.synthetic.main.fragment_dialogs.*
import kotlinx.android.synthetic.main.toolbar.*
import javax.inject.Inject


open class DialogsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: DialogsViewModel.Factory
    private lateinit var viewModel: DialogsViewModel

    private val adapter by lazy {
        DialogsAdapter(contextOrThrow, ::loadMore, ::onClick, ::onLongClick)
    }

    override fun getLayoutId() = R.layout.fragment_dialogs

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        toolbar.hide()


        progressBar.show()
        swipeRefresh.setOnRefreshListener {
            viewModel.loadDialogs()
            adapter.reset()
            adapter.startLoading()
        }
        progressBar.stylize()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[DialogsViewModel::class.java]
        viewModel.getDialogs().observe(viewLifecycleOwner, Observer(::updateDialogs))
        viewModel.getTypingPeerIds().observe(viewLifecycleOwner, Observer { adapter.typingPeerIds = it })
        viewModel.loadDialogs()
        adapter.startLoading()
    }

    private fun initRecycler() {
        rvDialogs.layoutManager = LinearLayoutManager(context)
        rvDialogs.adapter = adapter
    }

    private fun updateDialogs(data: Wrapper<ArrayList<Dialog>>) {
        swipeRefresh.isRefreshing = false
        progressBar.hide()
        if (data.data != null) {
            adapter.update(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun loadMore(offset: Int) {
        viewModel.loadDialogs(offset)
    }

    protected open fun onClick(dialog: Dialog) {
        ChatActivity.launch(context, dialog)
    }

    protected open fun onLongClick(dialog: Dialog) {
        val items = arrayListOf(
                ContextPopupItem(R.drawable.ic_pinned, if (dialog.isPinned) R.string.unpin else R.string.pin) {
                    viewModel.pinDialog(dialog)
                },
                ContextPopupItem(R.drawable.ic_eye, R.string.read) {
                    viewModel.readDialog(dialog)
                },
                ContextPopupItem(R.drawable.ic_delete_popup, R.string.delete) {
                    showDeleteDialog(context) {
                        viewModel.deleteDialog(dialog)
                    }
                },
                ContextPopupItem(R.drawable.ic_alias, R.string.alias) {
                    TextInputAlertDialog(
                            contextOrThrow,
                            dialog.title,
                            dialog.alias ?: dialog.title
                    ) { newAlias ->
                        viewModel.addAlias(dialog, newAlias)
                    }.show()
                },
                ContextPopupItem(R.drawable.ic_home, R.string.add_shortcut) {
                    createShortcut(context, dialog)
//                    loadBitmapIcon(dialog.photo, useSquare = true) { bitmap ->
//                        val back = createNotificationBackground(bitmap, debug = true)
//                        val file = File(context!!.cacheDir, "back${dialog.peerId}.png").absolutePath
//                        saveBmp(file, back.background)
//                        ImageViewerActivity.viewImage(context, "file://$file")
//                    }
                }
        )

        if (dialog.peerId.matchesUserId()) {
            items.add(ContextPopupItem(R.drawable.ic_start_secret_chat, R.string.encryption) {
                SecretChatActivity.launch(context, dialog)
            })
        }

        createContextPopup(context ?: return, items).show()
    }

    companion object {
        fun newInstance() = DialogsFragment()
    }
}