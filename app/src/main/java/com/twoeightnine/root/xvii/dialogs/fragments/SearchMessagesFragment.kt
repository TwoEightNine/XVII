package com.twoeightnine.root.xvii.dialogs.fragments

import android.os.CountDownTimer
import android.view.View
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.fragments.ChatFragment
import com.twoeightnine.root.xvii.dialogs.adapters.SearchMessagesAdapter
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.mvp.presenter.SearchMessagesFragmentPresenter
import com.twoeightnine.root.xvii.mvp.view.SearchMessagesFragmentView
import com.twoeightnine.root.xvii.utils.hideKeyboard
import com.twoeightnine.root.xvii.utils.showCommon
import kotlinx.android.synthetic.main.fragment_dialogs.*
import javax.inject.Inject

class SearchMessagesFragment: BaseOldFragment(), SearchMessagesFragmentView {

    var timer: CountDownTimer? = null

    companion object {
        fun newInstance(isForwarded: Boolean = false): DialogsFragment {
            val frag = DialogsFragment()
            frag.isForwarded = isForwarded
            return frag
        }
    }

    override fun getLayout() = R.layout.fragment_dialogs

    @Inject
    lateinit var presenter: SearchMessagesFragmentPresenter
    lateinit var adapter: SearchMessagesAdapter

    override fun bindViews(view: View) {
        initAdapter()
        initRefresh()
        initSearchView()
        App.appComponent?.inject(this)
        presenter.view = this
    }

    private fun initRefresh() {
        swipeRefresh.isEnabled = false
    }

    fun initAdapter() {
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        adapter = SearchMessagesAdapter(safeActivity, { loadMore(it) }, { onClick(it) }, { onLongClick(it) })
        recyclerView.adapter = adapter
    }

    private fun initSearchView() {
        searchView.setBackgroundColor(resources.getColor(R.color.popup))
        searchView.setTextColor(resources.getColor(R.color.main_text))
        searchView.setHintTextColor(resources.getColor(R.color.other_text))
        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                presenter.searchDialogs(query, withClear = true)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (timer != null) {
                    timer!!.cancel()
                    timer = null
                }
                timer = object : CountDownTimer(300L, 300L) {
                    override fun onTick(l: Long) {}

                    override fun onFinish() {
                        onQueryTextSubmit(newText)
                    }
                }.start()
                return true
            }
        })
        searchView.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewClosed() {
                rootActivity.onBackPressed()
            }

            override fun onSearchViewShown() {}
        })
        searchView.post {
            searchView.showSearch()
            searchView.showKeyboard(searchView)
        }
    }

    override fun onNew(view: View) {
        presenter.searchDialogs()
    }

    override fun onRecovered(view: View) {
        adapter.stopLoading(presenter.getSavedSearch())
    }

    fun loadMore(offset: Int) {
        presenter.searchDialogs(offset = offset)
    }

    private fun onLongClick(position: Int): Boolean {
        return true
    }

    override fun onDialogsLoaded(dialogs: MutableList<Message>) {
        adapter.stopLoading(dialogs)
    }

    override fun onDialogsClear() {
        adapter.clear()
    }

    fun onClick(position: Int) {
        hideKeyboard(safeActivity)
        rootActivity.loadFragment(ChatFragment.newInstance(adapter.items[position]))
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
}