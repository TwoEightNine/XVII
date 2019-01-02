package com.twoeightnine.root.xvii.chats.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.adapters.ChatAdapter
import com.twoeightnine.root.xvii.dialogs.fragments.DialogFwFragment
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.mvp.presenter.ImportantFragmentPresenter
import com.twoeightnine.root.xvii.mvp.view.ImportantFragmentView
import com.twoeightnine.root.xvii.profile.fragments.ProfileFragment
import com.twoeightnine.root.xvii.utils.*
import javax.inject.Inject

class ImportantFragment: BaseFragment(), ImportantFragmentView {

    @BindView(R.id.rvImportant)
    lateinit var rvImportant: RecyclerView

    @Inject
    lateinit var presenter: ImportantFragmentPresenter
    @Inject
    lateinit var apiUtils: ApiUtils

    lateinit var adapter: ChatAdapter

    override fun bindViews(view: View) {
        super.bindViews(view)
        ButterKnife.bind(this, view)
        App.appComponent?.inject(this)
        presenter.view = this
        initAdapter()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.important))
    }

    fun initAdapter() {
        rvImportant.layoutManager = LinearLayoutManager(activity)
        adapter = ChatAdapter(
                safeActivity,
                { presenter.loadHistory(it) },
                {}, ::onLongClick,
                { rootActivity.loadFragment(ProfileFragment.newInstance(it)) },
                {},
                { apiUtils.showPhoto(safeContext, it.photoId, it.accessKey) },
                { apiUtils.openVideo(safeContext, it) },
                true
        )
        val llm = LinearLayoutManager(activity)
        llm.stackFromEnd = true
        rvImportant.layoutManager = llm
        rvImportant.adapter = adapter
    }

    private fun onLongClick(position: Int): Boolean {
        if (position !in adapter.items.indices) return true

        val message = adapter.items[position]
        getContextPopup(safeActivity, R.layout.popup_important, {
            when (it.id) {
                R.id.llCopy -> copyToClip(message.body ?: "")
                R.id.llDelete -> showDeleteDialog(safeActivity, { presenter.deleteMessages(mutableListOf(message.id)) })
                R.id.llForward -> rootActivity.loadFragment(DialogFwFragment.newInstance("${message.id}"))
            }
        }).show()
        return true
    }

    override fun onNew(view: View) {
        presenter.loadHistory()
    }

    override fun onRecovered(view: View) {
        adapter.stopLoading(presenter.getSaved())
    }

    override fun getLayout() = R.layout.fragment_important

    override fun showLoading() {
        adapter.startLoading()
    }

    override fun hideLoading() {
//        adapter.stopLoading()
    }

    override fun showError(error: String) {
        adapter.setErrorLoading()
        showError(activity, error)
    }

    override fun onHistoryLoaded(history: MutableList<Message>) {
        adapter.stopLoading(history, true)
    }

    override fun onHistoryClear() {
        adapter.clear()
    }

    override fun onMessagesDeleted(mids: MutableList<Int>) {
        for (mid in mids) {
            for (pos in adapter.items.indices) {
                if (adapter.items[pos].id == mid) {
                    adapter.removeAt(pos)
                    break
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }
}