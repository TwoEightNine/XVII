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

package com.twoeightnine.root.xvii.chats.attachments.base

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.chats.attachments.audios.AudioAttachmentsViewModel
import com.twoeightnine.root.xvii.chats.attachments.docs.DocAttachmentsViewModel
import com.twoeightnine.root.xvii.chats.attachments.links.LinkAttachmentsViewModel
import com.twoeightnine.root.xvii.chats.attachments.photos.PhotoAttachmentsViewModel
import com.twoeightnine.root.xvii.chats.attachments.videos.VideoAttachmentsViewModel
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.AttachmentsResponse
import com.twoeightnine.root.xvii.utils.subscribeSmart
import javax.inject.Inject

abstract class BaseAttachmentsViewModel<T : Any>(protected val api: ApiService) : ViewModel() {

    protected val attachmentsLiveData = WrappedMutableLiveData<ArrayList<T>>()
    private var startFrom: String? = null

    var peerId: Int = 0
        set(value) {
            if (peerId == 0) {
                field = value
            }
        }

    abstract val mediaType: String

    abstract fun convert(attachment: Attachment?): T?

    fun getAttachments() = attachmentsLiveData as WrappedLiveData<ArrayList<T>>

    fun loadAttachments() {
        api.getHistoryAttachments(peerId, mediaType, COUNT, startFrom)
                .subscribeSmart(::onAttachmentsLoaded, { error ->
                    attachmentsLiveData.value = Wrapper(error = error)
                })
    }

    fun reset() {
        startFrom = null
    }

    private fun onAttachmentsLoaded(response: AttachmentsResponse) {
        val existing = if (startFrom.isNullOrEmpty()) {
            arrayListOf()
        } else {
            attachmentsLiveData.value?.data ?: arrayListOf()
        }
        startFrom = response.nextFrom
        val attachments = response.items.mapNotNull { convert(it.attachment) }.distinct()
        attachmentsLiveData.value = Wrapper(existing.apply { addAll(attachments) })
    }

    companion object {
        const val COUNT = 200
    }

    class Factory @Inject constructor(
            private val api: ApiService,
            private val context: Context
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel?> create(modelClass: Class<VM>): VM = when (modelClass) {
            DocAttachmentsViewModel::class.java -> DocAttachmentsViewModel(api) as VM
            LinkAttachmentsViewModel::class.java -> LinkAttachmentsViewModel(api) as VM
            VideoAttachmentsViewModel::class.java -> VideoAttachmentsViewModel(api) as VM
            PhotoAttachmentsViewModel::class.java -> PhotoAttachmentsViewModel(api) as VM
            AudioAttachmentsViewModel::class.java -> AudioAttachmentsViewModel(api, context) as VM

            else -> throw IllegalArgumentException("Unknown class $modelClass")

        }

    }
}