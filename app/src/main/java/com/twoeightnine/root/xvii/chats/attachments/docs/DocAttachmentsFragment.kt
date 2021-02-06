package com.twoeightnine.root.xvii.chats.attachments.docs

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsFragment
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.utils.BrowsingUtils
import com.twoeightnine.root.xvii.web.GifViewerFragment

class DocAttachmentsFragment : BaseAttachmentsFragment<Doc>() {

    override val adapter by lazy {
        DocAttachmentsAdapter(requireContext(), ::loadMore, ::onClick)
    }

    override fun getLayoutManager() = LinearLayoutManager(context)

    override fun inject() {
        App.appComponent?.inject(this)
    }

    private fun onClick(doc: Doc) {
        if (doc.isGif) {
            startFragment<GifViewerFragment>(GifViewerFragment.createArgs(doc))
        } else {
            BrowsingUtils.openUrl(context, doc.url)
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