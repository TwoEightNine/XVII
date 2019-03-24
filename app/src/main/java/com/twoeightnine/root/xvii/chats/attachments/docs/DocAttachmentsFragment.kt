package com.twoeightnine.root.xvii.chats.attachments.docs

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.activities.GifViewerActivity
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsFragment
import com.twoeightnine.root.xvii.model.Doc
import com.twoeightnine.root.xvii.utils.simpleUrlIntent

class DocAttachmentsFragment : BaseAttachmentsFragment<Doc>() {

    override val adapter by lazy {
        DocAttachmentsAdapter(contextOrThrow, ::loadMore, ::onClick)
    }

    override fun getLayoutManager() = LinearLayoutManager(context)

    override fun inject() {
        App.appComponent?.inject(this)
    }

    private fun onClick(doc: Doc) {
        if (doc.isGif) {
            GifViewerActivity.showGif(context, doc)
        } else {
            simpleUrlIntent(context, doc.url)
        }
    }

    override fun getViewModelClass() = DocAttachmentsViewModel::class.java

    companion object {
        fun newInstance(peerId: Int): DocAttachmentsFragment {
            val fragment = DocAttachmentsFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}