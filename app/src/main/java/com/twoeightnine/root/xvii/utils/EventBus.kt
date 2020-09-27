package com.twoeightnine.root.xvii.utils

import com.twoeightnine.root.xvii.background.longpoll.models.events.BaseLongPollEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

object EventBus {

    /**
     * longpoll received new event
     */
    private val longPollEventReceived = PublishSubject.create<BaseLongPollEvent>()

    fun subscribeLongPollEventReceived(action: (BaseLongPollEvent) -> Unit): Disposable = longPollEventReceived
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(action)

    fun publishLongPollEventReceived(longPollEvent: BaseLongPollEvent) = longPollEventReceived.onNext(longPollEvent)
}