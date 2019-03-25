package com.twoeightnine.root.xvii.chats.attachments.stickers

import androidx.recyclerview.widget.GridLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachFragment
import com.twoeightnine.root.xvii.model.Attachment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class StickersFragment : BaseAttachFragment<Attachment.Sticker>() {

    override val adapter by lazy {
        StickersAdapter(contextOrThrow, ::loadMore, ::onClick)
    }

    override fun getLayoutManager() = GridLayoutManager(context, SPAN_COUNT)

    override fun inject() {
        App.appComponent?.inject(this)
    }

    private fun onClick(sticker: Attachment.Sticker) {
        selectedSubject.onNext(sticker)
    }

    override fun getViewModelClass() = StickersViewModel::class.java

    companion object {

        const val SPAN_COUNT = 5

        private val disposables = CompositeDisposable()
        private val selectedSubject = PublishSubject.create<Attachment.Sticker>()

        fun newInstance(onSelected: (Attachment.Sticker) -> Unit): StickersFragment {
            selectedSubject.subscribe(onSelected).let { disposables.add(it) }
            return StickersFragment()
        }

        fun dispose() {
            disposables.dispose()
        }
    }
}