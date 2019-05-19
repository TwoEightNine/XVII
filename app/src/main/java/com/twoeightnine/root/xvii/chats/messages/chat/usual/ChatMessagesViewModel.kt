package com.twoeightnine.root.xvii.chats.messages.chat.usual

import com.twoeightnine.root.xvii.chats.messages.chat.base.BaseChatMessagesViewModel
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.applySchedulers
import com.twoeightnine.root.xvii.utils.subscribeSmart
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class ChatMessagesViewModel(api: ApiService) : BaseChatMessagesViewModel(api) {

    override fun attachPhoto(path: String, onAttached: (String, Attachment) -> Unit) {
        api.getPhotoUploadServer()
                .subscribeSmart({ uploadServer ->
                    val file = File(path)
                    val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
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
}