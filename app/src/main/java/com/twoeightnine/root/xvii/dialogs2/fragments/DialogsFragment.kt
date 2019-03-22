package com.twoeightnine.root.xvii.dialogs2.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.dialogs2.adapters.DialogsAdapter
import com.twoeightnine.root.xvii.dialogs2.models.Dialog
import com.twoeightnine.root.xvii.dialogs2.viewmodels.DialogsViewModel
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.showError
import kotlinx.android.synthetic.main.fragment_dialogs_new.*
import javax.inject.Inject

class DialogsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: DialogsViewModel.Factory
    private lateinit var viewModel: DialogsViewModel

    private val adapter by lazy {
        DialogsAdapter(contextOrThrow, ::onClick, ::onLongClick)
    }

    override fun getLayoutId() = R.layout.fragment_dialogs_new

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[DialogsViewModel::class.java]
        viewModel.getDialogs().observe(this, Observer { updateDialogs(it) })
        viewModel.loadDialogs()

        swipeRefresh.isRefreshing = true
        swipeRefresh.setOnRefreshListener {
            viewModel.loadDialogs()
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

    private fun onClick(dialog: Dialog) {

    }

    private fun onLongClick(dialog: Dialog) {

    }

    companion object {
        fun newInstance() = DialogsFragment()
    }
}