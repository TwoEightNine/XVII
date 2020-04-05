package com.twoeightnine.root.xvii.search

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerActivity
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.main.InsetViewModel
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.*
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

    private val insetViewModel by lazy {
        ViewModelProviders.of(activity ?: return@lazy null)[InsetViewModel::class.java]
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        insetViewModel?.topInset?.observe(viewLifecycleOwner, Observer { top ->
            rlSearch.setTopPadding(top, context?.resources?.getDimensionPixelSize(R.dimen.toolbar_height) ?: 0)
        })
        insetViewModel?.bottomInset?.observe(viewLifecycleOwner, Observer { bottom ->
            val bottomNavHeight = context?.resources?.getDimensionPixelSize(R.dimen.bottom_navigation_height) ?: 0
            rvSearch.setBottomPadding(bottom + bottomNavHeight)
        })
    }

    private fun updateResults(data: Wrapper<ArrayList<Dialog>>) {
        if (data.data != null) {
            adapter.update(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun onClick(dialog: Dialog) {
        ChatOwnerActivity.launch(context, dialog.peerId)
    }

    private fun initRecycler() {
        rvSearch.layoutManager = LinearLayoutManager(context)
        rvSearch.adapter = adapter
        rvSearch.setOnTouchListener { _, _ ->
            activity?.let { hideKeyboard(it) }
            false
        }
        adapter.emptyView = llEmptyView

        ViewCompat.setOnApplyWindowInsetsListener(rvSearch) { view, insets ->
            val bottomNavBarHeight = context?.resources?.getDimensionPixelSize(R.dimen.bottom_navigation_height) ?: 0
            view.setPadding(0, 0, 0, insets.systemWindowInsetBottom + bottomNavBarHeight)
            insets
        }
    }

    companion object {

        fun newInstance() = SearchFragment()
    }
}