package com.twoeightnine.root.xvii.dialogs.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.fragments.ChatFragment
import com.twoeightnine.root.xvii.dialogs.adapters.DialogsAdapter
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.mvp.presenter.DialogFwFragmentPresenter
import com.twoeightnine.root.xvii.mvp.view.DialogFwFragmentView
import com.twoeightnine.root.xvii.utils.showCommon
import javax.inject.Inject

class DialogFwFragment : BaseFragment(), DialogFwFragmentView {

    @BindView(R.id.recyclerView)
    open lateinit var recyclerView: RecyclerView
    @BindView(R.id.swipeRefresh)
    open lateinit var swipeRefresh: SwipyRefreshLayout

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
        ButterKnife.bind(this, view)
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
        recyclerView.layoutManager = LinearLayoutManager(activity)
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