package com.twoeightnine.root.xvii.chats.messages.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.music.models.Track
import com.twoeightnine.root.xvii.background.music.services.MusicService
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.WallPost
import com.twoeightnine.root.xvii.model.attachments.*
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.poll.PollFragment
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.wallpost.WallPostFragment
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.show
import kotlinx.android.synthetic.main.container_audio.view.*
import kotlinx.android.synthetic.main.container_audio.view.tvText
import kotlinx.android.synthetic.main.container_audio.view.tvTitle
import kotlinx.android.synthetic.main.container_doc.view.*
import kotlinx.android.synthetic.main.container_gift.view.*
import kotlinx.android.synthetic.main.container_link.view.*
import kotlinx.android.synthetic.main.container_link.view.ivPhoto
import kotlinx.android.synthetic.main.container_poll.view.*
import kotlinx.android.synthetic.main.container_video.view.*
import kotlinx.android.synthetic.main.container_wall.view.*
import kotlinx.android.synthetic.main.item_message_wtf.view.civPhoto
import kotlinx.android.synthetic.main.item_message_wtf.view.tvName

class MessageInflater(
        private val context: Context,
        private val callback: MessagesAdapter.Callback
) {

    private val resources = context.resources
    private val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private val photoMargin = resources.getDimensionPixelSize(R.dimen.chat_message_photo_margin)
    private val levelPadding = resources.getDimensionPixelSize(R.dimen.chat_message_level_padding)
    private val contentWidth = resources.getDimensionPixelSize(R.dimen.chat_message_content_width)
    private val defaultRadius = resources.getDimensionPixelSize(R.dimen.default_radius)

    fun createViewsFor(message: Message, level: Int = 0): List<View> {
        val attachments = message.attachments ?: return emptyList()
        return attachments.mapNotNull { createViewFor(it, message, level) }
    }

    private fun createViewFor(attachment: Attachment, message: Message, level: Int = 0): View? {
        val attachments = message.attachments ?: return null
        return when (attachment.type) {
            Attachment.TYPE_PHOTO -> attachment.photo
                    ?.let { photo -> createPhoto(photo, attachments.getPhotos(), level) }
            Attachment.TYPE_STICKER -> attachment.sticker?.photo512?.let(::createSticker)
            Attachment.TYPE_GIFT -> attachment.gift?.let { createGift(it, message.text) }
            Attachment.TYPE_AUDIO -> attachment.audio
                    ?.let { audio -> createAudio(audio, attachments.getAudios().filterNotNull()) }
            Attachment.TYPE_LINK -> attachment.link?.let(::createLink)
            Attachment.TYPE_VIDEO -> attachment.video?.let(::createVideo)
            Attachment.TYPE_POLL -> attachment.poll?.let(::createPoll)
            Attachment.TYPE_WALL -> attachment.wall?.let(::createWallPost)
            Attachment.TYPE_GRAFFITI -> attachment.graffiti?.url?.let(::createSticker)
            Attachment.TYPE_AUDIO_MESSAGE -> attachment.audioMessage?.let(::createAudioMessage)
            Attachment.TYPE_DOC -> attachment.doc?.let { doc ->
                when {
                    doc.isGif -> createGif(doc)
                    doc.isEncrypted -> createEncrypted(doc)
                    else -> createDoc(doc)
                }
            }
            else -> null
        }
    }

    private fun createPhoto(photo: Photo, photos: List<Photo>, level: Int = 0): View {
        val roundedImageView = RoundedImageView(context)
        roundedImageView.cornerRadius = defaultRadius.toFloat()

        val width = contentWidth - 2 * level * levelPadding - 2 * photoMargin
        val photoSize = photo.getOptimalPhoto()
                ?: photo.getMediumPhoto()
                ?: photo.getSmallPhoto()
                ?: return roundedImageView

        val scale = width * 1.0f / photoSize.width
        val ivHeight = (photoSize.height * scale).toInt()

        roundedImageView.layoutParams = LinearLayout.LayoutParams(width, ivHeight).apply {
            topMargin = photoMargin
            bottomMargin = photoMargin
            marginStart = photoMargin
            marginEnd = photoMargin
        }

        roundedImageView.load(photoSize.url) {
            override(width, ivHeight)
            centerCrop()
        }
        roundedImageView.setOnClickListener { }
        return roundedImageView
    }

    private fun createSticker(stickerUrl: String): View {
        val imageView = ImageView(context)
        imageView.load(stickerUrl, placeholder = false)
        return imageView
    }

    private fun createGift(gift: Gift, messageBody: String): View {
        val included = inflater.inflate(R.layout.container_gift, null)
        included.ivThumb.load(gift.thumb256)
        if (messageBody.isNotBlank()) {
            included.tvGiftMessage.text = when {
                EmojiHelper.hasEmojis(messageBody) -> EmojiHelper.getEmojied(context, messageBody)
                else -> messageBody
            }
        }
        return included
    }

    //TODO pass all audio messages
    private fun createAudioMessage(audioMessage: AudioMessage): View {
        val audio = Audio(audioMessage, context.getString(R.string.voice_message))
        return createAudio(audio, text = audioMessage.transcript)
    }

    private fun createAudio(audio: Audio, audios: List<Audio> = listOf(audio), text: String? = null): View {
        val audioView = inflater.inflate(R.layout.container_audio, null)
        val dPlay = ContextCompat.getDrawable(context, R.drawable.ic_play)
        val dPause = ContextCompat.getDrawable(context, R.drawable.ic_pause)
        dPlay?.paint(Munch.color.color)
        dPause?.paint(Munch.color.color)
        audioView.ivButton.setImageDrawable(dPlay)
        audioView.tvTitle.text = audio.title
        audioView.tvArtist.text = audio.artist
        if (MusicService.getPlayedTrack()?.audio == audio && MusicService.isPlaying()) {
            audioView.ivButton.setImageDrawable(dPause)
        }
        if (!text.isNullOrBlank()) {
            audioView.ivSubtitles.show()
            audioView.tvText.text = text
            if (Prefs.lowerTexts) {
                audioView.tvText.lower()
            }
            audioView.ivSubtitles.setOnClickListener {
                audioView.tvText.show()
                audioView.ivSubtitles.hide()
            }
        }
        audioView.ivButton.setOnClickListener {
            audioView.ivButton.setImageDrawable(dPause)
            val position = audios.indexOf(audio)
            val tracks = ArrayList(audios.map { Track(it) })
            MusicService.launch(context.applicationContext, tracks, position)
            MusicService.subscribeOnAudioPlaying { track ->
                if (audio == track.audio) {
                    audioView.ivButton.setImageDrawable(dPause)
                }
            }
            MusicService.subscribeOnAudioPausing {
                audioView.ivButton.setImageDrawable(dPlay)
            }
        }
        return audioView
    }

    private fun createLink(link: Link): View {
        val linkView = inflater.inflate(R.layout.container_link, null)
        linkView.ivPhoto.load(link.photo?.getSmallPhoto()?.url)

        linkView.tvTitle.text = link.title
        linkView.tvCaption.text = link.url
        linkView.setOnClickListener { simpleUrlIntent(context, link.url) }
        return linkView
    }

    private fun createVideo(video: Video): View {
        val videoView = LayoutInflater.from(context).inflate(R.layout.container_video, null)
        videoView.ivVideo.load(video.maxPhoto) {
            override(pxFromDp(context, 250), pxFromDp(context, 186))
            centerCrop()
        }
        videoView.tvDuration.text = secToTime(video.duration)
        videoView.setOnClickListener { }
        return videoView
    }

    private fun createPoll(poll: Poll): View {
        val pollView = inflater.inflate(R.layout.container_poll, null)

        pollView.tvQuestion.text = poll.question
        pollView.ivPhoto.paint(Munch.color.color)
        pollView.setOnClickListener {
            context.startFragment<PollFragment>(PollFragment.getArgs(poll))
        }
        return pollView
    }

    private fun createWallPost(wallPost: WallPost): View? {
        val postId = wallPost.stringId
        val title = wallPost.group?.name ?: wallPost.user?.fullName
        val photo = wallPost.group?.photo100 ?: wallPost.user?.photo100
        if (title == null && photo == null) return null

        val wallPostView = inflater.inflate(R.layout.container_wall, null)
        wallPostView.setOnClickListener {
            context.startFragment<WallPostFragment>(WallPostFragment.createArgs(postId))
        }

        with(wallPostView) {
            tvName.show()
            civPhoto.show()
            tvPlaceHolder.hide()

            civPhoto.load(photo)
            tvName.text = title?.toLowerCase()
            if (!wallPost.text.isNullOrBlank()) {
                tvText.show()
                tvText.text = wallPost.text
            }
            wallPost.attachments?.getPhotos()?.firstOrNull()?.also { photo ->
                ivPhoto.show()
                ivPhoto.load(photo.getOptimalPhoto()?.url) {
                    override(
                            resources.getDimensionPixelSize(R.dimen.chat_wall_post_image_width),
                            resources.getDimensionPixelSize(R.dimen.chat_wall_post_image_height)
                    )
                    centerCrop()
                }
            }
        }
        return wallPostView
    }

    private fun createGif(doc: Doc): View? {
        val gifUrl = doc.preview?.photo?.sizes?.firstOrNull()?.src ?: return null
        val gifView = inflater.inflate(R.layout.container_video, null)

        gifView.ivVideo.load(gifUrl) {
            override(pxFromDp(context, 250), pxFromDp(context, 186))
            centerCrop()
        }
        gifView.tvDuration.text = "gif"
        gifView.setOnClickListener {
            if (!gifView.ivPlay.isShown) {
                gifView.ivPlay.show()
                gifView.ivPlayWhite.show()
                gifView.rlDuration.show()
                gifView.ivVideo.load(gifUrl) {
                    override(pxFromDp(context, 250), pxFromDp(context, 186))
                    centerCrop()
                }
            } else {
                gifView.ivPlay.hide()
                gifView.ivPlayWhite.hide()
                gifView.rlDuration.hide()
                Glide.with(gifView)
                        .load(doc.url)
                        .into(gifView.ivVideo)
            }
        }
        return gifView
    }

    private fun createDoc(doc: Doc): View {
        val docView = inflater.inflate(R.layout.container_doc, null)
        docView.relativeLayout.stylize(changeStroke = false)
        docView.tvExt.text = doc.ext
        docView.tvTitle.text = doc.title
        docView.tvSize.text = getSize(context.resources, doc.size)
        docView.setOnClickListener {
            simpleUrlIntent(context, doc.url)
        }
        return docView
    }

    private fun createEncrypted(doc: Doc): View {
        val encryptedView = inflater.inflate(R.layout.container_enc, null)
        encryptedView.relativeLayout.stylize(changeStroke = false)
        encryptedView.tvTitle.text = doc.title
        encryptedView.tvSize.text = getSize(context.resources, doc.size)
        encryptedView.setOnClickListener {
//            decryptCallback.invoke(doc)
        }
        return encryptedView
    }
}