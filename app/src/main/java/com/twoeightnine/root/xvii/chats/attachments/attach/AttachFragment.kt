package com.twoeightnine.root.xvii.chats.attachments.attach

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.CommonPagerAdapter
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.attachments.docs.DocAttachFragment
import com.twoeightnine.root.xvii.chats.attachments.gallery.GalleryFragment
import com.twoeightnine.root.xvii.chats.attachments.photos.PhotoAttachFragment
import com.twoeightnine.root.xvii.chats.attachments.videos.VideoAttachFragment
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.attachments.Attachment
import kotlinx.android.synthetic.main.fragment_attach.*
import kotlinx.android.synthetic.main.toolbar.*

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
        Style.forTabLayout(tabs)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Style.forToolbar(toolbar)
        updateTitle(getString(R.string.attach))
    }

    override fun getHomeAsUpIcon() = R.drawable.ic_back

    private fun onImagesSelected(paths: List<String>) {
        val intent = Intent().apply {
            putStringArrayListExtra(ARG_PATHS, ArrayList(paths))
        }
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    private fun onAttachmentsSelected(attachments: List<Attachment>) {
        val intent = Intent().apply {
            putParcelableArrayListExtra(ARG_ATTACHMENTS, ArrayList(attachments))
        }
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    companion object {
        const val ARG_ATTACHMENTS = "attachments"
        const val ARG_PATHS = "paths"

        fun newInstance() = AttachFragment()
    }
}