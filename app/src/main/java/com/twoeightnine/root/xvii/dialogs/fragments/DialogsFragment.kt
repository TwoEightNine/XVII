package com.twoeightnine.root.xvii.dialogs.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.longpoll.models.events.NewMessageEvent
import com.twoeightnine.root.xvii.chats.fragments.ChatFragment
import com.twoeightnine.root.xvii.chats.fragments.ImportantFragment
import com.twoeightnine.root.xvii.dialogs.adapters.DialogsAdapter
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.mvp.presenter.DialogsFragmentPresenter
import com.twoeightnine.root.xvii.mvp.view.DialogsFragmentView
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.fragment_dialogs.*
import java.util.*
import javax.inject.Inject

open class DialogsFragment : BaseOldFragment(), DialogsFragmentView {

    companion object {
        fun newInstance(isForwarded: Boolean = false): DialogsFragment {
            val frag = DialogsFragment()
            frag.isForwarded = isForwarded
            return frag
        }
    }

    @Inject
    open lateinit var presenter: DialogsFragmentPresenter
    @Inject
    lateinit var apiUtils: ApiUtils

    open lateinit var adapter: DialogsAdapter

    var isForwarded = false
    var fwdMessages = ""

    override fun getLayout() = R.layout.fragment_dialogs

    override fun bindViews(view: View) {
        initAdapter()
        initRefresh()
        App.appComponent?.inject(this)
        presenter.view = this
    }

    override fun onNew(view: View) {
        presenter.loadCachedDialogs()
        apiUtils.updateStickers()
    }

    override fun onRecovered(view: View) {
        adapter.stopLoading(presenter.getSaved())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.dialogs))
    }

    open fun initRefresh() {
        swipeRefresh.setOnRefreshListener { presenter.loadDialogs(withClear = true) }
        swipeRefresh.setDistanceToTriggerSync(50)
    }

    open fun initAdapter() {
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        recyclerView.itemAnimator = null
        adapter = DialogsAdapter(safeActivity, { loadMore(it) }, { onClick(it) }, { onLongClick(it) })
        adapter.trier = { loadMore(adapter.itemCount) }
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.dialog_menu, menu)
    }

    open fun loadMore(offset: Int) {
        if (isOnline()) {
            presenter.loadDialogs(offset)
        }
    }

    fun onClick(position: Int) {
        if (position !in adapter.items.indices) return
        if (isForwarded) {
            rootActivity.onBackPressed()
            rootActivity.loadFragment(ChatFragment.newInstance(adapter.items[position], fwdMessages))
        } else {
            rootActivity.loadFragment(ChatFragment.newInstance(adapter.items[position]))
        }
    }

    open fun onLongClick(position: Int): Boolean {
        if (position !in adapter.items.indices) return true

        val message = adapter.items[position]
        getContextPopup(safeActivity, R.layout.popup_dialogs) { view ->
            when (view.id) {

                R.id.llDelete -> showDeleteDialog(safeActivity) {
                    presenter.deleteDialog(message, position)
                    CacheHelper.deleteDialogAsync(getPeerId(message.userId, message.chatId))
                }
                R.id.llRead -> presenter.readDialog(message)
                R.id.llMute -> {
                    val muteList = Prefs.muteList
                    val wasMute = message.isMute
                    message.isMute = !wasMute
                    adapter.notifyItemChanged(position)
                    val from = getPollFrom(message.userId, message.chatId)
                    if (wasMute) {
                        muteList.remove(from)
                    } else {
                        muteList.add(from)
                    }
                    Prefs.muteList = muteList
                }
            }
        }.show()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_search_users -> {
                rootActivity.loadFragment(SearchMessagesFragment())
                true
            }
            R.id.important_menu -> {
                rootActivity.loadFragment(ImportantFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun showLoading() {
        adapter.startLoading()
    }

    override fun hideLoading() {
//        adaptr.stopLoading()
    }

    override fun showError(error: String) {
        adapter.setErrorLoading()
        swipeRefresh.isRefreshing = false
        showError(activity, error)
    }

    override fun onDialogsLoaded(dialogs: MutableList<Message>) {
        adapter.stopLoading(dialogs)
        swipeRefresh?.isRefreshing = false
    }

    override fun onDialogsClear() {
        adapter.clear()
        adapter.isDone = false
    }

    override fun onRemoveDialog(position: Int) {
        adapter.removeAt(position)
    }

    override fun onMessageReceived(event: NewMessageEvent) {
        val userId = event.peerId
        for (position in adapter.items.indices) {
            val message = adapter.items[position]

            if (isRightItem(userId, message)) {
                val out = if (event.isOut()) 1 else 0
                message.date = event.timeStamp
                message.id = event.id
                message.body = event.text
                message.setRead(0)
                message.out = out
                message.attachments = null
                message.fwdMessages = null
                message.emoji = if (event.hasEmoji()) 1 else 0
                message.unread = if (event.isOut()) 0 else message.unread + 1
                if (event.info.getForwardedCount() > 0) {
                    val bicycle = ArrayList<Message>()
                    for (j in 0 until event.info.getForwardedCount()) {
                        bicycle.add(Message())
                    }
                    message.fwdMessages = bicycle
                }
                if (event.hasMedia()) {
                    val bicycle = ArrayList<Attachment>()
                    for (j in 0 until event.info.attachmentsCount) {
                        bicycle.add(Attachment())
                    }
                    message.attachments = bicycle
                }
                CacheHelper.saveMessageAsync(message)
                val wasAtTop = adapter.firstVisiblePosition() == 0
                adapter.removeAt(position)
                adapter.add(message, 0)
                if (wasAtTop) {
                    recyclerView?.scrollToPosition(0)
                }
                return
            }
        }
        presenter.loadNewDialog(event)

    }

    override fun onOnlineChanged(userId: Int, isOnline: Boolean) {
        for (position in adapter.items.indices) {
            val message = adapter.items[position]
            if (isRightItem(userId, message)) {
                message.online = if (isOnline) 1 else 0
                adapter.notifyItemChanged(position)
                break
            }
        }
    }

    override fun onMessageReadIn(userId: Int, mid: Int) {
        for (position in adapter.items.indices) {
            val message = adapter.items[position]
            if (isRightItem(userId, message, mid)) {
                message.readState = 1
                message.unread = 0
                CacheHelper.saveMessageAsync(message)
                adapter.notifyItemChanged(position)
                break
            }
        }
    }

    override fun onMessageReadOut(userId: Int, mid: Int) {
        for (position in adapter.items.indices) {
            val message = adapter.items[position]
            if (isRightItem(userId, message, mid)) {
                message.readState = 1
                CacheHelper.saveMessageAsync(message)
                adapter.notifyItemChanged(position)
                break
            }
        }
    }

    override fun onCacheRestored() {
        if (!Prefs.manualUpdating && isOnline()) {
            swipeRefresh.isRefreshing = true
            presenter.loadDialogs(withClear = true)
        } else {
            swipeRefresh.isRefreshing = false
        }
    }

    override fun onMessageNew(message: Message) {
        adapter.add(message, 0)
        CacheHelper.saveMessageAsync(message)
    }

    private fun isRightItem(userId: Int, mess: Message, mid: Int = 0) =
            (userId < 2000000000 && userId == mess.userId && mess.chatId == 0 ||
                    userId > 2000000000 && mess.chatId + 2000000000 == userId ||
                    userId in 1000000000..2000000000 && mess.userId == -(userId - 1000000000))
                    && (mid == 0 || mess.id == mid)
}