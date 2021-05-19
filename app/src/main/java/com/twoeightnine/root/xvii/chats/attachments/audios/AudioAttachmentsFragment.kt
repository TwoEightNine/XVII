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

package com.twoeightnine.root.xvii.chats.attachments.audios

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.background.music.models.Track
import com.twoeightnine.root.xvii.background.music.services.MusicService
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsFragment
import com.twoeightnine.root.xvii.storage.SessionProvider
import com.twoeightnine.root.xvii.utils.showDeleteDialog

class AudioAttachmentsFragment : BaseAttachmentsFragment<Track>() {

    private val audioViewModel: AudioAttachmentsViewModel
        get() = viewModel as AudioAttachmentsViewModel

    override val adapter by lazy {
        AudioAttachmentsAdapter(requireContext(), ::loadMore, ::onClick,
                ::onLongClick, audioViewModel::download, SessionProvider.isDevUserId())
    }

    override fun getLayoutManager() = LinearLayoutManager(context)

    override fun getViewModelClass() = AudioAttachmentsViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (MusicService.isPlaying()) {
            adapter.played = MusicService.getPlayedTrack()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        audioViewModel.getPlayedTrack().observe(viewLifecycleOwner) { adapter.played = it }
    }

    private fun onClick(track: Track) {
        val tracks = ArrayList(adapter.items)
        MusicService.launch(context?.applicationContext, tracks, tracks.indexOf(track))
    }

    private fun onLongClick(track: Track) {
        if (!track.isCached()) return

        showDeleteDialog(context, "") { // not in production
            audioViewModel.removeFromCache(track)
        }
    }

    companion object {
        fun newInstance(peerId: Int): AudioAttachmentsFragment {
            val fragment = AudioAttachmentsFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}