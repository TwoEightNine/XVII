package com.twoeightnine.root.xvii.chats.fragments.attachments

import android.os.Bundle
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.music.models.Track
import com.twoeightnine.root.xvii.background.music.services.MusicService
import com.twoeightnine.root.xvii.background.music.utils.TrackManager
import com.twoeightnine.root.xvii.chats.adapters.attachments.AudioAttachmentsAdapter
import com.twoeightnine.root.xvii.model.Audio
import com.twoeightnine.root.xvii.network.response.AttachmentsResponse
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_attachments_audio.*

class AudioAttachmentsFragment : BaseAttachmentsFragment<Track>() {

    private val disposable = CompositeDisposable()
    private val audios = arrayListOf<Audio>()

    private val trackManager by lazy {
        TrackManager(context ?: throw IllegalStateException("Context leaked away!"))
    }

    override fun getLayout() = R.layout.fragment_attachments_audio

    override fun getMedia() = "audio"

    override fun initAdapter() {
        App.appComponent?.inject(this)
        adapter = AudioAttachmentsAdapter({ loadMore() }, { track ->
            val tracks = ArrayList(adapter.items)
            MusicService.launch(context, tracks, tracks.indexOf(track))
        }, { track ->
            trackManager.downloadTrack(track) {
                adapter.update(trackManager.getTracks(audios))
            }
        })
        (adapter as? AudioAttachmentsAdapter)?.played = MusicService.getPlayedTrack()
        lvAudios.adapter = adapter
    }

    override fun onLoaded(response: AttachmentsResponse) {
        audios.addAll(response.items
                .mapNotNull { it.attachment?.audio }
                .distinct())
        adapter.stopLoading(trackManager.getTracks(audios))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MusicService.subscribeOnAudioPlaying {
            (adapter as? AudioAttachmentsAdapter)?.played = it
        }.let { disposable.add(it) }
        MusicService.subscribeOnAudioPausing {
            (adapter as? AudioAttachmentsAdapter)?.played = null
        }
    }

    override fun onDestroyView() {
        disposable.dispose()
        super.onDestroyView()
    }

    companion object {

        fun newInstance(peerId: Int): AudioAttachmentsFragment {
            val farg = AudioAttachmentsFragment()
            farg.peerId = peerId
            return farg
        }
    }
}