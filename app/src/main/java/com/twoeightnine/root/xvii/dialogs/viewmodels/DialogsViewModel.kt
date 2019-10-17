package com.twoeightnine.root.xvii.dialogs.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.App.Companion.context
import com.twoeightnine.root.xvii.background.longpoll.models.events.*
import com.twoeightnine.root.xvii.db.AppDb
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.network.response.ConversationsResponse
import com.twoeightnine.root.xvii.utils.*
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DialogsViewModel(
        private val api: ApiService,
        private val appDb: AppDb
) : ViewModel() {

    private var longPollSubscription: Disposable? = null
    private var typingCompositeDisposable = CompositeDisposable()

    private val typingPeerIds =
            MutableLiveData<HashSet<Int>>().apply { value = hashSetOf() }
    private val dialogsLiveData = WrappedMutableLiveData<ArrayList<Dialog>>()

    init {
        longPollSubscription?.dispose()
        longPollSubscription = EventBus.subscribeLongPollEventReceived { event ->
            when (event) {
                is OnlineEvent -> onStatusChanged(event.userId, true)
                is OfflineEvent -> onStatusChanged(event.userId, false)
                is ReadOutgoingEvent -> onReadStateChanged(event.peerId)
                is ReadIncomingEvent -> onReadStateChanged(event.peerId)
                is NewMessageEvent -> onNewMessageAdded(event)
                is DeleteMessagesEvent -> onDialogRemoved(event.peerId)
                is TypingEvent -> onTyping(event.userId)
                is TypingChatEvent -> onTyping(event.peerId)
            }
        }
    }

    fun getTypingPeerIds() = typingPeerIds as LiveData<HashSet<Int>>

    fun getDialogs() = dialogsLiveData as WrappedLiveData<ArrayList<Dialog>>

    @SuppressLint("CheckResult")
    fun loadDialogs(offset: Int = 0) {
        if (!isOnline()) {
            if (offset == 0) {
                appDb.dialogsDao().getDialogs()
                        .compose(applySingleSchedulers())
                        .subscribe({ dialogs ->
                            dialogsLiveData.value = Wrapper(ArrayList(dialogs))
                        }, {
                            it.printStackTrace()
                            lw("error loading from cache: ${it.message}")
                            dialogsLiveData.value = Wrapper(error = it.message)
                        })
            }
            return
        }
        api.getConversations(COUNT_CONVERSATIONS, offset)
                .map { convertToDialogs(it) }
                .subscribeSmart({ dialogs ->
                    val existing = if (offset == 0) {
                        arrayListOf()
                    } else {
                        dialogsLiveData.value?.data ?: arrayListOf()
                    }
                    dialogsLiveData.value = Wrapper(existing.apply { addAll(dialogs) })
                    notifyDialogsChanged()
                    saveDialogsAsync(dialogs)
                    setOffline()
                }, ::onErrorOccurred)
    }

    fun readDialog(dialog: Dialog) {
        api.markAsRead("${dialog.messageId}")
                .subscribeSmart({}, ::onErrorOccurred)
    }

    fun deleteDialog(dialog: Dialog) {
        api.deleteConversation(dialog.peerId, COUNT_DELETE)
                .subscribeSmart({
                    dialogsLiveData.value?.data?.removeAll { it.peerId == dialog.peerId }
                    notifyDialogsChanged()
                    removeDialog(dialog)
                }, ::onErrorOccurred)
    }

    fun muteDialog(d: Dialog) {
        val dialog = dialogsLiveData.value?.data
                ?.firstOrNull { it.peerId == d.peerId } ?: return

        val muteList = Prefs.muteList
        dialog.isMute = !dialog.isMute
        if (dialog.isMute) {
            muteList.add(dialog.peerId)
        } else {
            muteList.remove(dialog.peerId)
        }
        Prefs.muteList = muteList
        notifyDialogsChanged()
        saveDialogAsync(dialog)
    }

    fun pinDialog(d: Dialog) {
        val dialog = dialogsLiveData.value?.data
                ?.find { it.peerId == d.peerId } ?: return

        dialog.isPinned = !dialog.isPinned
        notifyDialogsChanged()
        saveDialogAsync(dialog)
    }

    fun addAlias(d: Dialog, alias: String) {
        val dialog = dialogsLiveData.value?.data
                ?.find { it.peerId == d.peerId } ?: return

        dialog.alias = if (alias.isNotEmpty()) alias else null
        notifyDialogsChanged()
        saveDialogAsync(dialog)
    }

    private fun convertToDialogs(resp: BaseResponse<ConversationsResponse>): BaseResponse<ArrayList<Dialog>> {
        val dialogs = arrayListOf<Dialog>()
        val response = resp.response
        val muteList = Prefs.muteList
        getStoredDialogs { storedDialogs ->
            val pinnedIds = storedDialogs.filter { it.isPinned }.map { it.peerId }

            response?.items?.forEach { dm ->
                val message = dm.lastMessage
                dialogs.add(Dialog(
                        message.peerId,
                        message.id,
                        response.getTitleFor(dm) ?: "???",
                        response.getPhotoFor(dm),
                        message.getResolvedMessage(context),
                        message.date,
                        message.isOut(),
                        dm.conversation.isRead(),
                        dm.conversation.unreadCount,
                        response.isOnline(dm),
                        message.peerId in muteList,
                        message.peerId in pinnedIds,
                        storedDialogs.find { it.peerId == message.peerId }?.alias
                ))
            }
        }
        return BaseResponse(dialogs, resp.error)
    }

    private fun setOffline() {
        if (Prefs.beOffline) {
            api.setOffline()
                    .subscribeSmart({}, {})
        }
    }

    @SuppressLint("CheckResult")
    private fun getStoredDialogs(onLoaded: (List<Dialog>) -> Unit) {
        appDb.dialogsDao().getDialogs()
                .subscribe(onLoaded) {
                    it.printStackTrace()
                    lw("error loading stored: ${it.message}")
                    onLoaded(arrayListOf())
                }
    }

    private fun onStatusChanged(peerId: Int, isOnline: Boolean) {
        val dialog = dialogsLiveData.value?.data
                ?.find { it.peerId == peerId } ?: return

        dialog.isOnline = isOnline
        notifyDialogsChanged()
        saveDialogAsync(dialog)
    }

    private fun onReadStateChanged(peerId: Int) {
        val dialog = dialogsLiveData.value?.data
                ?.find { it.peerId == peerId } ?: return

        with(dialog) {
            isRead = true
            unreadCount = 0
        }
        notifyDialogsChanged()
        saveDialogAsync(dialog)
    }

    private fun onNewMessageAdded(event: NewMessageEvent) {
        val dialog = dialogsLiveData.value?.data
                ?.find { event.peerId == it.peerId }
        if (dialog != null) { // existing dialog
            with(dialog) {
                messageId = event.id
                isRead = false
                isOut = event.isOut()
                text = event.getResolvedMessage(context)
                timeStamp = event.timeStamp
                unreadCount++
            }
            notifyDialogsChanged()
            saveDialogAsync(dialog)
        } else { // new dialog
            api.getConversations(COUNT_NEW_CONVERSATION)
                    .map { convertToDialogs(it) }
                    .subscribeSmart({ dialogs ->
                        val newDialog = dialogs.find { it.peerId == event.peerId }
                                ?: return@subscribeSmart

                        dialogsLiveData.value?.data?.add(newDialog)
                        notifyDialogsChanged()
                        saveDialogAsync(newDialog)
                    }, ::onErrorOccurred)
        }
    }

    private fun onDialogRemoved(peerId: Int) {
        val dialog = dialogsLiveData.value?.data?.find { it.peerId == peerId } ?: return

        dialogsLiveData.value?.data?.remove(dialog)
        notifyDialogsChanged()
        removeDialog(dialog)
    }

    private fun onTyping(peerId: Int) {
        val set = typingPeerIds.value ?: return

        if (peerId !in set) {
            set.add(peerId)
        }
        typingPeerIds.value = set

        Observable.just(peerId)
                .delay(5, TimeUnit.SECONDS)
                .onErrorReturnItem(0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { emittedPeerId ->
                    val setAfterEmit = typingPeerIds.value ?: return@subscribe

                    if (emittedPeerId in setAfterEmit) {
                        setAfterEmit.remove(emittedPeerId)
                    }
                    typingPeerIds.value = setAfterEmit
                }
                .let { typingCompositeDisposable.add(it) }
    }

    private fun notifyDialogsChanged() {
        val dialogs = dialogsLiveData.value?.data ?: return
        dialogsLiveData.value = Wrapper(ArrayList(dialogs.sortedByDescending(DIALOGS_COMPARATOR)))
    }

    private fun onErrorOccurred(error: String) {
        dialogsLiveData.value = Wrapper(error = error)
    }

    @SuppressLint("CheckResult")
    private fun saveDialogsAsync(dialogs: ArrayList<Dialog>) {
        appDb.dialogsDao().insertDialogs(*dialogs.toTypedArray())
                .compose(applyCompletableSchedulers())
                .subscribe({
                    l("cached list")
                }, {
                    it.printStackTrace()
                    lw("cache list error: ${it.message}")
                })
    }

    @SuppressLint("CheckResult")
    private fun saveDialogAsync(dialog: Dialog) {
        appDb.dialogsDao().insertDialog(dialog)
                .compose(applyCompletableSchedulers())
                .subscribe({
                    l("cached one dialog")
                }, {
                    it.printStackTrace()
                    lw("cache one dialog error: ${it.message}")
                })
    }

    @SuppressLint("CheckResult")
    private fun removeDialog(dialog: Dialog) {
        appDb.dialogsDao().removeDialog(dialog)
                .compose(applySingleSchedulers())
                .subscribe({
                    l("removed from cache")
                }, {
                    it.printStackTrace()
                    lw("remove from cache err: ${it.message}")
                })
    }

    override fun onCleared() {
        longPollSubscription?.dispose()
        typingCompositeDisposable.dispose()
        super.onCleared()
    }

    private fun l(s: String) {
        Lg.i("[dialogs] $s")
    }

    private fun lw(s: String) {
        Lg.wtf("[dialogs] $s")
    }

    companion object {

        /**
         * sorts respecting [Dialog.isPinned] parameter to be first
         */
        private val DIALOGS_COMPARATOR = { dialog: Dialog ->
            (if (dialog.isPinned) 1 else 0) * 10000000000L + dialog.timeStamp
        }

        const val COUNT_NEW_CONVERSATION = 3
        const val COUNT_CONVERSATIONS = 50
        const val COUNT_DELETE = 10000
    }

    class Factory @Inject constructor(
            private val api: ApiService,
            private val appDb: AppDb
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DialogsViewModel::class.java)) {
                return DialogsViewModel(api, appDb) as T
            }
            throw IllegalArgumentException("Unknown $modelClass")
        }
    }
}