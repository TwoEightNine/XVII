package com.twoeightnine.root.xvii.chats.attachments.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.showError
import kotlinx.android.synthetic.main.fragment_attachments.*
import javax.inject.Inject

abstract class BaseAttachFragment<T : Any> : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseAttachViewModel.Factory
    protected lateinit var viewModel: BaseAttachViewModel<T>

    abstract val adapter: BaseAttachmentsAdapter<T, out BaseAttachmentsAdapter.BaseAttachmentViewHolder<T>>

    abstract fun getLayoutManager(): RecyclerView.LayoutManager

    abstract fun getViewModelClass(): Class<out BaseAttachViewModel<T>>

    abstract fun inject()

    override fun getLayoutId() = R.layout.fragment_attachments

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inject()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[getViewModelClass()]
        initRecycler()

        viewModel.getAttach().observe(this, Observer { updateList(it) })
        viewModel.loadAttach()
        adapter.startLoading()

        swipeRefresh.isRefreshing = true
        swipeRefresh.setOnRefreshListener {
            viewModel.loadAttach()
            adapter.loadAgain()
        }
    }

    private fun updateList(data: Wrapper<ArrayList<T>>) {
        swipeRefresh.isRefreshing = false
        if (data.data != null) {
            adapter.update(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    fun loadMore(offset: Int) {
        viewModel.loadAttach(offset)
    }

    private fun initRecycler() {
        rvAttachments.layoutManager = getLayoutManager()
        rvAttachments.adapter = adapter
    }
}