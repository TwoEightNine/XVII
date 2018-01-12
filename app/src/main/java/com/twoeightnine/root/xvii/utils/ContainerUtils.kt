package com.twoeightnine.root.xvii.utils

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.GifViewerActivity
import com.twoeightnine.root.xvii.activities.RootActivity
import com.twoeightnine.root.xvii.background.MediaPlayerAsyncTask
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.*
import org.w3c.dom.Text

fun getPhoto(photo: Photo, context: Context, onClick: (Photo) -> Unit = {}): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_photo, null, false)
    Picasso.with(context)
            .loadUrl(photo.almostMax)
            .resize(pxFromDp(context, 250), pxFromDp(context, 300))
            .centerCrop()
            .into(included.findViewById<ImageView>(R.id.ivInternal))
//    included.setOnClickListener { apiUtils.showPhoto(context, photo.photoId, photo.accessKey) }
    included.setOnClickListener { onClick.invoke(photo) }
    return included
}

fun getPhotoWall(photo: Photo, activity: RootActivity, onClick: (Photo) -> Unit = {}): View {
    val iv = ImageView(activity)
    val width = screenWidth(activity)
    val scale = width * 1.0f / photo.width!!
    val ivHeight = photo.height!! * scale
    val params = LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT)
    params.topMargin = 12
    params.bottomMargin = 12
    iv.layoutParams = params
    Picasso.with(activity)
            .loadUrl(photo.almostMax)
            .resize(width, ivHeight.toInt())
            .centerCrop()
            .into(iv)
    iv.setOnClickListener { onClick.invoke(photo) }
    return iv
}

fun getGif(doc: Doc, context: Context): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_video, null, false)
    val ivVideo = included.findViewById<ImageView>(R.id.ivVideo)
    Picasso.with(context)
            .loadUrl(doc.preview?.photo?.sizes?.get(0)?.src ?: "")
            .resize(pxFromDp(context, 250), pxFromDp(context, 186))
            .centerCrop()
            .into(ivVideo)
    included.findViewById<TextView>(R.id.tvDuration).text = "GIF"
    included.setOnClickListener { GifViewerActivity.showGif(context, doc.ownerId, doc.id, doc.url ?: "", doc.accessKey ?: "", doc.title ?: "", doc.getPreview()) }
    return included
}

fun getDoc(doc: Doc, context: Context): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_doc, null, false)
    Style.forViewGroup(included.findViewById<RelativeLayout>(R.id.relativeLayout))
    included.findViewById<TextView>(R.id.tvExt).text = doc.ext?.toUpperCase()
    included.findViewById<TextView>(R.id.tvTitle).text = doc.title
    included.findViewById<TextView>(R.id.tvSize).text = getSize(context, doc.size)
    included.setOnClickListener {
        simpleUrlIntent(context, doc.url)
    }
    return included
}

fun getEncrypted(doc: Doc, context: Context, decryptCallback: (Doc) -> Unit = {}): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_enc, null, false)
    Style.forViewGroup(included.findViewById(R.id.relativeLayout))
    included.findViewById<TextView>(R.id.tvTitle).text = doc.title
    included.findViewById<TextView>(R.id.tvSize).text = getSize(context, doc.size)
    included.setOnClickListener {
        decryptCallback.invoke(doc)
    }
    return included
}

fun getAudio(audio: Audio, context: Context): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_audio, null, false)
    val dPlay = ContextCompat.getDrawable(context, R.drawable.play_big)
    val dPause = ContextCompat.getDrawable(context, R.drawable.ic_pause)
    Style.forDrawable(dPlay, Style.DARK_TAG)
    Style.forDrawable(dPause, Style.DARK_TAG)
    val ivButton = included.findViewById<ImageView>(R.id.ivButton)
    ivButton.setImageDrawable(dPlay)
    included.findViewById<TextView>(R.id.tvTitle).text = audio.title
    included.findViewById<TextView>(R.id.tvArtist).text = audio.artist
    if (Prefs.playerUrl == audio.url && RootActivity.player != null) {
        ivButton.setImageDrawable(dPause)
    }
    ivButton.setOnClickListener {
        if (RootActivity.player != null && RootActivity.player!!.isExecuting) {
            RootActivity.player!!.cancel(true)
            RootActivity.player = null
            ivButton.setImageDrawable(dPlay)
        } else {
            if (RootActivity.player == null) {
                RootActivity.player = MediaPlayerAsyncTask {
                    ivButton.setImageDrawable(dPlay)
                    RootActivity.player = null
                }
            }
            if (!RootActivity.player!!.isExecuting) {
                if (!audio.url.isNullOrEmpty()) {
                    try {
                        RootActivity.player!!.execute(audio.url)
                        ivButton.setImageDrawable(dPause)
                    } catch (e: IllegalStateException) {
                        Lg.i("container player: ${e.message}")
                        RootActivity.player!!.cancel(true)
                    }
                } else {
                    showError(context, R.string.audio_denied)
                }
            }
        }
    }
    return included
}

fun getLink(link: Link, context: Context): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_link, null, false)
    if (link.photo != null) {
        (included.findViewById<ImageView>(R.id.ivPhoto))
                .loadUrl(link.photo.photo75)
    }
    included.findViewById<TextView>(R.id.tvTitle).text = link.title
    included.findViewById<TextView>(R.id.tvCaption).text = link.url
    included.setOnClickListener { simpleUrlIntent(context, link.url) }
    return included
}

fun getVideo(video: Video, context: Context, onClick: (Video) -> Unit = {}): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_video, null, false)
    Picasso.with(context)
            .loadUrl(video.maxPhoto)
            .resize(pxFromDp(context, 250), pxFromDp(context, 186))
            .centerCrop()
            .into(included.findViewById<ImageView>(R.id.ivVideo))
    included.findViewById<TextView>(R.id.tvDuration).text = secToTime(video.duration)
    included.setOnClickListener { onClick.invoke(video) }
    return included
}