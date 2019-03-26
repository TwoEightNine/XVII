package com.twoeightnine.root.xvii.chats.attachments.videos

import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachFragment
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.Video
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class VideoAttachFragment : BaseAttachFragment<Video>() {

    override val adapter by lazy {
        VideoAttachmentsAdapter(contextOrThrow, ::loadMore, ::onClick)
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