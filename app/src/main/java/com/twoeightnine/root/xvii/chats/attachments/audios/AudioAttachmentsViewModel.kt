package com.twoeightnine.root.xvii.chats.attachments.audios

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twoeightnine.root.xvii.background.music.models.Track
import com.twoeightnine.root.xvii.background.music.services.MusicService
import com.twoeightnine.root.xvii.background.music.utils.TrackManager
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsViewModel
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.network.ApiService

class AudioAttachmentsViewModel(
        api: ApiService,
        private val context: Context
) : BaseAttachmentsViewModel<Track>(api) {

    private val trackManager by lazy { TrackManager(context) }
    private val playedTrackLiveData = MutableLiveData<Track?>()

    init {
        MusicService.subscribeOnAudioPlaying { track ->
            playedTrackLiveData.value = track
        }
        MusicService.subscribeOnAudioPausing {
            playedTrackLiveData.value = null
        }
    }

    fun getPlayedTrack() = playedTrackLiveData as LiveData<Track?>

    override val mediaType = "audio"

    override fun convert(attachment: Attachment?) = attachment?.audio?.let { trackManager.getTrack(it) }

    fun download(track: Track) {
        trackManager.downloadTrack(track) { loadedTrack ->
            updateTrack(loadedTrack, loadedTrack.cachePath)
        }
    }

    fun removeFromCache(track: Track) {
        if (!track.isCached()) return

        trackManager.removeTrack(track)
        updateTrack(track, null)
    }

    private fun updateTrack(track: Track, cachePath: String?) {
        val tracks = attachmentsLiveData.value?.data ?: return

        val pos = tracks.indexOf(track)
        tracks[pos] = Track(track.audio, cachePath)
        attachmentsLiveData.value = Wrapper(tracks)
    }
}