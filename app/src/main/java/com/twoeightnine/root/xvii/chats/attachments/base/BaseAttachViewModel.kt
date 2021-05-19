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
import com.twoeightnine.root.xvii.chats.attachments.docs.DocAttachViewModel
import com.twoeightnine.root.xvii.chats.attachments.gallery.GalleryViewModel
import com.twoeightnine.root.xvii.chats.attachments.photos.PhotoAttachViewModel
import com.twoeightnine.root.xvii.chats.attachments.videos.VideoAttachViewModel
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.network.ApiService
import javax.inject.Inject

abstract class BaseAttachViewModel<T : Any> : ViewModel() {

    protected val attachLiveData = WrappedMutableLiveData<ArrayList<T>>()

    abstract fun loadAttach(offset: Int = 0)

    fun getAttach() = attachLiveData as WrappedLiveData<ArrayList<T>>

    protected fun onAttachmentsLoaded(offset: Int, response: ArrayList<T>) {
        val existing = if (offset == 0) {
            arrayListOf()
        } else {
            attachLiveData.value?.data ?: arrayListOf()
        }

        attachLiveData.value = Wrapper(existing.apply { addAll(response) })
    }

    protected fun onErrorOccurred(error: String) {
        attachLiveData.value = Wrapper(error = error)
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
            PhotoAttachViewModel::class.java -> PhotoAttachViewModel(api) as VM
            GalleryViewModel::class.java -> GalleryViewModel(context) as VM
            DocAttachViewModel::class.java -> DocAttachViewModel(api) as VM
            VideoAttachViewModel::class.java -> VideoAttachViewModel(api) as VM

            else -> throw IllegalArgumentException("Unknown class $modelClass")

        }
    }
}