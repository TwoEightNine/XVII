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
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.music.models.Track
import com.twoeightnine.root.xvii.background.music.services.MusicService
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.attachments.*
import com.twoeightnine.root.xvii.poll.PollFragment
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import kotlinx.android.synthetic.main.container_audio.view.*
import kotlinx.android.synthetic.main.container_video.view.*


fun getPhoto(photo: Photo, context: Context, level: Int = 0, onClick: (Photo) -> Unit = {}): View {
    val margin = context.resources.getDimensionPixelSize(R.dimen.chat_message_photo_margin)
    val levelPadding = context.resources.getDimensionPixelSize(R.dimen.chat_message_level_padding)
    val contentWidth = context.resources.getDimensionPixelSize(R.dimen.chat_message_content_width)

    val view = RoundedImageView(context) // LayoutInflater.from(context).inflate(R.layout.container_photo, null, false)
    view.setCornerRadiusDimen(R.dimen.default_radius)
    val width = contentWidth - 2 * level * levelPadding - 2 * margin
    val photoSize = photo.getOptimalPhoto()
            ?: photo.getMediumPhoto()
            ?: photo.getSmallPhoto()
            ?: return view

    val scale = width * 1.0f / photoSize.width
    val ivHeight = (photoSize.height * scale).toInt()

    view.layoutParams = LinearLayout.LayoutParams(width, ivHeight).apply {
        topMargin = margin
        bottomMargin = margin
        marginStart = margin
        marginEnd = margin
    }

    Picasso.get()
            .load(photoSize.url)
            .resize(width, ivHeight)
            .centerCrop()
            .into(view)
    view.setOnClickListener { onClick.invoke(photo) }
    return view
}

fun getPhotoWall(photo: Photo, activity: Activity, onClick: (Photo) -> Unit = {}): View {
    val iv = RoundedImageView(activity)
    iv.setCornerRadiusDimen(R.dimen.default_radius)
    val width = screenWidth(activity)
    val photoSize = photo.getLargePhoto()
            ?: photo.getOptimalPhoto()
            ?: photo.getMediumPhoto()
            ?: return iv

    val scale = width * 1.0f / photoSize.width
    val ivHeight = (photoSize.height * scale).toInt()
    val params = LinearLayout.LayoutParams(width, ivHeight)
    params.topMargin = 12
    params.bottomMargin = 12
    iv.layoutParams = params
    Picasso.get()
            .load(photoSize.url)
            .resize(width, ivHeight)
            .centerCrop()
            .into(iv)
    iv.setOnClickListener { onClick.invoke(photo) }
    return iv
}

fun getGif(doc: Doc, context: Context): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_video, null, false)

    Picasso.get()
            .load(doc.preview?.photo?.sizes?.get(0)?.src ?: "")
            .resize(pxFromDp(context, 250), pxFromDp(context, 186))
            .centerCrop()
            .into(included.ivVideo)
    included.tvDuration.text = "gif"
    included.setOnClickListener {
        //        GifViewerActivity.showGif(context, doc)
        if (!included.ivPlay.isShown) {
            included.ivPlay.show()
            included.ivPlayWhite.show()
            included.rlDuration.show()
            Picasso.get()
                    .load(doc.preview?.photo?.sizes?.get(0)?.src ?: "")
                    .resize(pxFromDp(context, 250), pxFromDp(context, 186))
                    .centerCrop()
                    .into(included.ivVideo)
        } else {
            included.ivPlay.hide()
            included.ivPlayWhite.hide()
            included.rlDuration.hide()
            Glide.with(included)
                    .load(doc.url)
                    .into(included.ivVideo)
        }
    }
    return included
}

fun getDoc(doc: Doc, context: Context): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_doc, null, false)
    included.findViewById<RelativeLayout>(R.id.relativeLayout).stylize(changeStroke = false)
    included.findViewById<TextView>(R.id.tvExt).text = doc.ext
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

fun getAudio(audio: Audio, context: Context, audios: List<Audio> = arrayListOf(audio), text: String? = null): View {
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
    if (!text.isNullOrBlank()) {
        included.ivSubtitles.show()
        included.tvText.text = text
        if (Prefs.lowerTexts) {
            included.tvText.lower()
        }
        included.ivSubtitles.setOnClickListener {
            included.tvText.show()
            included.ivSubtitles.hide()
        }
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
    Picasso.get()
            .load(video.maxPhoto)
            .resize(pxFromDp(context, 250), pxFromDp(context, 186))
            .centerCrop()
            .into(included.findViewById<ImageView>(R.id.ivVideo))
    included.findViewById<TextView>(R.id.tvDuration).text = secToTime(video.duration)
    included.setOnClickListener { onClick.invoke(video) }
    return included
}

fun getGift(context: Context, gift: Gift, messageBody: String): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_gift, null, false)
    Picasso.get()
            .loadRounded(gift.thumb256)
            .into(included.findViewById<ImageView>(R.id.ivThumb))

    if (messageBody.isNotBlank()) {
        included.findViewById<TextView>(R.id.tvGiftMessage).text = when {
            EmojiHelper.hasEmojis(messageBody) -> EmojiHelper.getEmojied(context, messageBody)
            else -> messageBody
        }
    }
    return included
}

fun getPoll(poll: Poll, context: Context): View {
    val included = LayoutInflater.from(context).inflate(R.layout.container_poll, null, false)

    included.findViewById<TextView>(R.id.tvQuestion).text = poll.question
    included.findViewById<ImageView>(R.id.ivPhoto).paint(Munch.color.color)
    included.setOnClickListener {
        context.startFragment<PollFragment>(PollFragment.getArgs(poll))
    }
    return included
}