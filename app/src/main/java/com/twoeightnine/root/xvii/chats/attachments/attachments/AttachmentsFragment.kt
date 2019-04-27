package com.twoeightnine.root.xvii.chats.attachments.attachments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.CommonPagerAdapter
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.attachments.audios.AudioAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.docs.DocAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.links.LinkAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.photos.PhotoAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.videos.VideoAttachmentsFragment
import com.twoeightnine.root.xvii.managers.Style
import kotlinx.android.synthetic.main.fragment_attachments_history.*
import kotlinx.android.synthetic.main.toolbar.*

class AttachmentsFragment : BaseFragment() {

    private val adapter by lazy {
        CommonPagerAdapter(childFragmentManager)
    }

    private val peerId by lazy { arguments?.getInt(ARG_PEER_ID) ?: 0 }

    override fun getLayoutId() = R.layout.fragment_attachments_history

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.attachments))
        Style.forToolbar(toolbar)
    }

    private fun initAdapter() {
        with(adapter) {
            add(PhotoAttachmentsFragment.newInstance(peerId), getString(R.string.photos))
            add(AudioAttachmentsFragment.newInstance(peerId), getString(R.string.audios))
            add(VideoAttachmentsFragment.newInstance(peerId), getString(R.string.videos))
            add(LinkAttachmentsFragment.newInstance(peerId), getString(R.string.links))
            add(DocAttachmentsFragment.newInstance(peerId), getString(R.string.docs))
        }
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager, true)
        Style.forTabLayout(tabs)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    companion object {
        const val ARG_PEER_ID = "peerId"

        fun newInstance(peerId: Int): AttachmentsFragment {
            val frag = AttachmentsFragment()
            frag.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return frag
        }
    }

}