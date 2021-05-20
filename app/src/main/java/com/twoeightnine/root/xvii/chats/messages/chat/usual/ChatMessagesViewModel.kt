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

package com.twoeightnine.root.xvii.chats.messages.chat.usual

import com.twoeightnine.root.xvii.chats.messages.chat.base.BaseChatMessagesViewModel
import com.twoeightnine.root.xvii.chats.tools.ChatStorage
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.applySchedulers
import com.twoeightnine.root.xvii.utils.subscribeSmart
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ChatMessagesViewModel(
        api: ApiService,
        private val chatStorage: ChatStorage
) : BaseChatMessagesViewModel(api) {

    override fun attachPhoto(path: String, onAttached: (String, Attachment) -> Unit) {
        api.getPhotoUploadServer()
                .subscribeSmart({ uploadServer ->
                    val file = File(path)
                    val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)
                    api.uploadPhoto(uploadServer.uploadUrl ?: "", body)
                            .compose(applySchedulers())
                            .subscribe({ uploaded ->
                                api.saveMessagePhoto(
                                        uploaded.photo ?: "",
                                        uploaded.hash ?: "",
                                        uploaded.server
                                )
                                        .subscribeSmart({
                                            onAttached(path, Attachment(it[0]))
                                        }, { error ->
                                            onErrorOccurred(error)
                                            lw("save uploaded photo error: $error")
                                        })
                            }, { error ->
                                val message = error.message ?: "null"
                                lw("uploading photo error: $message")
                                onErrorOccurred(message)
                            })

                }, { error ->
                    onErrorOccurred(error)
                    lw("getting ploading server error: $error")
                })
    }

    override fun prepareTextOut(text: String?) = text ?: ""

    override fun prepareTextIn(text: String) = text

    fun getMessageText() = chatStorage.getMessageText(peerId)

    fun invalidateMessageText(text: String) {
        chatStorage.setMessageText(peerId, text)
    }
}