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

package com.twoeightnine.root.xvii.utils.deeplink.cases

import android.content.Intent
import android.net.Uri
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.utils.deeplink.DeepLinkParser

object ShareCaseParser : DeepLinkParser.CaseParser {

    private const val TAG = "share parser"

    override fun getResult(intent: Intent): DeepLinkParser.Result {
        var shareText: String? = null
        val shareMediaUris = mutableListOf<Uri>()
        when {
            intent.action == Intent.ACTION_SEND && intent.type == "text/plain" -> {
                shareText = intent.getStringExtra(Intent.EXTRA_TEXT)
            }
            intent.action == Intent.ACTION_SEND
                    && intent.type?.startsWith("image/") == true -> {
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                        ?.also(shareMediaUris::add)
            }
            intent.action == Intent.ACTION_SEND_MULTIPLE
                    && intent.type?.startsWith("image/") == true -> {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                        ?.forEach(shareMediaUris::add)
            }
            else -> Unit
        }
        L.tag(TAG).log("contains ${shareMediaUris.size} files")
        return when {
            shareText.isNullOrBlank() && shareMediaUris.isEmpty() -> DeepLinkParser.Result.Unknown
            else -> DeepLinkParser.Result.Share(shareText, shareMediaUris)
        }
    }
}