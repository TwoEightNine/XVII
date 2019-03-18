package com.twoeightnine.root.xvii.dialogs.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.fragments.ChatFragment
import com.twoeightnine.root.xvii.dialogs.adapters.DialogsAdapter
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.mvp.presenter.DialogFwFragmentPresenter
import com.twoeightnine.root.xvii.mvp.view.DialogFwFragmentView
import com.twoeightnine.root.xvii.utils.showCommon
import kotlinx.android.synthetic.main.fragment_dialogs.*
import javax.inject.Inject

class DialogFwFragment : BaseOldFragment(), DialogFwFragmentView {

    companion object {
        fun newInstance(fwdMessages: String): DialogFwFragment {
            val frag = DialogFwFragment()
            frag.fwdMessages = fwdMessages
            return frag
        }
    }

    var fwdMessages = ""

    @Inject
    lateinit var presenter: DialogFwFragmentPresenter
    lateinit var adapter: DialogsAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.choose_dialog))
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    override fun getLayout() = R.layout.fragment_dialogs

    override fun bindViews(view: View) {
        initAdapter()
        initRefresh()
        App.appComponent?.inject(this)
        presenter.view = this
    }

    override fun onNew(view: View) {
        presenter.loadDialogs()
    }

    fun initRefresh() {
        swipeRefresh.isEnabled = false
    }

    fun initAdapter() {
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        adapter = DialogsAdapter(safeActivity, { loadMore(it) }, { onClick(it) }, { true })
        adapter.trier = { loadMore(adapter.itemCount) }
        recyclerView.adapter = adapter
    }

    fun loadMore(offset: Int) {
        presenter.loadDialogs(offset)
    }

    fun onClick(position: Int) {
        rootActivity.onBackPressed()
        rootActivity.loadFragment(ChatFragment.newInstance(adapter.items[position], fwdMessages))
    }

    override fun showLoading() {
        adapter.startLoading()
    }

    override fun hideLoading() {
//        adaptr.stopLoading()
    }

    override fun showError(error: String) {
        adapter.setErrorLoading()
        showCommon(activity, error)
    }

    override fun onDialogsLoaded(dialogs: MutableList<Message>) {
        adapter.stopLoading(dialogs)
    }
}