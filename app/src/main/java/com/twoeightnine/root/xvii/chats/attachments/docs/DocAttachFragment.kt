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
        DocAttachmentsAdapter(contextOrThrow, ::loadMore, ::onClick)
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