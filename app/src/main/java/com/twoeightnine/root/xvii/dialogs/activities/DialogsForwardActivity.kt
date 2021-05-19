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

package com.twoeightnine.root.xvii.dialogs.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.base.ContentActivity
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsForwardFragment
import com.twoeightnine.root.xvii.utils.writeToFileFromContentUri
import java.io.File

class DialogsForwardActivity : ContentActivity() {

    override fun createFragment(intent: Intent?): Fragment {
        var forwarded: String? = null
        var shareText: String? = null
        var shareImage: String? = null
        when {
            intent?.action == Intent.ACTION_SEND && intent.type == "text/plain" -> {
                shareText = intent.getStringExtra(Intent.EXTRA_TEXT)
            }
            intent?.action == Intent.ACTION_SEND
                    && intent.type?.startsWith("image/") == true -> {
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.also { uri ->
                    val imageFile = File(cacheDir, "share.jpg")
                    val written = writeToFileFromContentUri(this, imageFile, uri)
                    shareImage = if (written) {
                        imageFile.absolutePath
                    } else {
                        uri.path
                    }
                }
            }
            else -> {
                forwarded = intent?.extras?.getString(DialogsForwardFragment.ARG_FORWARDED)
            }
        }
        return DialogsForwardFragment.newInstance(forwarded, shareText, shareImage)
    }

    companion object {
        fun launch(context: Context?, forwarded: String? = null) {
            if (context == null) return

            context.startActivity(Intent(context, DialogsForwardActivity::class.java).apply {
                if (!forwarded.isNullOrEmpty()) {
                    putExtra(DialogsForwardFragment.ARG_FORWARDED, forwarded)
                }
            })
        }
    }
}