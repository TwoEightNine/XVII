package com.twoeightnine.root.xvii.background.music.services

import android.annotation.TargetApi
import android.app.Service
import android.content.*
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import com.twoeightnine.root.xvii.background.music.models.Track
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.utils.notifications.NotificationUtils
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject


class MusicService : Service(), MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {

    private val binder by lazy { MusicBinder() }
    private val player by lazy { MediaPlayer() }
    private val tracks = arrayListOf<Track>()

    private val noisyReceiver = NoisyReceiver()
    private var receiverRegistered = false

    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private val handler = Handler()
    private val focusLock = Any()
    private var isPlaybackDelayed = false
    private var focusRequest: AudioFocusRequest? = null

    private var playedPosition: Int = 0

    private var playbackSpeed = 1f

    override fun onCreate() {
        super.onCreate()
        player.apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener(this@MusicService)
            setOnCompletionListener(this@MusicService)
            setOnErrorListener(this@MusicService)
        }
    }

    private fun updateAudios(tracks: ArrayList<Track>, position: Int = 0) {
        val nowPlayed = getPlayedTrack()
        this.tracks.clear()
        this.tracks.addAll(tracks)
        playedPosition = position
        if (nowPlayed == getPlayedTrack()) {
            playOrPause()
        } else {
            requestFocus()
        }
    }

    override fun onDestroy() {
        try {
            pausingAudioSubject.onNext(Unit)
            player.release()
            unregisterNoisyReceiver()
        } catch (e: Exception) {
            lw("destroying", e)
        }
        super.onDestroy()
    }

    private fun startPlaying() {
        val playedTrack = getPlayedTrack()
        val path = when {
            playedTrack == null -> null
            playedTrack.isCached() -> playedTrack.cachePath
            else -> playedTrack.audio.url
        } ?: return

        l("playing ${playedTrack?.audio?.fullId} when cached is ${playedTrack?.isCached() == true}")
        try {
            player.reset()
            player.setDataSource(path)
            updateSpeed(showNotification = false)
            player.prepareAsync()
            registerNoisyReceiver()
        } catch (e: Exception) {
            lw("preparing error", e)
        }
    }

    private fun requestFocus() {
        if (Build.VERSION.SDK_INT >= 26) {
            val focusRequest = createFocusRequest()
            this.focusRequest = focusRequest

            val result = audioManager.requestAudioFocus(focusRequest)
            synchronized(focusLock) {
                when (result) {
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                        startPlaying()
                    }
                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                        isPlaybackDelayed = true
                    }
                }
            }
        } else {
            val result: Int = audioManager.requestAudioFocus(
                    this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
            )
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                startPlaying()
            }
        }
    }

    private fun toggleSpeed() {
        playbackSpeed = when (playbackSpeed) {
            1f -> 1.25f
            1.25f -> 1.5f
            1.5f -> 2f
            2f -> 0.5f
            else -> 1f
        }
        updateSpeed()
    }

    private fun updateSpeed(showNotification: Boolean = true) {
        try {
            player.playbackParams = player.playbackParams.setSpeed(playbackSpeed)
        } catch (e: Exception) {
            lw("unable to update speed", e)
        }
        if (showNotification) {
            showNotification()
        }
    }

    private fun playNext() {
        onCompletion(player)
    }

    private fun playPrevious() {
        playedPosition -= 2
        onCompletion(player)
    }

    private fun playOrPause() {
        if (player.isPlayingSafe()) {
            pause()
        } else {
            try {
                player.start()
                playingAudioSubject.onNext(getPlayedTrack() ?: return)
            } catch (e: Exception) {
                lw("playing error", e)
                startPlaying()
            }
        }
        showNotification()
    }

    private fun pause() {
        if (!player.isPlayingSafe()) return
        try {
            player.pause()
            pausingAudioSubject.onNext(Unit)
        } catch (e: Exception) {
            lw("error pausing", e)
            stop()
        }
    }

    private fun stop() {
        try {
            player.stop()
            tracks.clear()
            pausingAudioSubject.onNext(Unit)
            if (Build.VERSION.SDK_INT >= 26) {
                focusRequest?.let(audioManager::abandonAudioFocusRequest)
            } else {
                audioManager.abandonAudioFocus(this)
            }
            unregisterNoisyReceiver()
        } catch (e: Exception) {
            lw("error stopping", e)
            stopForeground(true)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                l("audiofocus gain")
                if (isPlaybackDelayed) {
                    synchronized(focusLock) {
                        isPlaybackDelayed = false
                    }
                    startPlaying()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                l("audiofocus loss")
                synchronized(focusLock) {
                    isPlaybackDelayed = false
                }
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                l("audiofocus transient gain")
                synchronized(focusLock) {
                    isPlaybackDelayed = false
                }
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                l("audiofocus transient loss can duck")
                pause()
            }
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        player.start()
        showNotification()
        playingAudioSubject.onNext(getPlayedTrack() ?: return)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        playedPosition++
        if (playedPosition !in tracks.indices) {
            playedPosition = 0
            stop()
            stopForeground(true)
        } else {
            startPlaying()
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        lw("onError what=$what extra=$extra")
        return false // call OnCompletion
    }

    private fun getPlayedTrack() = tracks.getOrNull(playedPosition)

    private fun showNotification() {
        val audio = getPlayedTrack()?.audio ?: return
        NotificationUtils.showMusicNotification(audio, this, player.isPlayingSafe(), playbackSpeed)
    }

    @TargetApi(26)
    private fun createFocusRequest() =
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .run {
                        setAudioAttributes(AudioAttributes.Builder().run {
                            setUsage(AudioAttributes.USAGE_MEDIA)
                            setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            build()
                        })
                        setAcceptsDelayedFocusGain(true)
                        setWillPauseWhenDucked(true)
                        setOnAudioFocusChangeListener(this@MusicService, handler)
                        build()
                    }

    override fun onBind(intent: Intent?) = binder

    override fun onUnbind(intent: Intent?): Boolean {
        player.release()
        return false
    }

    private fun registerNoisyReceiver() = synchronized(this) {
        if (!receiverRegistered) {
            registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            receiverRegistered = true
        }
    }

    private fun unregisterNoisyReceiver() = synchronized(this) {
        if (receiverRegistered) {
            unregisterReceiver(noisyReceiver)
            receiverRegistered = false
        }
    }

    private fun MediaPlayer.isPlayingSafe() = try {
        isPlaying
    } catch (e: Exception) {
        lw("isPlaying", e)
        false
    }

    private fun l(s: String) {
        L.tag(TAG).log(s)
    }

    private fun lw(s: String, throwable: Throwable? = null) {
        L.tag(TAG).throwable(throwable).log(s)
    }

    /**
     * also provides public API
     */
    companion object {

        private const val TAG = "music"

        private var service: MusicService? = null

        private val playingAudioSubject = PublishSubject.create<Track>()
        private val pausingAudioSubject = PublishSubject.create<Unit>()

        var isBound = false
            private set

        fun launch(
                applicationContext: Context?,
                tracks: ArrayList<Track>,
                position: Int
        ) {
            applicationContext ?: return

            val serviceConnection = object : ServiceConnection {

                override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
                    val binder = iBinder as MusicBinder
                    binder.getService().also {
                        service = it
                        it.updateAudios(tracks, position)
                    }
                    isBound = true
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    isBound = false
                }
            }
            val intent = Intent(applicationContext, MusicService::class.java)
            applicationContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            applicationContext.startService(intent)
        }

        fun exit() {
            service?.stop()
            service?.stopForeground(true)
        }

        fun next() {
            service?.playNext()
        }

        fun previous() {
            service?.playPrevious()
        }

        fun playPause() {
            service?.playOrPause()
        }

        fun toggleSpeed() {
            service?.toggleSpeed()
        }

        fun subscribeOnAudioPlaying(consumer: (Track) -> Unit): Disposable = playingAudioSubject.subscribe(consumer)

        fun subscribeOnAudioPausing(consumer: (Unit) -> Unit): Disposable = pausingAudioSubject.subscribe(consumer)

        fun getPlayedTrack() = service?.getPlayedTrack()

        fun isPlaying() = try {
            service?.player?.isPlaying ?: false
        } catch (e: Exception) {
            false
        }
    }

    inner class MusicBinder : Binder() {

        fun getService() = this@MusicService
    }

    private inner class NoisyReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                l("becoming noisy")
                pause()
            }
        }
    }
}