package com.twoeightnine.root.xvii.background.music

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import androidx.core.app.NotificationCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.model.Audio
import io.reactivex.subjects.PublishSubject


class MusicService : Service(), MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private val binder by lazy { MusicBinder() }
    private val player by lazy { MediaPlayer() }
    private val audios = arrayListOf<Audio>()

    private var playedPosition: Int = 0

    fun updateAudios(audios: ArrayList<Audio>, position: Int = 0) {
        this.audios.clear()
        this.audios.addAll(audios)
        playedPosition = position
        startPlaying()
    }

    override fun onCreate() {
        super.onCreate()
        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
    }

    private fun startPlaying() {
        val playedAudio = getPlayedAudio()
        val url = playedAudio?.url ?: return

        l("playing ${playedAudio.id}_${playedAudio.ownerId}")
        player.reset()
        try {
            player.setDataSource(url)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        player.prepareAsync()
    }

    private fun playNext() {
        onCompletion(player)
    }

    private fun playPrevious() {
        playedPosition -= 2
        onCompletion(player)
    }

    private fun playOrPause() {
        if (player.isPlaying) {
            player.pause()
            pausingAudioSubject.onNext(Unit)
        } else {
            player.start()
            playingAudioSubject.onNext(getPlayedAudio() ?: return)
        }
        showNotification()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        player.start()
        showNotification()
        playingAudioSubject.onNext(getPlayedAudio() ?: return)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        playedPosition++
        if (playedPosition !in audios.indices) {
            playedPosition = 0
        }
        startPlaying()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        lw("onError what=$what extra=$extra")
        return false // call OnCompletion
    }

    private fun getPlayedAudio() = audios.getOrNull(playedPosition)

    private fun showNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        initChannel(notificationManager)
        val audio = getPlayedAudio() ?: return

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setCustomContentView(bindRemoteViews(R.layout.view_music_notification, audio))
                .setCustomBigContentView(bindRemoteViews(R.layout.view_music_notification_extended, audio))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_play_music)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
        startForeground(FOREGROUND_ID, notification)
    }

    private fun initChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.app_name)
            val descriptionText = applicationContext.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = descriptionText
            channel.setSound(null, null)

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun bindRemoteViews(@LayoutRes viewId: Int, audio: Audio): RemoteViews {
        val remoteViews = RemoteViews(packageName, viewId)
        with(remoteViews) {
            setTextViewText(R.id.tvTitle, audio.title)
            setTextViewText(R.id.tvArtist, audio.artist)
            val playPauseRes = if (player.isPlaying) R.drawable.ic_pause_music else R.drawable.ic_play_music
            setImageViewResource(R.id.ivPlayPause, playPauseRes)
            setOnClickPendingIntent(R.id.ivNext, getActionPendingIntent(MusicBroadcastReceiver.ACTION_NEXT))
            setOnClickPendingIntent(R.id.ivPrevious, getActionPendingIntent(MusicBroadcastReceiver.ACTION_PREVIOUS))
            setOnClickPendingIntent(R.id.ivPlayPause, getActionPendingIntent(MusicBroadcastReceiver.ACTION_PLAY_PAUSE))
        }
        return remoteViews
    }

    private fun getActionPendingIntent(action: String) = PendingIntent.getBroadcast(
            applicationContext, 0,
            Intent(applicationContext, MusicBroadcastReceiver::class.java).apply {
                this.action = action
            },
            PendingIntent.FLAG_UPDATE_CURRENT
    )

    override fun onBind(intent: Intent?) = binder

    override fun onUnbind(intent: Intent?): Boolean {
        player.stop()
        player.release()
        return false
    }

    private fun l(s: String) {
        Lg.i("[music] $s")
    }

    private fun lw(s: String) {
        Lg.wtf("[music] $s")
    }

    companion object {

        private const val FOREGROUND_ID = 3676
        private const val CHANNEL_ID = "xvii.music"

        private var service: MusicService? = null

        private val playingAudioSubject = PublishSubject.create<Audio>()
        private val pausingAudioSubject = PublishSubject.create<Unit>()

        var isBound = false
            private set

        fun launch(
                context: Context?,
                audios: ArrayList<Audio>,
                position: Int
        ) {
            context ?: return

            val serviceConnection = object : ServiceConnection {

                override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
                    val binder = iBinder as MusicBinder
                    binder.getService().also {
                        service = it
                        it.updateAudios(audios, position)
                    }
                    isBound = true
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    isBound = false
                }
            }
            val intent = Intent(context, MusicService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            context.startService(intent)
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

        fun subscribeOnAudioPlaying(consumer: (Audio?) -> Unit) = playingAudioSubject.subscribe(consumer)

        fun subscribeOnAudioPausing(consumer: (Unit) -> Unit) = pausingAudioSubject.subscribe(consumer)
    }

    inner class MusicBinder : Binder() {

        fun getService() = this@MusicService
    }
}