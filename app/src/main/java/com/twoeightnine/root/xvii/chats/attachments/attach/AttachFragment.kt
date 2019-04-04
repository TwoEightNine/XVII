package com.twoeightnine.root.xvii.chats.attachments.attach

import android.os.Bundle
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.CommonPagerAdapter
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.attachments.docs.DocAttachFragment
import com.twoeightnine.root.xvii.chats.attachments.gallery.GalleryFragment
import com.twoeightnine.root.xvii.chats.attachments.photos.PhotoAttachFragment
import com.twoeightnine.root.xvii.chats.attachments.videos.VideoAttachFragment
import com.twoeightnine.root.xvii.model.Attachment
import kotlinx.android.synthetic.main.fragment_attach.*

class AttachFragment : BaseFragment() {

    private val adapter by lazy {
        CommonPagerAdapter(childFragmentManager)
    }

    override fun getLayoutId() = R.layout.fragment_attach

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(adapter) {
            add(GalleryFragment.newInstance(::onImagesSelected), getString(R.string.device_photos))
            add(PhotoAttachFragment.newInstance(::onAttachmentsSelected), getString(R.string.photos))
            add(VideoAttachFragment.newInstance(::onAttachmentsSelected), getString(R.string.videos))
            add(DocAttachFragment.newInstance(::onAttachmentsSelected), getString(R.string.docs))
            vpAttach.adapter = this
        }
        tabs.setupWithViewPager(vpAttach, true)
    }

    private fun onImagesSelected(paths: List<String>) {

    }

    private fun onAttachmentsSelected(attachments: List<Attachment>) {

    }

    companion object {
        fun newInstance() = AttachFragment()
    }
}