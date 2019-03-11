package com.twoeightnine.root.xvii.chats.fragments.attachments

import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.adapters.attachments.PhotoAttachmentsAdapter
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.response.AttachmentsResponse
import com.twoeightnine.root.xvii.utils.ApiUtils
import kotlinx.android.synthetic.main.fragment_attachments_photo.*

class PhotoAttachmentsFragment : BaseAttachmentsFragment<Photo>() {

    override fun initAdapter() {
        fabDone.visibility = View.GONE
        App.appComponent?.inject(this)
        adapter = PhotoAttachmentsAdapter(
                safeActivity,
                { loadMore() },
                { apiUtils.showPhoto(safeActivity, it.photoId, it.accessKey) }
        )
        adapter.setAdapter(gvPhotos)
    }

    override fun getLayout() = R.layout.fragment_attachments_photo

    override fun getMedia() = "photo"

    override fun onLoaded(response: AttachmentsResponse) {
        adapter.stopLoading(response.items
                .map { it.attachment?.photo!! }
                .toMutableList())
    }

    companion object {

        fun newInstance(peerId: Int): PhotoAttachmentsFragment {
            val frag = PhotoAttachmentsFragment()
            frag.peerId = peerId
            return frag
        }
    }
}
