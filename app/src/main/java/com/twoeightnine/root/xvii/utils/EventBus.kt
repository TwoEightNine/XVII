package com.twoeightnine.root.xvii.utils

import com.twoeightnine.root.xvii.background.longpoll.models.events.BaseLongPollEvent
import com.twoeightnine.root.xvii.background.longpoll.models.events.NewMessageEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

object EventBus {

    /**
     * longpoll received new event
     */
    private val longPollEventReceived = PublishSubject.create<BaseLongPollEvent>()

    /**
     * exchange events
     */
    private val exchangeEventReceived = PublishSubject.create<NewMessageEvent>()

    fun subscribeLongPollEventReceived(action: (BaseLongPollEvent) -> Unit): Disposable = longPollEventReceived
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(action)

    fun publishLongPollEventReceived(longPollEvent: BaseLongPollEvent) = longPollEventReceived.onNext(longPollEvent)

    fun subscribeExchangeEventReceived(action: (NewMessageEvent) -> Unit): Disposable = exchangeEventReceived
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(action)

    fun publishExchangeEventReceived(event: NewMessageEvent) = exchangeEventReceived.onNext(event)
}