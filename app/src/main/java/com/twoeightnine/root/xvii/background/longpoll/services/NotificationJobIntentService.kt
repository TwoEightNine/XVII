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

package com.twoeightnine.root.xvii.background.longpoll.services

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.twoeightnine.root.xvii.background.longpoll.core.LongPollCore


class NotificationJobIntentService : JobIntentService() {

    companion object {
        private const val JOB_ID = 113

        fun enqueue(context: Context, intent: Intent = Intent(context, NotificationJobIntentService::class.java)) {
            enqueueWork(context, NotificationJobIntentService::class.java, JOB_ID, intent)
        }
    }

    private val core by lazy { LongPollCore(applicationContext) }

    override fun onHandleWork(intent: Intent) {
        core.run(intent)
    }

    override fun onDestroy() {
        core.onDestroy()
        super.onDestroy()
        enqueue(applicationContext)
    }
}
