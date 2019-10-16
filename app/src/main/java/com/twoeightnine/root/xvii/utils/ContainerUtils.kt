package com.twoeightnine.root.xvii.utils

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.music.models.Track
import com.twoeightnine.root.xvii.background.music.services.MusicService
import com.twoeightnine.root.xvii.model.attachments.*
import com.twoeightnine.root.xvii.poll.PollsActivity
import com.twoeightnine.root.xvii.web.GifViewerActivity


fun getPhoto(photo: Photo, context: Context, onClick: (Photo) -> Unit = {}): View {
    val view = LayoutInflater.from(context).inflate(R.layout.container_photo, null, false)
    XviiPicasso.get()
            .loadRounded(photo.getOptimalPhoto().url)
            .resize(pxFromDp(context, 250), pxFromDp(context, 300))
            .centerCrop()
            .into(view.findViewById<ImageView>(R.id.ivInternal))
    view.setOnClickListener { onClick.invoke(photo) }
    return view
}

fun getPhotoWall(photo: Photo, activity: Activity, onClick: (Photo) -> Unit = {}): View {
    val iv = ImageView(activity)
    val width = screenWidth(activity)
    val photoSize = photo.getLargePhoto()
    val scale = width * 1.0f / photoSize.width
    val ivHeight = (photoSize.height * scale).toInt()
    val params = LinearLayout.LayoutParams(width, ivHeight)
    params.topMargin = 12
    params.bottomMargin = 12
    iv.layoutParams = params
    XviiPicasso.get()
            .loadRounded(photoSize.url)
            .resize(width, ivHeight)
            .centerCrop()
            .into(iv)
    iv.setOnClickListener { onClick.invoke(photo) }
    return iv
}

fun getGif(doc: Doc, context: Context): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_video, null, false)
    val ivVideo = included.findViewById<ImageView>(R.id.ivVideo)
    XviiPicasso.get()
            .loadRounded(doc.preview?.photo?.sizes?.get(0)?.src ?: "")
            .resize(pxFromDp(context, 250), pxFromDp(context, 186))
            .centerCrop()
            .into(ivVideo)
    included.findViewById<TextView>(R.id.tvDuration).text = "gif"
    included.setOnClickListener { GifViewerActivity.showGif(context, doc) }
    return included
}

fun getDoc(doc: Doc, context: Context): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_doc, null, false)
    included.findViewById<RelativeLayout>(R.id.relativeLayout).stylize(changeStroke = false)
    included.findViewById<TextView>(R.id.tvExt).text = doc.ext?.toUpperCase()
    included.findViewById<TextView>(R.id.tvTitle).text = doc.title
    included.findViewById<TextView>(R.id.tvSize).text = getSize(context.resources, doc.size)
    included.setOnClickListener {
        simpleUrlIntent(context, doc.url)
    }
    return included
}

fun getEncrypted(doc: Doc, context: Context, decryptCallback: (Doc) -> Unit = {}): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_enc, null, false)
    included.findViewById<ViewGroup>(R.id.relativeLayout).stylize(changeStroke = false)
    included.findViewById<TextView>(R.id.tvTitle).text = doc.title
    included.findViewById<TextView>(R.id.tvSize).text = getSize(context.resources, doc.size)
    included.setOnClickListener {
        decryptCallback.invoke(doc)
    }
    return included
}

fun getAudio(audio: Audio, context: Context, audios: List<Audio> = arrayListOf(audio)): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_audio, null, false)
    val dPlay = ContextCompat.getDrawable(context, R.drawable.ic_play)
    val dPause = ContextCompat.getDrawable(context, R.drawable.ic_pause)
    dPlay?.stylize(ColorManager.DARK_TAG)
    dPause?.stylize(ColorManager.DARK_TAG)
    val ivButton = included.findViewById<ImageView>(R.id.ivButton)
    ivButton.setImageDrawable(dPlay)
    included.findViewById<TextView>(R.id.tvTitle).text = audio.title
    included.findViewById<TextView>(R.id.tvArtist).text = audio.artist
    if (MusicService.getPlayedTrack()?.audio == audio && MusicService.isPlaying()) {
        ivButton.setImageDrawable(dPause)
    }
    ivButton.setOnClickListener {
        ivButton.setImageDrawable(dPause)
        val position = audios.indexOf(audio)
        val tracks = ArrayList(audios.map { Track(it) })
        MusicService.launch(context.applicationContext, tracks, position)
        MusicService.subscribeOnAudioPlaying { track ->
            if (audio == track.audio) {
                ivButton.setImageDrawable(dPause)
            }
        }
        MusicService.subscribeOnAudioPausing {
            ivButton.setImageDrawable(dPlay)
        }
    }
    return included
}

fun getLink(link: Link, context: Context): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_link, null, false)
    (included.findViewById<ImageView>(R.id.ivPhoto)).load(link.photo?.getSmallPhoto()?.url)

    included.findViewById<TextView>(R.id.tvTitle).text = link.title
    included.findViewById<TextView>(R.id.tvCaption).text = link.url
    included.setOnClickListener { simpleUrlIntent(context, link.url) }
    return included
}

fun getVideo(video: Video, context: Context, onClick: (Video) -> Unit = {}): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_video, null, false)
    XviiPicasso.get()
            .loadRounded(video.maxPhoto)
            .resize(pxFromDp(context, 250), pxFromDp(context, 186))
            .centerCrop()
            .into(included.findViewById<ImageView>(R.id.ivVideo))
    included.findViewById<TextView>(R.id.tvDuration).text = secToTime(video.duration)
    included.setOnClickListener { onClick.invoke(video) }
    return included
}

fun getPoll(poll: Poll, context: Context): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_poll, null, false)

    included.findViewById<TextView>(R.id.tvQuestion).text = poll.question
    included.findViewById<ImageView>(R.id.ivPhoto).stylize()
    included.setOnClickListener { PollsActivity.launch(context, poll) }
    return included
}