/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.background.longpoll.core

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Vibrator
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.longpoll.LongPollStorage
import com.twoeightnine.root.xvii.background.longpoll.models.LongPollServer
import com.twoeightnine.root.xvii.background.longpoll.models.LongPollUpdate
import com.twoeightnine.root.xvii.background.longpoll.models.events.*
import com.twoeightnine.root.xvii.background.longpoll.receivers.KeyExchangeHandler
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.utils.notifications.NotificationUtils
import global.msnthrp.xvii.core.journal.JournalUseCase
import global.msnthrp.xvii.data.db.AppDb
import global.msnthrp.xvii.data.dialogs.Dialog
import global.msnthrp.xvii.data.journal.DbJournalDataSource
import global.msnthrp.xvii.uikit.extensions.lowerIf
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.sign
import kotlin.random.Random


class LongPollCore(private val context: Context) {

    private val coreId = Random.nextLong().run { this * sign }

    private val vibrator by lazy { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    private val journalUseCase by lazy {
        JournalUseCase(DbJournalDataSource(appDb.journalDao()))
    }
    private val keyExchangeHandler by lazy {
        KeyExchangeHandler()
    }

    private val unreadMessages = hashMapOf<Int, ArrayList<String>>()
    private val disposables = CompositeDisposable()
    private var isRunning = false

    @Inject
    lateinit var longPollStorage: LongPollStorage

    @Inject
    lateinit var api: ApiService

    @Inject
    lateinit var appDb: AppDb

    fun run(intent: Intent?) {
        App.appComponent?.inject(this)

        isRunning = false
        runningCoreId = coreId
        l("launched core ...${coreId % 1000}")
        while (true) {
            while (isRunning) {
                Thread.sleep(WAIT_DELAY)
            }
            getUpdates()

            doOnNotCurrentCore {
                return
            }
        }
    }

    private fun getUpdates() {
        isRunning = true
        lastRun = time()
        longPollStorage.getLongPollServer() ?: updateLongPollServer()

        l("on ${longPollStorage.getLongPollServer()?.ts}")
        getConnectSingle(longPollStorage.getLongPollServer() ?: return)
                .subscribe({ longPollUpdate: LongPollUpdate ->
                    onUpdateReceived(longPollUpdate)
                }, {
                    lw("error during getting updates", it)
                    waitInBg(NO_NETWORK_DELAY)
                }).let { disposables.add(it) }
    }

    private fun updateLongPollServer() {
        api.getLongPollServer()
                .subscribeSmart({
                    longPollStorage.saveLongPoll(it)
                    isRunning = false
                }, { msg ->
                    lw("error during updating: $msg")
                    waitInBg(NO_NETWORK_DELAY)
                }, {
                    lw("no network")
                    waitInBg(NO_NETWORK_DELAY)
                }).let { disposables.add(it) }
    }

    private fun onUpdateReceived(longPollUpdate: LongPollUpdate) {
        doOnNotCurrentCore { return }
        when {
            longPollUpdate.shouldUpdateServer() -> updateLongPollServer()
            else -> {
                updateTs(longPollStorage.getLongPollServer() ?: return, longPollUpdate.ts)
                deliverUpdate(longPollUpdate.updates)
                isRunning = false
            }
        }
    }

    private fun deliverUpdate(updates: ArrayList<ArrayList<Any>>) {
        val events = LongPollEventFactory.createAll(updates)
        if (events.isNotEmpty()) {
            l("updates: ${events.size}")
        }
        events.forEach { event ->
            putEventToJournal(event)

            var publishEventToCommonBus = true
            when (event) {
                is UnreadCountEvent -> processUnreadCount(event)
                is ReadIncomingEvent -> processReadIncoming(event)
                is NewMessageEvent -> {
                    val isExchange = processExchangeMessage(event)
                    if (isExchange) {
                        publishEventToCommonBus = false
                        EventBus.publishExchangeEventReceived(event)
                    } else {
                        processNewMessage(event)
                    }
                }
            }
            if (publishEventToCommonBus) {
                EventBus.publishLongPollEventReceived(event)
            }
        }
    }

    /**
     * close notification for peerId if incoming message is read
     */
    private fun processReadIncoming(event: ReadIncomingEvent) {
        unreadMessages[event.peerId]?.clear()
        NotificationUtils.hideMessageNotification(context, event.peerId)
    }

    /**
     * cancel all notifications if unreadCount is 0
     */
    private fun processUnreadCount(event: UnreadCountEvent) {
        if (event.unreadCount == 0) {
            unreadMessages.clear()
            try {
                NotificationUtils.hideAllMessageNotifications(context)
            } catch (e: SecurityException) {
                lw("error cancelling all", e)
            }
        }
    }

    /**
     * checks it [event] is exchange event, process it if it is
     *
     * @return true if processed and no more processing required
     */
    private fun processExchangeMessage(event: NewMessageEvent): Boolean {
        if (!event.text.matchesXviiKeyEx()) return false

        keyExchangeHandler.handleKeyExchange(context, event)
        return true
    }

    /**
     * show notification if new message received
     */
    private fun processNewMessage(event: NewMessageEvent) {
        if (event.isOut()
                || event.peerId in Prefs.muteList
                || event.isUser() && !Prefs.showNotifs
                || !event.isUser() && !Prefs.showNotifsChats) return

        val shouldVibrate = Prefs.vibrateChats && !event.isUser()
                || Prefs.vibrate && event.isUser()
        val shouldRing = Prefs.soundChats && !event.isUser()
                || Prefs.sound && event.isUser()
        if (!isInForeground() && shouldVibrate) {
            vibrate()
        }

        val shouldShowContent = event.isUser() && Prefs.showContent
                || !event.isUser() && Prefs.showContentChats
        if (event.peerId !in unreadMessages) {
            unreadMessages[event.peerId] = arrayListOf()
        }
        unreadMessages[event.peerId]?.add(event.getResolvedMessage(context, !shouldShowContent))

        val content = unreadMessages[event.peerId]?.takeLast(5)?.let { ArrayList(it) }
                ?: arrayListOf(context.getString(R.string.messages))

        val timeStamp = event.timeStamp * 1000L
        val ledColor = when {
            event.isUser() -> Prefs.ledColor
            else -> Prefs.ledColorChats
        }

        val isPeerIdStillActual = { peerId: Int ->
            !unreadMessages[peerId].isNullOrEmpty()
        }

        // trying to get dialog from database
        getDialog(event.peerId, { dialog ->
            val name = dialog.aliasOrTitle.lowerIf(Prefs.lowerTexts)
            if (Prefs.showName) {
                loadBitmapIcon(context, dialog.photo, useSquare = Prefs.useStyledNotifications) { bitmap ->
                    NotificationUtils.showNewMessageNotification(
                            context = context,
                            content = content,
                            timeStamp = timeStamp,
                            peerId = event.peerId,
                            messageId = event.id,
                            userName = name,
                            title = name,
                            icon = bitmap,
                            ledColor = ledColor,
                            photo = dialog.photo,
                            unreadMessagesCount = unreadMessages.getOrElse(event.peerId) { emptyList() }.size,
                            shouldVibrate = shouldVibrate,
                            shouldRing = shouldRing,
                            stylish = Prefs.useStyledNotifications,
                            isPeerIdStillActual = isPeerIdStillActual
                    )
                }
            } else {
                NotificationUtils.showNewMessageNotification(
                        context = context,
                        content = content,
                        timeStamp = timeStamp,
                        peerId = event.peerId,
                        messageId = event.id,
                        userName = name,
                        title = context.getString(R.string.app_name),
                        icon = BitmapFactory.decodeResource(context.resources, R.drawable.xvii_logo_128_circle),
                        ledColor = ledColor,
                        photo = dialog.photo,
                        unreadMessagesCount = unreadMessages.getOrElse(event.peerId) { emptyList() }.size,
                        shouldVibrate = shouldVibrate,
                        shouldRing = shouldRing,
                        stylish = Prefs.useStyledNotifications,
                        isPeerIdStillActual = isPeerIdStillActual
                )
            }
        }, {
            if (event.peerId.matchesChatId()) {
                // chats are shown as is
                val title = event.title.lowerIf(Prefs.lowerTexts)
                NotificationUtils.showNewMessageNotification(
                        context = context,
                        content = content,
                        timeStamp = timeStamp,
                        peerId = event.peerId,
                        messageId = event.id,
                        userName = title,
                        title = title,
                        icon = BitmapFactory.decodeResource(context.resources, R.drawable.xvii_logo_128_circle),
                        ledColor = ledColor,
                        photo = null,
                        unreadMessagesCount = unreadMessages.getOrElse(event.peerId) { emptyList() }.size,
                        shouldVibrate = shouldVibrate,
                        shouldRing = shouldRing,
                        stylish = Prefs.useStyledNotifications,
                        isPeerIdStillActual = isPeerIdStillActual
                )
            } else {

                // for groups and users try to resolve them
                resolveSenderByPeerId(event.peerId, { title, photo ->
                    val processedTitle = title.lowerIf(Prefs.lowerTexts)
                    loadBitmapIcon(context, photo, useSquare = Prefs.useStyledNotifications) { bitmap ->
                        NotificationUtils.showNewMessageNotification(
                                context = context,
                                content = content,
                                timeStamp = timeStamp,
                                peerId = event.peerId,
                                messageId = event.id,
                                userName = processedTitle,
                                title = processedTitle,
                                icon = bitmap,
                                ledColor = ledColor,
                                photo = photo,
                                unreadMessagesCount = unreadMessages.getOrElse(event.peerId) { emptyList() }.size,
                                shouldVibrate = shouldVibrate,
                                shouldRing = shouldRing,
                                stylish = Prefs.useStyledNotifications,
                                isPeerIdStillActual = isPeerIdStillActual
                        )
                    }
                }, {
                    NotificationUtils.showNewMessageNotification(
                            context = context,
                            content = content,
                            timeStamp = timeStamp,
                            peerId = event.peerId,
                            messageId = event.id,
                            userName = event.title,
                            title = context.getString(R.string.app_name),
                            icon = BitmapFactory.decodeResource(context.resources, R.drawable.xvii_logo_128_circle),
                            ledColor = ledColor,
                            photo = null,
                            unreadMessagesCount = unreadMessages.getOrElse(event.peerId) { emptyList() }.size,
                            shouldVibrate = shouldVibrate,
                            shouldRing = shouldRing,
                            stylish = Prefs.useStyledNotifications,
                            isPeerIdStillActual = isPeerIdStillActual
                    )
                })
            }
        })

    }

    fun onDestroy() {
        disposables.dispose()
        isRunning = false
        l("service destroyed")
    }

    private fun updateTs(longPollServer: LongPollServer, ts: Int) {
        val newServer = LongPollServer(
                longPollServer.key,
                longPollServer.server,
                ts
        )
        longPollStorage.saveLongPoll(newServer)
    }

    fun showForeground(service: Service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.showLongPollNotification(service)
        }
    }

