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

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.twoeightnine.root.xvii.background.longpoll.core.LongPollCore


class NotificationService : Service() {

    companion object {

        fun launch(context: Context, intent: Intent = Intent(context, NotificationService::class.java)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context?, intent: Intent = Intent(context, NotificationService::class.java)) {
            context?.stopService(intent)
        }
    }

    private var core: LongPollCore? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val core = LongPollCore(applicationContext)
        Thread {
            core.run(intent)
        }.start()
        core.showForeground(this)
        this.core = core
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        core?.onDestroy()
        super.onDestroy()
        launch(applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
