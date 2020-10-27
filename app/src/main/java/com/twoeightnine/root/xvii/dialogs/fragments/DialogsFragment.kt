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
import com.twoeightnine.root.xvii.main.InsetViewModel
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.utils.contextpopup.ContextPopupItem
import com.twoeightnine.root.xvii.utils.contextpopup.createContextPopup
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
import kotlinx.android.synthetic.main.fragment_dialogs.*
import javax.inject.Inject


open class DialogsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: DialogsViewModel.Factory
    private lateinit var viewModel: DialogsViewModel

    private val adapter by lazy {
        DialogsAdapter(requireContext(), ::loadMore, ::onClick, ::onLongClick)
    }

    private val insetViewModel by lazy {
        ViewModelProviders.of(activity ?: return@lazy null)[InsetViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.fragment_dialogs

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
//        toolbar.hide()

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
        viewModel.getTypingPeerIds().observe(viewLifecycleOwner) { adapter.typingPeerIds = it }
        viewModel.loadDialogs()
        adapter.startLoading()

//        insetViewModel?.topInset?.observe(viewLifecycleOwner) { top ->
//            adapter.firstItemPadding = top
//        }
        insetViewModel?.bottomInset?.observe(viewLifecycleOwner) { bottom ->
            val bottomNavHeight = context?.resources?.getDimensionPixelSize(R.dimen.bottom_navigation_height) ?: 0
            rvDialogs.setBottomPadding(bottom + bottomNavHeight)
        }
    }

    private fun initRecycler() {
        rvDialogs.layoutManager = LinearLayoutManager(context)
        rvDialogs.adapter = adapter
        rvDialogs.addOnScrollListener(AppBarLifter(xviiToolbar))
    }

    private fun updateDialogs(data: Wrapper<ArrayList<Dialog>>) {
        swipeRefresh.isRefreshing = false
        progressBar.hide()
        if (data.data != null) {
            val d = arrayListOf<Dialog>().apply {
                addAll(data.data)
                addAll(data.data)
            }
            adapter.update(d)
//            adapter.update(FakeData.dialogs)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun loadMore(offset: Int) {
        viewModel.loadDialogs(offset)
    }

    protected open fun onClick(dialog: Dialog) {
//        startFragment<DialogsFragment>()
        ChatActivity.launch(context, dialog)
    }

    protected open fun onLongClick(dialog: Dialog) {
        val items = arrayListOf(
                ContextPopupItem(
                        if (dialog.isPinned) R.drawable.ic_pinned_crossed else R.drawable.ic_pinned,
                        if (dialog.isPinned) R.string.unpin else R.string.pin
                ) { viewModel.pinDialog(dialog) },
                ContextPopupItem(R.drawable.ic_eye, R.string.mark_as_read) {
                    viewModel.readDialog(dialog)
                },
                ContextPopupItem(R.drawable.ic_delete_popup, R.string.delete) {
                    showDeleteDialog(context, getString(R.string.this_dialog)) {
                        viewModel.deleteDialog(dialog)
                    }
                },
                ContextPopupItem(R.drawable.ic_alias, R.string.alias) {
                    TextInputAlertDialog(
                            requireContext(),
                            dialog.title,
                            dialog.alias ?: dialog.title
                    ) { newAlias ->
                        viewModel.addAlias(dialog, newAlias)
                    }.show()
                },
                ContextPopupItem(R.drawable.ic_link, R.string.add_shortcut) {
                    createShortcut(context, dialog)
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