/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.chats.attachments

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import com.makeramen.roundedimageview.RoundedImageView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.music.models.Track
import com.twoeightnine.root.xvii.background.music.services.MusicService
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.databinding.*
import com.twoeightnine.root.xvii.extensions.load
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.WallPost
import com.twoeightnine.root.xvii.model.attachments.*
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.poll.PollFragment
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.wallpost.WallPostFragment
import com.twoeightnine.root.xvii.web.GifViewerFragment
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.lowerIf
import global.msnthrp.xvii.uikit.extensions.show
import global.msnthrp.xvii.uikit.utils.DisplayUtils
import global.msnthrp.xvii.uikit.utils.SizeUtils
import global.msnthrp.xvii.uikit.utils.color.DocColors
import java.io.File

class AttachmentsInflater(
        private val context: Context,
        private val callback: Callback
) {

    /**
     * lambda to fetch a list of actual audios to play sequentially
     */
    var audiosFetcher: (() -> List<Audio>)? = null

    /**
     * lambda to fetch a list of actual audio messages to play sequentially
     */
    var audioMessagesFetcher: (() -> List<AudioMessage>)? = null

    private val resources = context.resources
    private val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private val messageMaxWidth = resources.getDimensionPixelSize(R.dimen.chat_message_max_width)
    private val photoMargin = resources.getDimensionPixelSize(R.dimen.chat_message_photo_margin)
    private val levelPadding = resources.getDimensionPixelSize(R.dimen.chat_message_level_padding)
    private val contentWidth = resources.getDimensionPixelSize(R.dimen.chat_message_content_width)
    private val defaultRadius = resources.getDimensionPixelSize(R.dimen.default_radius)
    private val videoWidth = resources.getDimensionPixelSize(R.dimen.chat_message_video_width)
    private val videoHeight = resources.getDimensionPixelSize(R.dimen.chat_message_video_height)
    private val stickerSize = resources.getDimensionPixelSize(R.dimen.chat_message_sticker_width)
    private val graffitiSize = resources.getDimensionPixelSize(R.dimen.chat_message_graffiti_width)
    private val loaderSize = resources.getDimensionPixelSize(R.dimen.chat_message_loader)
    private val wtfDimen = SizeUtils.pxFromDp(context, 3)

    fun getTimeStyle(message: Message): TimeStyle {
        val attachments = message.attachments
        return when {
            attachments.isNullOrEmpty() -> TimeStyle.TEXT
            attachments.last().isGraphical -> TimeStyle.ATTACHMENTS_OVERLAYED
            else -> TimeStyle.ATTACHMENTS_EMBEDDED
        }
    }

    fun getMessageWidth(message: Message, fullDeepness: Boolean, level: Int): Int {
        message.replyMessage?.also {
            return if (message.isReplyingSticker()) {
                stickerSize
            } else {
                contentWidth
            }
        }
        if (!message.fwdMessages.isNullOrEmpty()) {
            return if (fullDeepness) {
                ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                contentWidth
            }
        }

        if (!message.attachments.isNullOrEmpty()) {
            return when {
                message.isSticker() -> stickerSize
                message.isGraffiti() -> graffitiSize
                else -> contentWidth
            } - levelPadding * level * 2
        }
        return ViewGroup.LayoutParams.WRAP_CONTENT
    }

    fun getMessageMaxWidth(fullDeepness: Boolean, level: Int): Int {
        return when {
            fullDeepness -> ViewGroup.LayoutParams.MATCH_PARENT
            else -> messageMaxWidth - levelPadding * level * 2
        }
    }

    fun getViewLoader(): View = ContainerLoaderBinding.inflate(inflater).root

    fun getRepliedMessageView(repliedMessage: Message): View =
            ItemMessageRepliedBinding.inflate(inflater).run {
                tvName.text = repliedMessage.name
                tvName.lowerIf(Prefs.lowerTexts)
                tvBody.text = repliedMessage.getResolvedMessage(context)
                root
            }

    fun createViewsFor(wallPost: WallPost): List<View> {
        val attachments = wallPost.attachments ?: return emptyList()
        return attachments.mapNotNull { createViewForWallPost(it, wallPost) }
    }

    fun createViewsFor(message: Message, level: Int = 0): List<View> {
        val attachments = message.attachments ?: return emptyList()
        return attachments.mapNotNull { createViewForMessage(it, message, level) }
    }

    private fun createViewForMessage(attachment: Attachment, message: Message, level: Int = 0): View? {
        val attachments = message.attachments ?: return null
        return when (attachment.type) {
            Attachment.TYPE_PHOTO -> attachment.photo
                    ?.let { photo -> createPhotoForMessage(photo, attachments.getPhotos(), level) }
            Attachment.TYPE_STICKER -> attachment.sticker?.let(::createSticker)
            Attachment.TYPE_GIFT -> attachment.gift?.let { createGift(it, message.text) }
            Attachment.TYPE_WALL -> attachment.wall?.let(::createWallPost)
            Attachment.TYPE_GRAFFITI -> attachment.graffiti?.let(::createGraffiti)
            Attachment.TYPE_AUDIO_MESSAGE -> attachment.audioMessage?.let(::createAudioMessage)
            Attachment.TYPE_CALL -> attachment.call?.let(::createCall)
            else -> createViewFor(attachment, attachments, level)
        }
    }

    private fun createViewForWallPost(attachment: Attachment, wallPost: WallPost): View? {
        val attachments = wallPost.attachments ?: return null
        return when (attachment.type) {
            Attachment.TYPE_PHOTO -> attachment.photo
                    ?.let { photo -> createPhotoForWallPost(photo, attachments.getPhotos()) }
            else -> createViewFor(attachment, attachments)
        }
    }

    private fun createViewFor(attachment: Attachment, attachments: List<Attachment>, level: Int = 0): View? {
        return when (attachment.type) {
            Attachment.TYPE_AUDIO -> {
                val audios = audiosFetcher?.invoke() ?: attachments.getAudios().filterNotNull()
                attachment.audio?.let { audio -> createAudio(audio, audios) }
            }
            Attachment.TYPE_LINK -> attachment.link?.let(::createLink)
            Attachment.TYPE_VIDEO -> attachment.video?.let(::createVideo)
            Attachment.TYPE_POLL -> attachment.poll?.let(::createPoll)
            Attachment.TYPE_DOC -> attachment.doc?.let { doc ->
                when {
                    doc.isGif -> createGif(doc, level)
                    doc.isEncrypted -> createEncrypted(doc)
                    else -> createDoc(doc)
                }
            }
            else -> null
        }
    }

    private fun createPhotoForMessage(photo: Photo, photos: List<Photo>, level: Int = 0): View {
        val width = getViewWidth(photoMargin, level)
        val view = createPhoto(photo, width)
        view.setOnClickListener {
            val position = photos.indexOf(photo)
            callback.onPhotoClicked(position, photos)
        }
        return view
    }

    private fun createPhotoForWallPost(photo: Photo, photos: List<Photo>): View {
        val width = DisplayUtils.screenWidth
        val view = createPhoto(photo, width)
        view.setOnClickListener {
            val position = photos.indexOf(photo)
            callback.onPhotoClicked(position, photos)
        }
        return view
    }

    private fun createPhoto(photo: Photo, width: Int): View {
        val roundedImageView = RoundedImageView(context).apply {
            updatePadding(0, 0, 0, 0)
        }
        roundedImageView.cornerRadius = defaultRadius.toFloat()

        val photoSize = photo.getOptimalPhoto()
                ?: photo.getMediumPhoto()
                ?: photo.getSmallPhoto()
                ?: return roundedImageView

        val scale = width * 1.0f / photoSize.width
        val ivHeight = (photoSize.height * scale).toInt()

        roundedImageView.layoutParams = LinearLayout.LayoutParams(width, ivHeight)
                .withMargin(getAppliedMargin(photoMargin))

        roundedImageView.load(photoSize.url) {
            override(width, ivHeight)
            centerCrop()
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

    private fun createGift(gift: Gift, messageBody: String): View =
            ContainerGiftBinding.inflate(inflater).run {
                ivThumb.load(gift.thumb256)
                if (messageBody.isNotBlank()) {
                    tvGiftMessage.text = when {
                        EmojiHelper.hasEmojis(messageBody) -> EmojiHelper.getEmojied(context, messageBody)
                        else -> messageBody
                    }
                }
                root
            }

    private fun createCall(call: Call): View = ContainerCallBinding.inflate(inflater).run {
        relativeLayout.background.paint(Munch.color.color)
        tvCall.text = when (call.state) {
            Call.State.MISSED -> context.getString(R.string.call_missed)
            Call.State.UNKNOWN -> context.getString(R.string.call_unknown)
            Call.State.DECLINED -> context.getString(R.string.call_declined)
            Call.State.REACHED -> {
                val duration = secToTime(call.duration)
                context.getString(R.string.call_reached, duration)
            }
        }
        root
    }

    private fun createAudioMessage(audioMessage: AudioMessage): View {
        val audioMessages = audioMessagesFetcher?.invoke() ?: listOf(audioMessage)
        val audios = audioMessages.map { Audio(it, context.getString(R.string.voice_message)) }
        return createAudio(
                audio = Audio(audioMessage, context.getString(R.string.voice_message)),
                audios = audios,
                text = audioMessage.transcript
        )
    }

    private fun createAudio(audio: Audio, audios: List<Audio> = listOf(audio), text: String? = null): View {
        val binding = ContainerAudioBinding.inflate(inflater)
        val dPlay = ContextCompat.getDrawable(context, R.drawable.ic_play)
        val dPause = ContextCompat.getDrawable(context, R.drawable.ic_pause)

        dPlay?.paint(Munch.color.color)
        dPause?.paint(Munch.color.color)

        binding.apply {
            ivButton.setImageDrawable(dPlay)
            tvTitle.text = audio.title
            tvArtist.text = audio.artist

            if (MusicService.getPlayedTrack()?.audio == audio && MusicService.isPlaying()) {
                ivButton.setImageDrawable(dPause)
            }
        }
        if (!text.isNullOrBlank()) {
            binding.apply {
                ivSubtitles.show()
                tvText.text = text
                tvText.lowerIf(Prefs.lowerTexts)
                ivSubtitles.setOnClickListener {
                    tvText.show()
                    ivSubtitles.hide()
                }
            }
        }
        MusicService.subscribeOnAudioPlaying { track ->
            val image = when (audio) {
                track.audio -> dPause
                else -> dPlay
            }
            binding.ivButton.setImageDrawable(image)
        }
        MusicService.subscribeOnAudioPausing {
            binding.ivButton.setImageDrawable(dPlay)
        }
        binding.ivButton.setOnClickListener {
            val sameAudio = MusicService.getPlayedTrack()?.audio == audio
            val isPlaying = MusicService.isPlaying()

            val image = when {
                sameAudio && isPlaying -> dPlay
                else -> dPause
            }
            binding.ivButton.setImageDrawable(image)
            when {
                sameAudio -> MusicService.playPause()
                else -> {
                    MusicService.launch(
                            applicationContext = context.applicationContext,
                            tracks = ArrayList(audios.map { Track(it) }),
                            position = audios.indexOf(audio)
                    )
                }
            }
        }
        return binding.root
    }

    private fun createLink(link: Link): View =
            ContainerLinkBinding.inflate(inflater).run {
                ivPhoto.load(link.photo?.getSmallPhoto()?.url)
                tvTitle.text = link.title
                tvCaption.text = link.caption
                root.setOnClickListener {
                    callback.onLinkClicked(link)
                }
                root
            }

    private fun createVideo(video: Video, level: Int = 0): View =
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

                val width = getViewWidth(photoMargin, level)
                val ratio = videoWidth.toFloat() / videoHeight
                val height = (width / ratio).toInt()
                ivVideo.layoutParams = RelativeLayout.LayoutParams(width, height)
                        .withMargin(getAppliedMargin(photoMargin))
                root.setOnClickListener {
                    callback.onVideoClicked(video)
                }
                root
            }

    private fun createPoll(poll: Poll): View =
            ContainerPollBinding.inflate(inflater).run {
                tvQuestion.text = poll.question
                relativeLayout.background.paint(Munch.color.color)
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

    private fun createGif(doc: Doc, level: Int = 0): View =
            ContainerGifBinding.inflate(inflater).run {
                val smallPreview = doc.preview?.photo?.getSmallPreview()
                val width = getViewWidth(photoMargin, level)
                val defaultRatio = width.toFloat() / videoHeight
                val ratio = if (smallPreview != null) {
                    smallPreview.width.toFloat() / smallPreview.height
                } else {
                    defaultRatio
                }

                val height = (width / ratio).toInt()
                root.layoutParams = LinearLayout.LayoutParams(width, height)
                        .withMargin(getAppliedMargin(photoMargin))
                ivGif.load(doc.url)
                ivGif.setOnClickListener {
                    callback.onGifClicked(doc)
                }
                root
            }

    private fun createDoc(doc: Doc): View =
            ContainerDocBinding.inflate(inflater).run {
                relativeLayout.background.paint(
                        DocColors.getColorByExtension(doc.ext ?: "") ?: Munch.color.color
                )
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
                relativeLayout.background.paint(Munch.color.color)
                tvTitle.text = doc.title
                tvSize.text = getSize(context.resources, doc.size)
                root.setOnClickListener {
                    callback.onEncryptedDocClicked(doc)
                }
                root
            }

    private fun getViewWidth(marginWeWant: Int, level: Int = 0) =
            contentWidth - 2 * marginWeWant - 2 * level * levelPadding

    private fun getAppliedMargin(marginWeWant: Int) = marginWeWant - wtfDimen

    private fun ViewGroup.MarginLayoutParams.withMargin(margin: Int) = apply {
        updateMargins(margin, margin, margin, margin)
    }

    interface Callback {
        fun onEncryptedDocClicked(doc: Doc)
        fun onPhotoClicked(position: Int, photos: List<Photo>)
        fun onVideoClicked(video: Video)
        fun onLinkClicked(link: Link)
        fun onDocClicked(doc: Doc)
        fun onGifClicked(doc: Doc)
        fun onPollClicked(poll: Poll)
        fun onWallPostClicked(wallPost: WallPost)
    }

    enum class TimeStyle {
        ATTACHMENTS_OVERLAYED,
        ATTACHMENTS_EMBEDDED,
        TEXT
    }

    abstract class DefaultCallback(
            private val context: Context,
            private val permissionHelper: PermissionHelper
    ) : Callback {

        override fun onPhotoClicked(position: Int, photos: List<Photo>) {
            ImageViewerActivity.viewImages(context, photos, position)
        }

        override fun onLinkClicked(link: Link) {
            val fullUrl = link.url
            val shortUrl = link.caption
            val message = context.getString(R.string.attachment_open_link_prompt, shortUrl)
            showConfirm(context, message) { yes ->
                if (yes) {
                    BrowsingUtils.openUrl(context, fullUrl)
                }
            }
        }

        override fun onDocClicked(doc: Doc) {
            doc.url ?: return
            val fileName = doc.fileName
            val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    fileName
            )
            val dialog = AlertDialog.Builder(context)
                    .setMessage(R.string.attachment_open_doc_prompt)
                    .setPositiveButton(R.string.attachment_open_doc_prompt_download) { _, _ ->
                        permissionHelper.doOrRequest(
                                arrayOf(PermissionHelper.WRITE_STORAGE, PermissionHelper.READ_STORAGE),
                                R.string.no_access_to_storage,
                                R.string.need_access_to_storage
                        ) { DownloadUtils.download(context, file, doc.url) }
                    }
                    .setNeutralButton(R.string.attachment_open_doc_prompt_browser) { _, _ ->
                        BrowsingUtils.openUrl(context, doc.url)
                    }
                    .create()
            dialog.show()
            dialog.stylize()
        }

        override fun onGifClicked(doc: Doc) {
            context.startFragment<GifViewerFragment>(GifViewerFragment.createArgs(doc))
        }

        override fun onPollClicked(poll: Poll) {
            context.startFragment<PollFragment>(PollFragment.getArgs(poll))
        }

        override fun onWallPostClicked(wallPost: WallPost) {
            context.startFragment<WallPostFragment>(WallPostFragment.createArgs(wallPost.stringId))
        }
    }
}