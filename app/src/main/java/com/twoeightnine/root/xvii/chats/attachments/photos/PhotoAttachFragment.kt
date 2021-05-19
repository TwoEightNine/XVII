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

package com.twoeightnine.root.xvii.chats.attachments.photos

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachFragment
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Photo
import global.msnthrp.xvii.uikit.extensions.setVisible
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_attachments.*

class PhotoAttachFragment : BaseAttachFragment<Photo>() {

    override val adapter by lazy {
        PhotoAttachmentsAdapter(requireContext(), viewModel::loadAttach) {}
    }

    override fun getLayoutManager() = GridLayoutManager(context, SPAN_COUNT)

    override fun getViewModelClass() = PhotoAttachViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.multiSelectMode = true
        adapter.multiListener = fabDone::setVisible
        fabDone.setOnClickListener {
            selectedSubject.onNext(adapter.multiSelect.map { Attachment(it) })
            adapter.clearMultiSelect()
        }
    }

    companion object {
        const val SPAN_COUNT = 4

        private val disposables = CompositeDisposable()
        private val selectedSubject = PublishSubject.create<List<Attachment>>()

        fun newInstance(onSelected: (List<Attachment>) -> Unit): PhotoAttachFragment {
            selectedSubject.subscribe(onSelected).let { disposables.add(it) }
            return PhotoAttachFragment()
        }

        fun clear() {
            disposables.clear()
        }
    }
}