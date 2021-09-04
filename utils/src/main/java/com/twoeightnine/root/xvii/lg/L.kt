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

package com.twoeightnine.root.xvii.lg

import android.util.Log
import global.msnthrp.utils.BuildConfig
import java.util.concurrent.locks.ReentrantLock

class L private constructor() {

    private var tag: String? = null
    private var isWarn = false
    private var onlyDebug = false
    private var throwable: Throwable? = null

    fun warn() = apply {
        isWarn = true
    }

    fun debug() = apply {
        onlyDebug = true
    }

    fun throwable(throwable: Throwable?) = apply {
        warn()
        this.throwable = throwable
    }

    fun log(message: String) {
        if (!BuildConfig.DEBUG && onlyDebug) return
        lock.lock()
        try {
            events.add(LgEvent(
                    text = message,
                    tag = tag,
                    throwable = throwable,
                    warn = isWarn
            ))
            val tagPreview = when (tag) {
                null -> ""
                else -> "[$tag] "
            }
            val text = "$tagPreview$message"
            if (isWarn) {
                Log.wtf(TAG, text, throwable)
            } else {
                Log.i(TAG, text, throwable)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            lock.unlock()
        }
    }

    companion object {

        private const val TAG = "vktag"

        private val lock = ReentrantLock()
        private val events = arrayListOf<LgEvent>()

        fun events(count: Int? = null): List<LgEvent> {
            lock.lock()
            return try {
                ArrayList(events.takeLast(count ?: events.size))
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            } finally {
                lock.unlock()
            }
        }

        fun events(transformer: EventTransformer, count: Int? = null): List<String> =
                events(count).map(transformer::transform)

        fun def() = L()

        fun tag(tag: String): L {
            return L().apply { this.tag = tag }
        }
    }

    interface EventTransformer {

        fun transform(event: LgEvent): String
    }

}