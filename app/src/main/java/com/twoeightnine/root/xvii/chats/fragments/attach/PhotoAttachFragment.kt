package com.twoeightnine.root.xvii.chats.fragments.attach

import android.support.design.widget.FloatingActionButton
import android.widget.GridView
import butterknife.BindView
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

class PhotoAttachFragment : BaseAttachFragment<Photo>(), SimpleAdapter.OnMultiSelected {

    companion object {
        fun newInstance(listener: ((MutableList<Attachment>) -> Unit)?): PhotoAttachFragment {
            val frag = PhotoAttachFragment()
            frag.list = listener
            return frag
        }
    }

    @BindView(R.id.gvPhotos)
    lateinit var gvPhotos: GridView
    @BindView(R.id.fabDone)
    lateinit var fabDne: FloatingActionButton

    var list: ((MutableList<Attachment>) -> Unit)? = null

    override fun getLayout() = R.layout.fragment_attachments_photo

    override fun initAdapter() {
        App.appComponent?.inject(this)
        fabDne.setOnClickListener {
            list?.invoke(
                    adapter.multiSelectRaw
                            .map { Attachment(it) }
                            .toMutableList()
            )
        }
        fabDne.hide()
        adapter = PhotoAttachmentsAdapter(
                activity,
                { load() },
                {
                    adapter.multiSelect(it)
                    adapter.notifyDataSetChanged()
                }
        )
        adapter.multiListener = this
        gvPhotos.adapter = adapter
        Style.forFAB(fabDne)
    }

    override fun onNonEmpty() {
        fabDne.show()
    }

    override fun onEmpty() {
        fabDne.hide()
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