    /**
     * returns title and photo by peerId
     * used for groups and users
     */
    private fun resolveSenderByPeerId(
            peerId: Int,
            onSuccess: (String, String?) -> Unit,
            onFail: () -> Unit
    ) {
        if (peerId < 0) {
            api.getGroups("${-peerId}")
                    .subscribeSmart({ groups ->
                        val group = groups.getOrElse(0) {
                            onFail()
                            return@subscribeSmart
                        }
                        onSuccess(group.name, group.photo100)
                    }, {
                        lw("resolve group error: $it")
                        onFail()
                    })
        } else {
            api.getUsers("$peerId")
                    .subscribeSmart({ users ->
                        val user = users.getOrElse(0) {
                            onFail()
                            return@subscribeSmart
                        }
                        onSuccess(user.fullName, user.photo100)
                    }, {
                        lw("resolving user error: $it")
                        onFail()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun getDialog(
            peerId: Int,
            onSuccess: (Dialog) -> Unit,
            onFail: () -> Unit) {
        appDb.dialogsDao().getDialogs(peerId)
                .compose(applySingleSchedulers())
                .subscribe(onSuccess) {
                    lw("loading from db error", it)
                    onFail()
                }
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            vibrator.vibrate(VIBRATE_DELAY)
        }
    }

    private fun getConnectSingle(longPollServer: LongPollServer) =
            api.connectLongPoll("https://${longPollServer.server}", longPollServer.key, longPollServer.ts)

    @SuppressLint("CheckResult")
    private fun waitInBg(delayMs: Long) {
        l("waiting in background")
        Observable.timer(delayMs, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    isRunning = false
                }
    }

    private inline fun doOnNotCurrentCore(runnable: () -> Unit) {
        if (runningCoreId != coreId) {
            l("core ...${coreId % 1000} exits")
            runnable()
        }
    }

    private fun l(s: String) {
        L.tag(TAG).log(s)
    }

    private fun lw(s: String, throwable: Throwable? = null) {
        L.tag(TAG).throwable(throwable).log(s)
    }

    private fun putEventToJournal(event: BaseLongPollEvent) {
        when (event) {
            is OnlineEvent -> {
                journalUseCase.addUserOnline(
                        userId = event.userId,
                        deviceCode = event.deviceCode,
                        timeStamp = event.timeStamp.toLongTimeStamp()
                )
            }
            is OfflineEvent -> {
                journalUseCase.addUserOffline(
                        userId = event.userId,
                        lastSeen = event.timeStamp.toLongTimeStamp()
                )
            }
            is TypingEvent -> {
                journalUseCase.addActivity(
                        peerId = event.userId,
                        isVoice = false
                )
            }
            is RecordingAudioEvent -> {
                journalUseCase.addActivity(
                        peerId = event.peerId,
                        isVoice = true
                )
            }
            is InstallFlagsEvent -> event.takeIf { it.isDeleted }?.let { deletionEvent ->
                if (!deletionEvent.isOut()) {
                    journalUseCase.addMessageDeleted(
                            peerId = deletionEvent.peerId,
                            messageId = deletionEvent.id
                    )
                }
            }
            is NewMessageEvent -> {
                if (!event.isOut()) {
                    journalUseCase.addMessage(
                            peerId = event.peerId,
                            messageId = event.id,
                            messageText = event.text,
                            isEdited = false,
                            fromId = event.info.from,
                            timeStamp = event.timeStamp.toLongTimeStamp()
                    )
                }
            }
            is EditMessageEvent -> {
                if (!event.isOut()) {
                    journalUseCase.addMessage(
                            peerId = event.peerId,
                            messageId = event.id,
                            messageText = event.text,
                            isEdited = true,
                            fromId = event.info.from,
                            timeStamp = System.currentTimeMillis()
                    )
                }
            }
            is ReadOutgoingEvent -> {
                journalUseCase.addReadMessage(
                        peerId = event.peerId,
                        messageId = event.mid
                )
            }
        }
    }

    private fun Int.toLongTimeStamp() = this * 1000L

    companion object {

        private const val TAG = "longpoll"

        private const val VIBRATE_DELAY = 60L
        private const val WAIT_DELAY = 1000L
        private const val NO_NETWORK_DELAY = 5000L

        /**
         * if the core didn't call [getUpdates] for [LAST_RUN_ALLOWED_DELAY] seconds
         * it is probably down =(
         */
        private const val LAST_RUN_ALLOWED_DELAY = 45

        /**
         * watches for running
         */
        var lastRun = 0
            private set

        private var runningCoreId: Long? = null

        fun isProbablyRunning() = time() - lastRun < LAST_RUN_ALLOWED_DELAY
    }
}