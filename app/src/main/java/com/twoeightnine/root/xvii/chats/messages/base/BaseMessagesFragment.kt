package com.twoeightnine.root.xvii.chats.messages.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.dialogs.activities.DialogsForwardActivity
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.hide
import com.twoeightnine.root.xvii.utils.setVisible
import com.twoeightnine.root.xvii.utils.show
import com.twoeightnine.root.xvii.utils.showError
import kotlinx.android.synthetic.main.fragment_chat.*
import javax.inject.Inject

abstract class BaseMessagesFragment<VM : BaseMessagesViewModel> : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseMessagesViewModel.Factory
    protected lateinit var viewModel: VM

    protected val adapter by lazy {
        MessagesAdapter(contextOrThrow, ::loadMore, getAdapterCallback(), getAdapterSettings())
    }

    abstract fun getViewModelClass(): Class<VM>

    abstract fun inject()

    abstract fun getAdapterCallback(): MessagesAdapter.Callback

    abstract fun getAdapterSettings(): MessagesAdapter.Settings

    protected open fun prepareViewModel() {}

    override fun getLayoutId() = R.layout.fragment_chat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        inject()
        initRecyclerView()
        initMultiAction()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[getViewModelClass()]
        prepareViewModel()
        viewModel.getMessages().observe(this, Observer { updateMessages(it) })
        viewModel.loadMessages()
        adapter.startLoading()

        progressBar.show()
        swipeContainer.setOnRefreshListener {
            loadMore(0)
            adapter.reset()
            adapter.startLoading()
        }
    }

    protected fun getSelectedMessageIds() = adapter.multiSelect
            .joinToString(separator = ",", transform = { it.id.toString() })

    private fun updateMessages(data: Wrapper<ArrayList<Message>>) {
        swipeContainer.isRefreshing = false
        progressBar.hide()
        if (data.data != null) {
            val lengthBefore = adapter.itemCount
            val diff = adapter.lastVisiblePosition(rvChatList.layoutManager)
            adapter.update(data.data.reversed())
            rvChatList.scrollToPosition(adapter.itemCount - lengthBefore + diff)
        } else {
            showError(context, data.error)
        }
    }

    private fun loadMore(offset: Int) {
        viewModel.loadMessages(offset)
    }

    private fun initRecyclerView() {
        rvChatList.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        rvChatList.adapter = adapter
        rvChatList.itemAnimator = null
        adapter.multiSelectListener = ::onMultiSelectChanged

        fabHasMore.setOnClickListener { rvChatList.scrollToPosition(adapter.itemCount - 1) }
        rvChatList.addOnScrollListener(ListScrollListener())
    }

    private fun onMultiSelectChanged(selectedCount: Int) {
        rlMultiAction.setVisible(selectedCount > 0)
//        tvSelectedCount.text = context?.resources
//                ?.getQuantityString(R.plurals.messages, selectedCount, selectedCount)
    }

    private fun initMultiAction() {
        ivCancelMulti.setOnClickListener {
            adapter.multiSelectMode = false
            rlMultiAction.hide()
        }
        ivForwardMulti.setOnClickListener {
            val messageIds = getSelectedMessageIds()
            adapter.multiSelectMode = false
            DialogsForwardActivity.launch(context, forwarded = messageIds)
        }
    }

    private inner class ListScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (fabHasMore.visibility != View.VISIBLE &&
                    adapter.lastVisiblePosition(rvChatList.layoutManager) != adapter.itemCount - 1) {
                fabHasMore.show()
            } else if (fabHasMore.visibility != View.INVISIBLE
                    && adapter.lastVisiblePosition(rvChatList.layoutManager) == adapter.itemCount - 1) {
                fabHasMore.hide()
            }
        }
    }
}