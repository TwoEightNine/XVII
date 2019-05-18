package com.twoeightnine.root.xvii.dialogs.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.ChatActivity
import com.twoeightnine.root.xvii.dialogs.adapters.DialogsAdapter
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.dialogs.viewmodels.DialogsViewModel
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.*
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
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[DialogsViewModel::class.java]
        viewModel.getDialogs().observe(this, Observer { updateDialogs(it) })
        viewModel.loadDialogs()
        adapter.startLoading()

        progressBar.show()
        swipeRefresh.setOnRefreshListener {
            viewModel.loadDialogs()
            adapter.reset()
            adapter.startLoading()
        }
        progressBar.stylize()
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
        getContextPopup(context ?: return, R.layout.popup_dialogs) { view ->
            when (view.id) {

                R.id.llPin -> viewModel.pinDialog(dialog)
                R.id.llRead -> viewModel.readDialog(dialog)
                R.id.llMute -> viewModel.muteDialog(dialog)
                R.id.llDelete -> showDeleteDialog(context) {
                    viewModel.deleteDialog(dialog)
                }
                R.id.llAlias -> TextInputAlertDialog(
                        contextOrThrow,
                        dialog.title,
                        dialog.alias ?: dialog.title
                ) { newAlias ->
                    viewModel.addAlias(dialog, newAlias)
                }.show()
            }
        }.show()
//        createContextPopup(context ?: return, arrayListOf(
//                ContextPopupItem(R.drawable.ic_pin, R.string.pin) {
//                    showToast(context, "pinpin")
//                }
//        )).show()
    }

    companion object {
        fun newInstance() = DialogsFragment()
    }
}