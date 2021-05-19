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

package com.twoeightnine.root.xvii.chats.attachments.videos

import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachFragment
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Video
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class VideoAttachFragment : BaseAttachFragment<Video>() {

    override val adapter by lazy {
        VideoAttachmentsAdapter(requireContext(), ::loadMore, ::onClick)
    }

    override fun getLayoutManager() = LinearLayoutManager(context)

    override fun getViewModelClass() = VideoAttachViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    private fun onClick(video: Video) {
        selectedSubject.onNext(arrayListOf(Attachment(video)))
    }

    companion object {
        private val disposables = CompositeDisposable()
        private val selectedSubject = PublishSubject.create<List<Attachment>>()

        fun newInstance(onSelected: (List<Attachment>) -> Unit): VideoAttachFragment {
            selectedSubject.subscribe(onSelected).let { disposables.add(it) }
            return VideoAttachFragment()
        }

        fun clear() {
            disposables.clear()
        }
    }

}