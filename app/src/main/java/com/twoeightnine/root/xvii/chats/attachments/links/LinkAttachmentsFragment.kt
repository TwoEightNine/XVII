package com.twoeightnine.root.xvii.chats.attachments.links

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsFragment
import com.twoeightnine.root.xvii.model.attachments.Link
import com.twoeightnine.root.xvii.utils.BrowsingUtils

class LinkAttachmentsFragment : BaseAttachmentsFragment<Link>() {

    override val adapter by lazy {
        LinkAttachmentsAdapter(requireContext(), ::loadMore, ::onClick)
    }

    override fun getLayoutManager() = LinearLayoutManager(context)

    override fun getViewModelClass() = LinkAttachmentsViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    private fun onClick(link: Link) {
        BrowsingUtils.openUrl(context, link.url)
    }

    companion object {
        fun newInstance(peerId: Int): LinkAttachmentsFragment {
            val fragment = LinkAttachmentsFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}
