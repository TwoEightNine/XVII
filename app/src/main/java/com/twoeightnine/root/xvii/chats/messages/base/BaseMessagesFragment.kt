package com.twoeightnine.root.xvii.chats.messages.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.attachments.AttachmentsInflater
import com.twoeightnine.root.xvii.chats.messages.Interaction
import com.twoeightnine.root.xvii.dialogs.activities.DialogsForwardActivity
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.applyCompletableSchedulers
import com.twoeightnine.root.xvii.utils.getDate
import com.twoeightnine.root.xvii.utils.showError
import global.msnthrp.xvii.uikit.extensions.*
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.view_chat_multiselect.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class BaseMessagesFragment<VM : BaseMessagesViewModel> : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseMessagesViewModel.Factory
    protected lateinit var viewModel: VM

    protected val adapter by lazy {
        MessagesAdapter(
                requireContext(),
                ::loadMore,
                getAdapterCallback(),
                getAttachmentsCallback(),
                getAdapterSettings()
        )
    }

    abstract fun getViewModelClass(): Class<VM>

    abstract fun inject()

    abstract fun getAdapterCallback(): MessagesAdapter.Callback

    abstract fun getAttachmentsCallback(): AttachmentsInflater.Callback

    abstract fun getAdapterSettings(): MessagesAdapter.Settings

    protected open fun prepareViewModel() {}

    /**
     * handle 'scrolled to bottom' events
     */
    protected open fun onScrolled(isAtBottom: Boolean) {}

    override fun getLayoutId() = R.layout.fragment_chat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        inject()
        initRecyclerView()
        initMultiAction()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[getViewModelClass()]
        prepareViewModel()
        adapter.startLoading()

        progressBar.show()
        xviiToolbar.isLifted = true
        swipeContainer.setOnRefreshListener {
            loadMore(0)
            adapter.reset()
            adapter.startLoading()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getInteraction().observe(viewLifecycleOwner, ::updateMessages2)
        viewModel.loadMessages()
    }

    protected fun getSelectedMessageIds() = adapter.multiSelect
            .joinToString(separator = ",", transform = { it.id.toString() })

    private fun updateMessages2(data: Wrapper<Interaction>) {
        swipeContainer.isRefreshing = false
        progressBar.hide()
        if (data.data == null) {
            showError(context, data.error)
        }
        val interaction = data.data ?: return

        try {
            when (interaction.type) {
                Interaction.Type.CLEAR -> {
                    adapter.clear()
                }
                Interaction.Type.ADD -> {
                    val firstLoad = adapter.isEmpty
                    val isAtEnd = adapter.isAtBottom(rvChatList.layoutManager as? LinearLayoutManager)
                    adapter.addAll(interaction.messages.toMutableList(), interaction.position)
                    adapter.stopLoading(interaction.messages.isEmpty())
                    when {
                        firstLoad -> {
                            var unreadPos = adapter.itemCount - 1 // default last item
                            for (index in interaction.messages.indices) {
                                val message = interaction.messages[index]
                                if (!message.read && !message.isOut()) {
                                    unreadPos = index
                                    break
                                }
                            }
                            rvChatList.scrollToPosition(unreadPos)
                        }
                        isAtEnd -> {
                            rvChatList.scrollToPosition(adapter.itemCount - 1)
                        }
                    }
                }
                Interaction.Type.UPDATE -> {
                    adapter.update(interaction.messages, interaction.position)
                }
                Interaction.Type.REMOVE -> {
                    adapter.removeAt(interaction.position)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            adapter.update(viewModel.getStoredMessages())
            rvChatList.scrollToPosition(adapter.itemCount - 1)
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

        rvChatList.addOnScrollListener(RecyclerDateScroller())
        adapter.multiSelectListener = ::onMultiSelectChanged

        fabHasMore.setOnClickListener { rvChatList.scrollToPosition(adapter.itemCount - 1) }
        rvChatList.addOnScrollListener(ListScrollListener())
    }

    private fun onMultiSelectChanged(selectedCount: Int) {
        rlMultiAction.setVisible(selectedCount > 0)
        if (selectedCount == 0 && adapter.multiSelectMode) {
            adapter.multiSelectMode = false
        }
        tvSelectedCount.text = context?.resources
                ?.getQuantityString(R.plurals.messages, selectedCount, selectedCount)
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
                onScrolled(isAtBottom = false)
            } else if (fabHasMore.visibility != View.INVISIBLE
                    && adapter.lastVisiblePosition(rvChatList.layoutManager) == adapter.itemCount - 1) {
                fabHasMore.hide()
                onScrolled(isAtBottom = true)
            }
        }
    }

    private inner class RecyclerDateScroller : RecyclerView.OnScrollListener() {

        private var lastHandledTopPosition = -1
        private var lastHandledBottomPosition = -1
        private var disposable: Disposable? = null

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val adapterTopPosition = (recyclerView.layoutManager as? LinearLayoutManager)
                    ?.findFirstVisibleItemPosition() ?: -1
            val adapterBottomPosition = (recyclerView.layoutManager as? LinearLayoutManager)
                    ?.findLastVisibleItemPosition() ?: -1
            if (adapterTopPosition != lastHandledTopPosition
                    && adapterTopPosition != -1) {
                val message = adapter.items.getOrNull(adapterTopPosition) ?: return
                if (message.date == 0) return

                val uiDate = getDate(message.date)
                showDate(uiDate)
                lastHandledTopPosition = adapterTopPosition
                lastHandledBottomPosition = adapterBottomPosition

                disposable?.dispose()
                disposable = Completable.timer(2L, TimeUnit.SECONDS)
                        .compose(applyCompletableSchedulers())
                        .subscribe {
                            hideDate()
                        }

            }
        }

        private fun showDate(date: String) {
            if (!tvDatePopup.isShown) {
                tvDatePopup.fadeIn(200L)
                tvDatePopup.show()
            }
            tvDatePopup.text = date
        }

        private fun hideDate() {
            tvDatePopup?.fadeOut(200L) {
                tvDatePopup?.hide()
            }
        }
    }
}