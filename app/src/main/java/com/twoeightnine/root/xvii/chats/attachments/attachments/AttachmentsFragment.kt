/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.chats.attachments.attachments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.attachments.audios.AudioAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.docs.DocAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.links.LinkAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.photos.PhotoAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.videos.VideoAttachmentsFragment
import global.msnthrp.xvii.uikit.base.adapters.BasePagerAdapter
import kotlinx.android.synthetic.main.fragment_attachments_history.*

class AttachmentsFragment : BaseFragment() {

    private val adapter by lazy {
        BasePagerAdapter(childFragmentManager)
    }

    private val peerId by lazy { arguments?.getInt(ARG_PEER_ID) ?: 0 }

    override fun getLayoutId() = R.layout.fragment_attachments_history

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
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
        xviiToolbar.setupWith(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    companion object {
        private const val ARG_PEER_ID = "peerId"

        fun createArgs(peerId: Int) = Bundle().apply {
            putInt(ARG_PEER_ID, peerId)
        }
    }

}