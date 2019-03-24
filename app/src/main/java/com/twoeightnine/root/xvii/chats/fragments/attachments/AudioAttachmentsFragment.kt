package com.twoeightnine.root.xvii.chats.fragments.attachments

import android.os.Bundle
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.music.MusicService
import com.twoeightnine.root.xvii.chats.adapters.attachments.AudioAttachmentsAdapter
import com.twoeightnine.root.xvii.model.Audio
import com.twoeightnine.root.xvii.network.response.AttachmentsResponse
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_attachments_audio.*

class AudioAttachmentsFragment : BaseAttachmentsFragment<Audio>() {

    private val disposable = CompositeDisposable()

    override fun getLayout() = R.layout.fragment_attachments_audio

    override fun getMedia() = "audio"

    override fun initAdapter() {
        App.appComponent?.inject(this)
        adapter = AudioAttachmentsAdapter({ loadMore() }, {
            val audios = ArrayList(adapter.items)
            MusicService.launch(context, audios, audios.indexOf(it))
        })
        lvAudios.adapter = adapter
    }

    override fun onLoaded(response: AttachmentsResponse) {
        adapter.stopLoading(response.items
                .mapNotNull { it.attachment?.audio }
                .toMutableList())
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