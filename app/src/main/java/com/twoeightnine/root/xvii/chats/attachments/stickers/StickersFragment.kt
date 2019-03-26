package com.twoeightnine.root.xvii.chats.attachments.stickers

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachViewModel
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.showError
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_attachments.*
import javax.inject.Inject

class StickersFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseAttachViewModel.Factory
    private lateinit var viewModel: StickersViewModel

    private val adapter by lazy {
        StickersAdapter(contextOrThrow, ::onClick)
    }

    override fun getLayoutId() = R.layout.fragment_attachments

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[StickersViewModel::class.java]
        initRecycler()

        viewModel.getStickers().observe(this, Observer { updateList(it) })
        viewModel.loadStickers()

        swipeRefresh.isRefreshing = true
        swipeRefresh.setOnRefreshListener {
            viewModel.loadStickers(refresh = true)
        }
    }

    private fun updateList(data: Wrapper<ArrayList<Attachment.Sticker>>) {
        swipeRefresh.isRefreshing = false
        if (data.data != null) {
            adapter.update(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun initRecycler() {
        rvAttachments.layoutManager = GridLayoutManager(context, SPAN_COUNT)
        rvAttachments.adapter = adapter
    }

    private fun onClick(sticker: Attachment.Sticker) {
        viewModel.onStickerSelected(sticker)
        selectedSubject.onNext(sticker)
    }

    companion object {

        const val SPAN_COUNT = 5

        private val disposables = CompositeDisposable()
        private val selectedSubject = PublishSubject.create<Attachment.Sticker>()

        fun newInstance(onSelected: (Attachment.Sticker) -> Unit): StickersFragment {
            selectedSubject.subscribe(onSelected).let { disposables.add(it) }
            return StickersFragment()
        }

        fun clear() {
            disposables.clear()
        }
    }
}