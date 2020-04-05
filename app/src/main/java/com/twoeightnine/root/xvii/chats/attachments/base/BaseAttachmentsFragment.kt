package com.twoeightnine.root.xvii.chats.attachments.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.main.InsetViewModel
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.fragment_attachments.*
import javax.inject.Inject

abstract class BaseAttachmentsFragment<T : Any> : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseAttachmentsViewModel.Factory
    protected lateinit var viewModel: BaseAttachmentsViewModel<T>

    private val insetViewModel by lazy {
        ViewModelProviders.of(activity ?: return@lazy null)[InsetViewModel::class.java]
    }

    private val peerId by lazy { arguments?.getInt(ARG_PEER_ID) ?: 0 }

    abstract val adapter: BaseAttachmentsAdapter<T, out BaseAttachmentsAdapter.BaseAttachmentViewHolder<T>>

    abstract fun getLayoutManager(): RecyclerView.LayoutManager

    abstract fun getViewModelClass(): Class<out BaseAttachmentsViewModel<T>>

    abstract fun inject()

    override fun getLayoutId() = R.layout.fragment_attachments

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inject()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[getViewModelClass()]
        viewModel.peerId = peerId
        initRecycler()

        viewModel.getAttachments().observe(this, Observer { updateList(it) })
        viewModel.loadAttachments()
        adapter.startLoading()

        progressBar.show()
        swipeRefresh.setOnRefreshListener {
            viewModel.reset()
            viewModel.loadAttachments()
            adapter.reset()
            adapter.startLoading()
        }
        progressBar.stylize()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        insetViewModel?.bottomInset?.observe(viewLifecycleOwner, Observer { bottom ->
            rvAttachments.setBottomPadding(bottom)
        })
    }

    private fun updateList(data: Wrapper<ArrayList<T>>) {
        swipeRefresh.isRefreshing = false
        progressBar.hide()
        if (data.data != null) {
            adapter.update(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    fun loadMore(offset: Int) {
        viewModel.loadAttachments()
    }

    private fun initRecycler() {
        rvAttachments.layoutManager = getLayoutManager()
        rvAttachments.adapter = adapter
    }

    companion object {
        const val ARG_PEER_ID = "peerId"
    }
}