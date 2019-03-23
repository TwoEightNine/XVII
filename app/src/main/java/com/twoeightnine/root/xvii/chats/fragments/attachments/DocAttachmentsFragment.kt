package com.twoeightnine.root.xvii.chats.fragments.attachments

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.adapters.attachments.DocAttachmentsAdapter
import com.twoeightnine.root.xvii.model.Doc
import com.twoeightnine.root.xvii.network.response.AttachmentsResponse
import com.twoeightnine.root.xvii.utils.simpleUrlIntent
import kotlinx.android.synthetic.main.fragment_attachments_doc.*

class DocAttachmentsFragment : BaseAttachmentsFragment<Doc>() {

    override fun getLayout() = R.layout.fragment_attachments_doc

    override fun getMedia() = "doc"

    override fun initAdapter() {
        App.appComponent?.inject(this)
        adapter = DocAttachmentsAdapter({ loadMore() }, { simpleUrlIntent(safeActivity, it.url ?: "") })
        lvDocs.adapter = adapter
    }

    override fun onLoaded(response: AttachmentsResponse) {
        adapter.stopLoading(response.items
                .map { it.attachment?.doc!! }
                .toMutableList())
    }

    companion object {

        fun newInstance(peerId: Int): DocAttachmentsFragment {
            val farg = DocAttachmentsFragment()
            farg.peerId = peerId
            return farg
        }
    }
}
