package com.twoeightnine.root.xvii.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import global.msnthrp.xvii.uikit.extensions.SimpleBitmapTarget
import global.msnthrp.xvii.uikit.extensions.load

object ShortcutUtils {

    fun createShortcut(context: Context, dialog: Dialog, onAdded: () -> Unit) {
        val title = dialog.alias ?: dialog.title

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(ChatActivity.PEER_ID, dialog.peerId)
            putExtra(ChatActivity.TITLE, title)
            putExtra(ChatActivity.AVATAR, dialog.photo)
            flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        SimpleBitmapTarget(tag = "shortcut") { bitmap, _ ->

            if (Build.VERSION.SDK_INT >= 25) {
                val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                intent.action = Intent.ACTION_VIEW

                val icon = when {
                    bitmap != null -> Icon.createWithBitmap(bitmap)
                    else -> Icon.createWithResource(context, R.drawable.xvii_logo_128_circle)
                }
                val shortcutInfo = ShortcutInfo.Builder(context, dialog.peerId.toString())
                        .setShortLabel(title)
                        .setIcon(icon)
                        .setIntent(intent)
                        .build()
                shortcutManager?.addDynamicShortcuts(arrayListOf(shortcutInfo))
                if (Build.VERSION.SDK_INT >= 26) {
                    shortcutManager?.requestPinShortcut(shortcutInfo, null)
                }
            } else {
                context.sendBroadcast(Intent().apply {
                    putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent)
                    putExtra(Intent.EXTRA_SHORTCUT_NAME, dialog)
                    if (bitmap != null) {
                        putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap)
                    } else {
                        putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                Intent.ShortcutIconResource.fromContext(context, R.drawable.xvii_logo_128_circle))
                    }
                    putExtra("duplicate", false)
                    action = "com.android.launcher.action.INSTALL_SHORTCUT"
                })
            }
            onAdded()
        }.load(context, dialog.photo ?: "") { circleCrop() }
    }



}