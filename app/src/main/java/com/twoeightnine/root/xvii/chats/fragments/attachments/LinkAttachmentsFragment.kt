package com.twoeightnine.root.xvii.chats.fragments.attachments

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.adapters.attachments.LinkAttachmentsAdapter
import com.twoeightnine.root.xvii.model.Link
import com.twoeightnine.root.xvii.network.response.AttachmentsResponse
import com.twoeightnine.root.xvii.utils.simpleUrlIntent
import kotlinx.android.synthetic.main.fragment_attachments_link.*

class LinkAttachmentsFragment : BaseAttachmentsFragment<Link>() {

    override fun getLayout() = R.layout.fragment_attachments_link

    override fun getMedia() = "link"

    override fun initAdapter() {
        App.appComponent?.inject(this)
        adapter = LinkAttachmentsAdapter({ loadMore() }, { simpleUrlIntent(safeActivity, it.url) })
        adapter.setAdapter(lvLinks)
    }

    override fun onLoaded(response: AttachmentsResponse) {
        adapter.stopLoading(response.items
                .map { it.attachment?.link!! }
                .toMutableList())
    }

    companion object {

        fun newInstance(peerId: Int): LinkAttachmentsFragment {
            val frag = LinkAttachmentsFragment()
            frag.peerId = peerId
            return frag
        }
    }
}
