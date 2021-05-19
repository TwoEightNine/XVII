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

package com.twoeightnine.root.xvii.chats.attachments.photos

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsFragment
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity

class PhotoAttachmentsFragment : BaseAttachmentsFragment<Photo>() {

    override val adapter by lazy {
        PhotoAttachmentsAdapter(requireContext(), ::loadMore, ::onClick)
    }

    override fun getLayoutManager() = GridLayoutManager(context, SPAN_COUNT)

    override fun getViewModelClass() = PhotoAttachmentsViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    private fun onClick(photo: Photo) {
        val photos = ArrayList(adapter.items)
        val position = photos.indexOf(photo)
        val fromPos = if (position > 20) position - 20 else 0
        val toPos = if (position < photos.size - 20) position + 20 else photos.size
        ImageViewerActivity.viewImages(context, ArrayList(photos.subList(fromPos, toPos)), position - fromPos)
    }

    companion object {

        const val SPAN_COUNT = 4

        fun newInstance(peerId: Int): PhotoAttachmentsFragment {
            val fragment = PhotoAttachmentsFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}