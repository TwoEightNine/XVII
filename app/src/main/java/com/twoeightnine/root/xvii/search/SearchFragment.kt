package com.twoeightnine.root.xvii.search

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.ChatActivity
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.hideKeyboard
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.utils.stylizeAll
import com.twoeightnine.root.xvii.utils.subscribeSearch
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.view_search.*
import javax.inject.Inject

class SearchFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: SearchViewModel.Factory
    private lateinit var viewModel: SearchViewModel

    private val adapter by lazy {
        SearchAdapter(contextOrThrow, ::onClick)
    }

    override fun getLayoutId() = R.layout.fragment_search

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[SearchViewModel::class.java]
        viewModel.getResult().observe(this, Observer { updateResults(it) })

        etSearch.subscribeSearch(true, viewModel::search)
        ivDelete.setOnClickListener { etSearch.setText("") }
        llEmptyView.stylizeAll()
    }

    private fun updateResults(data: Wrapper<ArrayList<Dialog>>) {
        if (data.data != null) {
            adapter.update(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun onClick(dialog: Dialog) {
        ChatActivity.launch(context, dialog)
    }

    private fun initRecycler() {
        rvSearch.layoutManager = LinearLayoutManager(context)
        rvSearch.adapter = adapter
        rvSearch.setOnTouchListener { _, _ ->
            activity?.let { hideKeyboard(it) }
            false
        }
        adapter.emptyView = llEmptyView
    }

    companion object {

        fun newInstance() = SearchFragment()
    }
}