package com.twoeightnine.root.xvii.chats.attachments.photos

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachFragment
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.utils.setVisible
import com.twoeightnine.root.xvii.utils.stylize
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_attachments.*

class PhotoAttachFragment : BaseAttachFragment<Photo>() {

    override val adapter by lazy {
        PhotoAttachmentsAdapter(contextOrThrow, viewModel::loadAttach) {}
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
        with(fabDone) {
            setOnClickListener {
                selectedSubject.onNext(adapter.multiSelect.map { Attachment(it) })
                adapter.clearMultiSelect()
            }
            stylize()
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