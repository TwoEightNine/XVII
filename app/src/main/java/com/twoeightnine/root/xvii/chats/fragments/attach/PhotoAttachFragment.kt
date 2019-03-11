package com.twoeightnine.root.xvii.chats.fragments.attach

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimpleAdapter
import com.twoeightnine.root.xvii.chats.adapters.attachments.PhotoAttachmentsAdapter
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.utils.subscribeSmart
import kotlinx.android.synthetic.main.fragment_attachments_photo.*
import java.lang.Exception

class PhotoAttachFragment : BaseAttachFragment<Photo>(), SimpleAdapter.OnMultiSelected {

    companion object {
        fun newInstance(listener: ((MutableList<Attachment>) -> Unit)?): PhotoAttachFragment {
            val frag = PhotoAttachFragment()
            frag.list = listener
            return frag
        }
    }

    var list: ((MutableList<Attachment>) -> Unit)? = null

    override fun getLayout() = R.layout.fragment_attachments_photo

    override fun initAdapter() {
        App.appComponent?.inject(this)
        fabDone.setOnClickListener {
            list?.invoke(
                    adapter.multiSelectRaw
                            .map { Attachment(it) }
                            .toMutableList()
            )
            adapter.clearMultiSelect()
        }
        fabDone.hide()
        adapter = PhotoAttachmentsAdapter(
                safeActivity,
                { load() },
                {
                    adapter.multiSelect(it)
                    adapter.notifyDataSetChanged()
                }
        )
        adapter.multiListener = this
        gvPhotos.adapter = adapter
        Style.forFAB(fabDone)
    }

    override fun onNonEmpty() {
        fabDone.show()
    }

    override fun onEmpty() {
        fabDone.hide()
    }

    fun load() {
        api.getPhotos(Session.uid, "saved", count, adapter.count)
                .subscribeSmart({
                    response ->
                    adapter.stopLoading(response.items)
                }, {
                    error ->
                    showError(activity, error)
                    adapter.isLoading = false
                })
    }
}