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

package com.twoeightnine.root.xvii.chats.messages.chat.secret

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.ContentActivity
import com.twoeightnine.root.xvii.model.User
import global.msnthrp.xvii.data.dialogs.Dialog

class SecretChatActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun onResume() {
        super.onResume()
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun createFragment(intent: Intent?): Fragment {
        val args = intent?.extras
        val dialog = args?.getParcelable(DIALOG) ?: Dialog()
        return SecretChatMessagesFragment.newInstance(dialog)
    }

    override fun getNavigationBarColor() = Color.TRANSPARENT

    companion object {
        const val DIALOG = "dialog"

        fun launch(context: Context?, userId: Int, title: String,
                   avatar: String? = null) {
            launch(context, Dialog(
                    peerId = userId,
                    title = title,
                    photo = avatar
            ))

        }

        fun launch(context: Context?, dialog: Dialog) {
            context ?: return

            context.startActivity(Intent(context, SecretChatActivity::class.java).apply {
                putExtra(DIALOG, dialog)
                flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }

        fun launch(context: Context?, user: User) {
            launch(context, Dialog(
                    peerId = user.id,
                    title = user.fullName,
                    photo = user.photo100
            ))
        }
    }
}