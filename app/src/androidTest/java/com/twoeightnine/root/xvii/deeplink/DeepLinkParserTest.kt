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

package com.twoeightnine.root.xvii.deeplink

import android.content.Intent
import android.net.Uri
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.utils.asChatPeerId
import com.twoeightnine.root.xvii.utils.deeplink.DeepLinkParser
import org.junit.Assert
import org.junit.Test

class DeepLinkParserTest {

    @Test
    fun parseUserChatOwner() = with(DeepLinkParser()) {
        createUrlIntentAndParse(URL_ID + "0").assertChatOwner(0)
        createUrlIntentAndParse(URL_ID + "1").assertChatOwner(1)
        createUrlIntentAndParse(URL_ID + "13").assertChatOwner(13)
        createUrlIntentAndParse(URL_ID + "666").assertChatOwner(666)
        createUrlIntentAndParse(URL_ID + "1753175317").assertChatOwner(1753175317)
    }

    @Test
    fun parseGroupChatOwner() = with(DeepLinkParser()) {
        listOf(URL_CLUB, URL_PUBLIC).forEach { url ->
            createUrlIntentAndParse(url + "1").assertChatOwner(-1)
            createUrlIntentAndParse(url + "100").assertChatOwner(-100)
            createUrlIntentAndParse(url + App.GROUP).assertChatOwner(-App.GROUP)
        }
    }

    @Test
    fun parseChat() = with(DeepLinkParser()) {
        createUrlIntentAndParse(URL_CHAT_WITH_PEER + "1").assertChat(1)
        createUrlIntentAndParse(URL_CHAT_WITH_PEER + "13").assertChat(13)
        createUrlIntentAndParse(URL_CHAT_WITH_PEER + "1753175317").assertChat(1753175317)

        createUrlIntentAndParse(URL_CHAT_WITH_PEER + "-1").assertChat(-1)
        createUrlIntentAndParse(URL_CHAT_WITH_PEER + (-App.GROUP)).assertChat(-App.GROUP)

        createUrlIntentAndParse(URL_CHAT_WITH_PEER + "c1").assertChat(1.asChatPeerId())
        createUrlIntentAndParse(URL_CHAT_WITH_PEER + "c13").assertChat(13.asChatPeerId())
    }

    @Test
    fun parseEmptyChat() = with(DeepLinkParser()) {
        createUrlIntentAndParse(URL_CHAT).let { result ->
            Assert.assertTrue(result is DeepLinkParser.Result.Unknown)
        }
    }

    private fun DeepLinkParser.Result.assertChatOwner(expectedPeerId: Int) {
        Assert.assertTrue(this is DeepLinkParser.Result.ChatOwner)
        Assert.assertEquals(expectedPeerId, (this as DeepLinkParser.Result.ChatOwner).peerId)
    }

    private fun DeepLinkParser.Result.assertChat(expectedPeerId: Int) {
        Assert.assertTrue(this is DeepLinkParser.Result.Chat)
        Assert.assertEquals(expectedPeerId, (this as DeepLinkParser.Result.Chat).peerId)
    }


    private fun DeepLinkParser.createUrlIntentAndParse(url: String): DeepLinkParser.Result {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        return parse(intent)
    }

    companion object {

        private const val URL_ID = "https://vk.com/id"
        private const val URL_CLUB = "https://vk.com/club"
        private const val URL_PUBLIC = "https://vk.com/public"
        private const val URL_CHAT = "https://vk.com/im"
        private const val URL_CHAT_WITH_PEER = "https://vk.com/im?sel="
    }

}