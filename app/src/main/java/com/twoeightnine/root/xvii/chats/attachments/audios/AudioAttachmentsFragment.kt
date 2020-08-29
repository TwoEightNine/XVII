package com.twoeightnine.root.xvii.chats.attachments.audios

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.background.music.models.Track
import com.twoeightnine.root.xvii.background.music.services.MusicService
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsFragment
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.utils.equalsDevUids
import com.twoeightnine.root.xvii.utils.showDeleteDialog

class AudioAttachmentsFragment : BaseAttachmentsFragment<Track>() {

    private val audioViewModel: AudioAttachmentsViewModel
        get() = viewModel as AudioAttachmentsViewModel

    override val adapter by lazy {
        AudioAttachmentsAdapter(contextOrThrow, ::loadMore, ::onClick,
                ::onLongClick, audioViewModel::download, equalsDevUids(Session.uid))
    }

    override fun getLayoutManager() = LinearLayoutManager(context)

    override fun getViewModelClass() = AudioAttachmentsViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioViewModel.getPlayedTrack().observe(this, Observer { adapter.played = it })
        if (MusicService.isPlaying()) {
            adapter.played = MusicService.getPlayedTrack()
        }
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