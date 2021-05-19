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

package com.twoeightnine.root.xvii.chats.attachments.docs

import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachFragment
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Doc
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class DocAttachFragment : BaseAttachFragment<Doc>() {

    override val adapter by lazy {
        DocAttachmentsAdapter(requireContext(), ::loadMore, ::onClick)
    }

    override fun getLayoutManager() = LinearLayoutManager(context)

    override fun inject() {
        App.appComponent?.inject(this)
    }

    private fun onClick(doc: Doc) {
        selectedSubject.onNext(arrayListOf(Attachment(doc)))
    }

    override fun getViewModelClass() = DocAttachViewModel::class.java

    companion object {

        private val disposables = CompositeDisposable()
        private val selectedSubject = PublishSubject.create<List<Attachment>>()

        fun newInstance(onSelected: (List<Attachment>) -> Unit): DocAttachFragment {
            selectedSubject.subscribe(onSelected).let { disposables.add(it) }
            return DocAttachFragment()
        }

        fun clear() {
            disposables.clear()
        }
    }

}