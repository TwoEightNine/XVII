package com.twoeightnine.root.xvii.chats.fragments.attachments

import android.support.design.widget.FloatingActionButton
import android.view.View
import android.widget.GridView
import butterknife.BindView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.adapters.attachments.PhotoAttachmentsAdapter
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.response.AttachmentsResponse
import com.twoeightnine.root.xvii.utils.ApiUtils

class PhotoAttachmentsFragment : BaseAttachmentsFragment<Photo>() {

    @BindView(R.id.gvPhotos)
    lateinit var gvPhotos: GridView
    @BindView(R.id.fabDone)
    lateinit var fabDone: FloatingActionButton

    override fun initAdapter() {
        fabDone.visibility = View.GONE
        App.appComponent?.inject(this)
        adapter = PhotoAttachmentsAdapter(
                context,
                { loadMore() },
                { ApiUtils().showPhoto(activity, it.photoId, it.accessKey) }
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
