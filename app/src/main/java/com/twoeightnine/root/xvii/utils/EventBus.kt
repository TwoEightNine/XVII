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