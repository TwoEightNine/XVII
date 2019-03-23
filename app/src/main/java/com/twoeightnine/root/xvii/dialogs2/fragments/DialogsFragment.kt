package com.twoeightnine.root.xvii.dialogs2.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.fragments.ChatFragment
import com.twoeightnine.root.xvii.chats.fragments.ImportantFragment
import com.twoeightnine.root.xvii.dialogs.fragments.SearchMessagesFragment
import com.twoeightnine.root.xvii.dialogs2.adapters.DialogsAdapter
import com.twoeightnine.root.xvii.dialogs2.models.Dialog
import com.twoeightnine.root.xvii.dialogs2.viewmodels.DialogsViewModel
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.getContextPopup
import com.twoeightnine.root.xvii.utils.showDeleteDialog
import com.twoeightnine.root.xvii.utils.showError
import kotlinx.android.synthetic.main.fragment_dialogs_new.*
import kotlinx.android.synthetic.main.toolbar.*
import javax.inject.Inject

open class DialogsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: DialogsViewModel.Factory
    private lateinit var viewModel: DialogsViewModel

    private val adapter by lazy {
        DialogsAdapter(contextOrThrow, ::loadMore, ::onClick, ::onLongClick)
    }

    override fun getLayoutId() = R.layout.fragment_dialogs_new

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.dialogs))
        Style.forToolbar(toolbar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[DialogsViewModel::class.java]
        viewModel.getDialogs().observe(this, Observer { updateDialogs(it) })
        viewModel.loadDialogs()
        adapter.startLoading()

        swipeRefresh.isRefreshing = true
        swipeRefresh.setOnRefreshListener {
            viewModel.loadDialogs()
            adapter.loadAgain()
        }
    }

    private fun initRecycler() {
        rvDialogs.layoutManager = LinearLayoutManager(context)
        rvDialogs.adapter = adapter
    }

    private fun updateDialogs(data: Wrapper<ArrayList<Dialog>>) {
        swipeRefresh.isRefreshing = false
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
        val message = Message(
                0, 0, dialog.peerId, 0, 0, dialog.title, ""
        )
        if (dialog.peerId > 2000000000) {
            message.chatId = dialog.peerId - 2000000000
        }
        message.online = if (dialog.isOnline) 1 else 0
        rootActivity?.loadFragment(ChatFragment.newInstance(message))
    }

    protected open fun onLongClick(dialog: Dialog) {
        getContextPopup(context ?: return, R.layout.popup_dialogs) { view ->
            when (view.id) {

                R.id.llDelete -> showDeleteDialog(context) {
                    viewModel.deleteDialog(dialog)
                }
                R.id.llRead -> viewModel.readDialog(dialog)
                R.id.llMute -> viewModel.muteDialog(dialog)
            }
        }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.dialog_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_search_users -> {
                rootActivity?.loadFragment(SearchMessagesFragment())
                true
            }
            R.id.important_menu -> {
                rootActivity?.loadFragment(ImportantFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun newInstance() = DialogsFragment()
    }
}