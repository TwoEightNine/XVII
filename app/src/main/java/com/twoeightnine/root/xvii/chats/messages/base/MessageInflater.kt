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
import com.twoeightnine.root.xvii.databinding.*
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.WallPost
import com.twoeightnine.root.xvii.model.attachments.*
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.*
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.show

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
    private val videoWidth = resources.getDimensionPixelSize(R.dimen.chat_message_video_width)
    private val videoHeight = resources.getDimensionPixelSize(R.dimen.chat_message_video_height)
    private val stickerSize = resources.getDimensionPixelSize(R.dimen.chat_message_sticker_width)
    private val graffitiSize = resources.getDimensionPixelSize(R.dimen.chat_message_graffiti_width)

    fun getMessageWidth(message: Message, level: Int): Int = when {
        message.isSticker() -> stickerSize
        message.isGraffiti() -> graffitiSize
        else -> contentWidth
    } - levelPadding * level * 2

    fun createViewsFor(message: Message, level: Int = 0): List<View> {
        val attachments = message.attachments ?: return emptyList()
        return attachments.mapNotNull { createViewFor(it, message, level) }
    }

    private fun createViewFor(attachment: Attachment, message: Message, level: Int = 0): View? {
        val attachments = message.attachments ?: return null
        return when (attachment.type) {
            Attachment.TYPE_PHOTO -> attachment.photo
                    ?.let { photo -> createPhoto(photo, attachments.getPhotos(), level) }
            Attachment.TYPE_STICKER -> attachment.sticker?.let(::createSticker)
            Attachment.TYPE_GIFT -> attachment.gift?.let { createGift(it, message.text) }
            Attachment.TYPE_AUDIO -> attachment.audio
                    ?.let { audio -> createAudio(audio, attachments.getAudios().filterNotNull()) }
            Attachment.TYPE_LINK -> attachment.link?.let(::createLink)
            Attachment.TYPE_VIDEO -> attachment.video?.let(::createVideo)
            Attachment.TYPE_POLL -> attachment.poll?.let(::createPoll)
            Attachment.TYPE_WALL -> attachment.wall?.let(::createWallPost)
            Attachment.TYPE_GRAFFITI -> attachment.graffiti?.let(::createGraffiti)
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
        roundedImageView.setOnClickListener {
            val position = photos.indexOf(photo)
            callback.onPhotoClicked(position, photos)
        }
        return roundedImageView
    }

    private fun createSticker(sticker: Sticker): View =
            ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(stickerSize, stickerSize)
                load(sticker.photo512, placeholder = false)
            }

    private fun createGraffiti(graffiti: Graffiti): View =
            ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(graffitiSize, graffitiSize)
                load(graffiti.url, placeholder = false)
            }

    private fun createGift(gift: Gift, messageBody: String): View = ContainerGiftBinding.inflate(inflater).run {
        ivThumb.load(gift.thumb256)
        if (messageBody.isNotBlank()) {
            tvGiftMessage.text = when {
                EmojiHelper.hasEmojis(messageBody) -> EmojiHelper.getEmojied(context, messageBody)
                else -> messageBody
            }
        }
        root
    }

    //TODO pass all audio messages
    private fun createAudioMessage(audioMessage: AudioMessage): View =
            createAudio(
                    audio = Audio(audioMessage, context.getString(R.string.voice_message)),
                    text = audioMessage.transcript
            )

    private fun createAudio(audio: Audio, audios: List<Audio> = listOf(audio), text: String? = null): View {
        val binding = ContainerAudioBinding.inflate(inflater)
        val dPlay = ContextCompat.getDrawable(context, R.drawable.ic_play)
        val dPause = ContextCompat.getDrawable(context, R.drawable.ic_pause)
        dPlay?.paint(Munch.color.color)
        dPause?.paint(Munch.color.color)
        binding.ivButton.setImageDrawable(dPlay)
        binding.tvTitle.text = audio.title
        binding.tvArtist.text = audio.artist
        if (MusicService.getPlayedTrack()?.audio == audio && MusicService.isPlaying()) {
            binding.ivButton.setImageDrawable(dPause)
        }
        if (!text.isNullOrBlank()) {
            binding.ivSubtitles.show()
            binding.tvText.text = text
            if (Prefs.lowerTexts) {
                binding.tvText.lower()
            }
            binding.ivSubtitles.setOnClickListener {
                binding.tvText.show()
                binding.ivSubtitles.hide()
            }
        }
        binding.ivButton.setOnClickListener {
            // TODO
            binding.ivButton.setImageDrawable(dPause)
            val position = audios.indexOf(audio)
            val tracks = ArrayList(audios.map { Track(it) })
            MusicService.launch(context.applicationContext, tracks, position)
            MusicService.subscribeOnAudioPlaying { track ->
                if (audio == track.audio) {
                    binding.ivButton.setImageDrawable(dPause)
                }
            }
            MusicService.subscribeOnAudioPausing {
                binding.ivButton.setImageDrawable(dPlay)
            }
        }
        return binding.root
    }

    private fun createLink(link: Link): View =
            ContainerLinkBinding.inflate(inflater).run {
                ivPhoto.load(link.photo?.getSmallPhoto()?.url)
                tvTitle.text = link.title
                tvCaption.text = link.url
                root.setOnClickListener {
                    callback.onLinkClicked(link)
                }
                root
            }

    private fun createVideo(video: Video): View =
            ContainerVideoBinding.inflate(inflater).run {
                ivVideo.load(video.maxPhoto) {
                    override(videoWidth, videoHeight)
                    centerCrop()
                }
                if (video.duration != 0) {
                    tvDuration.text = secToTime(video.duration)
                } else {
                    rlDuration.hide()
                }
                root.setOnClickListener {
                    callback.onVideoClicked(video)
                }
                root
            }

    private fun createPoll(poll: Poll): View =
            ContainerPollBinding.inflate(inflater).run {
                tvQuestion.text = poll.question
                ivPhoto.paint(Munch.color.color)
                root.setOnClickListener {
                    callback.onPollClicked(poll)
                }
                root
            }

    private fun createWallPost(wallPost: WallPost): View? {
        val title = wallPost.group?.name ?: wallPost.user?.fullName
        val photo = wallPost.group?.photo100 ?: wallPost.user?.photo100
        if (title == null && photo == null) return null

        return ContainerWallBinding.inflate(inflater).run {
            root.setOnClickListener {
                callback.onWallPostClicked(wallPost)
            }

            tvName.show()
            civPhoto.show()
            tvPlaceHolder.hide()

            civPhoto.load(photo)
            tvName.text = title
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
            root
        }
    }

    // TODO autoplay, resize accroding to real size
    private fun createGif(doc: Doc): View? {
        val gifUrl = doc.preview?.photo?.sizes?.firstOrNull()?.src ?: return null
        return ContainerVideoBinding.inflate(inflater).run {
            ivVideo.load(gifUrl) {
                override(pxFromDp(context, 250), pxFromDp(context, 186))
                centerCrop()
            }
            tvDuration.text = "gif"
            root.setOnClickListener {
                if (!ivPlay.isShown) {
                    ivPlay.show()
                    ivPlayWhite.show()
                    rlDuration.show()
                    ivVideo.load(gifUrl) {
                        override(pxFromDp(context, 250), pxFromDp(context, 186))
                        centerCrop()
                    }
                } else {
                    ivPlay.hide()
                    ivPlayWhite.hide()
                    rlDuration.hide()
                    Glide.with(root)
                            .load(doc.url)
                            .into(ivVideo)
                }
            }
            root
        }
    }

    private fun createDoc(doc: Doc): View =
            ContainerDocBinding.inflate(inflater).run {
                relativeLayout.stylize(changeStroke = false)
                tvExt.text = doc.ext
                tvTitle.text = doc.title
                tvSize.text = getSize(context.resources, doc.size)
                root.setOnClickListener {
                    callback.onDocClicked(doc)
                }
                root
            }

    private fun createEncrypted(doc: Doc): View =
            ContainerEncBinding.inflate(inflater).run {
                relativeLayout.stylize(changeStroke = false)
                tvTitle.text = doc.title
                tvSize.text = getSize(context.resources, doc.size)
                root.setOnClickListener {
                    callback.onEncryptedFileClicked(doc)
                }
                root
            }
}