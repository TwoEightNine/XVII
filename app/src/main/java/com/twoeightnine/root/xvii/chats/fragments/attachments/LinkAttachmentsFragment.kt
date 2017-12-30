package com.twoeightnine.root.xvii.chats.fragments.attachments

import android.widget.ListView
import butterknife.BindView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.adapters.attachments.LinkAttachmentsAdapter
import com.twoeightnine.root.xvii.model.Link
import com.twoeightnine.root.xvii.response.AttachmentsResponse
import com.twoeightnine.root.xvii.utils.simpleUrlIntent

class LinkAttachmentsFragment : BaseAttachmentsFragment<Link>() {

    @BindView(R.id.lvLinks)
    lateinit var lvLinks: ListView

    override fun getLayout() = R.layout.fragment_attachments_link

    override fun getMedia() = "link"

    override fun initAdapter() {
        App.appComponent?.inject(this)
        adapter = LinkAttachmentsAdapter({ loadMore() }, { simpleUrlIntent(activity, it.url) })
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
