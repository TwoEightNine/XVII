package com.twoeightnine.root.xvii.chats.messages.chat.base

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragmentForResult
import com.twoeightnine.root.xvii.chatowner.ChatOwnerActivity
import com.twoeightnine.root.xvii.chats.attachments.AttachmentsInflater
import com.twoeightnine.root.xvii.chats.attachments.attach.AttachFragment
import com.twoeightnine.root.xvii.chats.attachments.attached.AttachedAdapter
import com.twoeightnine.root.xvii.chats.attachments.gallery.model.DeviceItem
import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesFragment
import com.twoeightnine.root.xvii.chats.messages.base.MessagesAdapter
import com.twoeightnine.root.xvii.chats.messages.base.MessagesReplyItemCallback
import com.twoeightnine.root.xvii.chats.messages.chat.MentionedMembersAdapter
import com.twoeightnine.root.xvii.chats.messages.chat.StickersSuggestionAdapter
import com.twoeightnine.root.xvii.chats.tools.ChatInputController
import com.twoeightnine.root.xvii.chats.tools.ChatToolbarController
import com.twoeightnine.root.xvii.dialogs.activities.DialogsForwardActivity
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.CanWrite
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.utils.contextpopup.ContextPopupItem
import com.twoeightnine.root.xvii.utils.contextpopup.createContextPopup
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
import global.msnthrp.xvii.uikit.extensions.*
import kotlinx.android.synthetic.main.chat_input_panel.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.view_chat_multiselect.*

abstract class BaseChatMessagesFragment<VM : BaseChatMessagesViewModel> : BaseMessagesFragment<VM>() {

    protected val peerId by lazy { arguments?.getInt(ARG_PEER_ID) ?: 0 }
    protected val title by lazy { arguments?.getString(ARG_TITLE) ?: "" }
    protected val photo by lazy { arguments?.getString(ARG_PHOTO) ?: "" }
    private val forwardedMessages by lazy { arguments?.getString(ARG_FORWARDED) }
    private val shareText by lazy { arguments?.getString(ARG_SHARE_TEXT) }
    private val shareImage by lazy { arguments?.getString(ARG_SHARE_IMAGE) }

    private val permissionHelper by lazy { PermissionHelper(this) }
    private val attachedAdapter by lazy {
        AttachedAdapter(requireContext(), ::onAttachClicked) {
            inputController?.setAttachedCount(it)
        }
    }
    private val membersAdapter by lazy {
        MentionedMembersAdapter(requireContext()) {
            inputController?.mentionUser(it)
        }
    }
    private val chatToolbarController by lazy {
        ChatToolbarController(xviiToolbar)
    }
    protected val stickersAdapter by lazy {
        StickersSuggestionAdapter(requireContext(), ::onSuggestedStickerClicked)
    }

    private val handler = Handler()
    private var inputController: ChatInputController? = null

    abstract fun onEncryptedDocClicked(doc: Doc)

    override fun prepareViewModel() {
        viewModel.peerId = peerId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        inputController = ChatInputController(requireContext(), view, InputCallback())
        swipeContainer.setOnRefreshListener { viewModel.loadMessages() }
        xviiToolbar.forChat = true

        rvAttached.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        rvAttached.adapter = attachedAdapter

        rvMentionedMembers.layoutManager = LinearLayoutManager(context)
        rvMentionedMembers.adapter = membersAdapter

        val swipeToReply = ItemTouchHelper(MessagesReplyItemCallback(::onSwipedToReply))
        swipeToReply.attachToRecyclerView(rvChatList)
        stylize()
        initContent()
        initMultiSelectMenu()

        rvStickersSuggestion.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvStickersSuggestion.adapter = stickersAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (FakeData.ENABLED) {
            chatToolbarController.setData(FakeData.NAME, FakeData.AVATAR, id = peerId)
        } else {
            chatToolbarController.setData(title, photo, id = peerId)
        }
        if (!peerId.matchesUserId()) {
            onOnlineChanged(Triple(false, 0, 0))
        }
        if (peerId.matchesChatId()) {
            viewModel.loadMembers()
        }
        xviiToolbar?.onClick = {
            activity?.let { hideKeyboard(it) }
            ChatOwnerActivity.launch(context, peerId)
        }

        viewModel.getLastSeen().observe(viewLifecycleOwner, ::onOnlineChanged)
        viewModel.getCanWrite().observe(viewLifecycleOwner, ::onCanWriteChanged)
        viewModel.getActivity().observe(viewLifecycleOwner, ::onActivityChanged)
        viewModel.mentionedMembers.observe(viewLifecycleOwner, ::showMentionedMembers)

        ViewCompat.setOnApplyWindowInsetsListener(rlInputBack) { view, insets ->
            view.setPadding(0, 0, 0, insets.systemWindowInsetBottom)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(fabHasMore) { view, insets ->
            (view.layoutParams as? FrameLayout.LayoutParams)?.apply {
                val margin = context?.resources?.getDimensionPixelSize(R.dimen.chat_fab_more_bottom_margin) ?: 0
                bottomMargin = margin + insets.systemWindowInsetBottom
            }
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(rvStickersSuggestion) { view, insets ->
            (view.layoutParams as? FrameLayout.LayoutParams)?.apply {
                val margin = context?.resources?.getDimensionPixelSize(R.dimen.chat_sticker_suggestions_bottom_margin) ?: 0
                bottomMargin = margin + insets.systemWindowInsetBottom
            }
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(rvMentionedMembers) { view, insets ->
            (view.layoutParams as? FrameLayout.LayoutParams)?.apply {
                val margin = context?.resources?.getDimensionPixelSize(R.dimen.chat_mentioned_members_bottom_margin) ?: 0
                bottomMargin = margin + insets.systemWindowInsetBottom
            }
            insets
        }
    }

    private fun initMultiSelectMenu() {
        ivReplyMulti.setOnClickListener {
            attachedAdapter.fwdMessages = getSelectedMessageIds()
            attachedAdapter.isReply = true
            adapter.multiSelectMode = false
        }
        ivDeleteMulti.setOnClickListener {
            val selectedMessages = adapter.multiSelect.map { it.message }
            val callback = { forAll: Boolean ->
                viewModel.deleteMessages(getSelectedMessageIds(), forAll)
                adapter.multiSelectMode = false
            }
            val edgeDate = time() - 3600 * 24
            val isOut = selectedMessages.none { !it.isOut() }
            val isRecent = selectedMessages.none { it.date < edgeDate }
            if (isOut && isRecent) {
                showDeleteMessagesDialog(callback)
            } else {
                context?.let {
                    showDeleteDialog(it, getString(R.string.these_messages)) { callback.invoke(false) }
                }
            }
        }
        ivMarkMulti.setOnClickListener {
            viewModel.markAsImportant(getSelectedMessageIds())
            adapter.multiSelectMode = false
        }
    }

    private fun stylize() {
        rlMultiAction.background?.paint(Munch.color.color20)
        listOf(ivCancelMulti, ivMarkMulti, ivDeleteMulti, ivForwardMulti, ivReplyMulti)
                .forEach { it.paint(Munch.color.colorDark(50)) }

        if (Prefs.chatBack.isNotEmpty()) {
            try {
                flContainer.backgroundImage = Drawable.createFromPath(Prefs.chatBack)
            } catch (e: Exception) {
                Prefs.chatBack = ""
                showError(activity, e.message ?: "background not found")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume(adapter.items.lastOrNull()?.message)
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onScrolled(isAtBottom: Boolean) {
        viewModel.isShown = isAtBottom
    }

    private fun initContent() {
        forwardedMessages?.also {
            handler.postDelayed({
                attachedAdapter.fwdMessages = it
            }, 500L)
        }
        shareText?.also {
            handler.postDelayed({
                etInput.setText(it)
            }, 500L)
        }
        shareImage?.also {
            handler.postDelayed({
                onImageSelected(it)
            }, 500L)
        }
    }

    private fun showDeleteMessagesDialog(callback: (Boolean) -> Unit) {
        val context = context ?: return

        val dialog = AlertDialog.Builder(context)
                .setMessage(R.string.wanna_delete_messages)
                .setNeutralButton(R.string.delete_for_all) { _, _ -> callback.invoke(true) }
                .setPositiveButton(R.string.delete_only_for_me) { _, _ -> callback.invoke(false) }
                .setNegativeButton(R.string.cancel, null)
                .create()
        dialog.show()
        dialog.stylize(warnPositive = true)
    }

    private fun showEditMessageDialog(message: Message) {
        val context = context ?: return

        TextInputAlertDialog(context, "", message.text) {
            viewModel.editMessage(message.id, it)
        }.show()
    }

    /**
     * handles click on attached media
     */
    private fun onAttachClicked(attachment: Attachment) {
        when (attachment.type) {
            Attachment.TYPE_PHOTO -> attachment.photo?.let {
                ImageViewerActivity.viewImages(context, arrayListOf(it))
            }
            Attachment.TYPE_VIDEO -> attachment.video?.let {
                viewModel.loadVideo(context ?: return, it, { playerUrl ->
                    BrowsingUtils.openUrl(context, playerUrl)
                }, { error ->
                    showError(context, error)
                })
            }
        }
    }

    /**
     * handle setting subtitle and changing user's status
     * triple represents:
     *  - online flag
     *  - last seen time
     *  - device code
     */
    private fun onOnlineChanged(value: Triple<Boolean, Int, Int>) {
        chatToolbarController.setSubtitle(when {
            peerId.matchesChatId() -> getString(R.string.conversation)
            peerId.matchesGroupId() -> getString(R.string.community)
            else -> LastSeenUtils.getFull(context, value.first, value.second, value.third)
        })
    }

    /**
     * handles setting peer's activity: one of [BaseChatMessagesViewModel.ACTIVITY_TYPING],
     * [BaseChatMessagesViewModel.ACTIVITY_VOICE], [BaseChatMessagesViewModel.ACTIVITY_NONE]
     */
    private fun onActivityChanged(activity: String) {
        when (activity) {
            BaseChatMessagesViewModel.ACTIVITY_NONE -> chatToolbarController.hideActions()
            else -> chatToolbarController.showActivity()
        }
    }

    private fun onSuggestedStickerClicked(sticker: Sticker) {
        viewModel.sendSticker(sticker)
        etInput.clear()
    }

    private fun onCanWriteChanged(canWrite: CanWrite) {
        rlCantWrite.setVisible(!canWrite.allowed)
    }

    private fun onSwipedToReply(position: Int) {
        val message = adapter.items.getOrNull(position)?.message ?: return

        attachedAdapter.fwdMessages = "${message.id}"
        attachedAdapter.isReply = true
    }

    private fun onAttachmentsSelected(attachments: List<Attachment>) {
        val maxOrder = attachedAdapter.maxOrder
        attachments.forEachIndexed { index, attachment ->
            attachedAdapter.addWithOrder(attachment, index + maxOrder)
        }
    }

    private fun onSelectedFromGallery(paths: List<DeviceItem>) {
        val maxOrder = attachedAdapter.maxOrder
        paths.forEachIndexed { index, deviceItem ->

            inputController?.addItemAsBeingLoaded(deviceItem.path)
            when (deviceItem.type) {
                DeviceItem.Type.PHOTO -> viewModel.attachPhoto(deviceItem.path) { path, attachment ->
                    inputController?.removeItemAsLoaded(path)
                    attachedAdapter.addWithOrder(attachment, index + maxOrder)
                }
                DeviceItem.Type.VIDEO -> viewModel.attachVideo(deviceItem.path) { path, attachment ->
                    inputController?.removeItemAsLoaded(path)
                    attachedAdapter.addWithOrder(attachment, index + maxOrder)
                }
                DeviceItem.Type.DOC -> viewModel.attachDoc(deviceItem.path) { path, attachment ->
                    inputController?.removeItemAsLoaded(path)
                    attachedAdapter.addWithOrder(attachment, index + maxOrder)
                }
            }
        }
    }

    private fun onImageSelected(path: String) {
        viewModel.attachPhoto(path) { pathAttached, attachment ->
            inputController?.removeItemAsLoaded(pathAttached)
            attachedAdapter.addWithOrder(attachment, attachedAdapter.maxOrder)
        }
        inputController?.addItemAsBeingLoaded(path)
    }

    private fun showMentionedMembers(members: List<User>) {
        if (members.isEmpty() && rvMentionedMembers.isShown) {
            rvMentionedMembers.fadeOut(200L) {
                rvMentionedMembers?.hide()
            }
        } else if (members.isNotEmpty() && !rvMentionedMembers.isShown) {
            rvMentionedMembers.show()
            rvMentionedMembers.fadeIn(200L)
        }
        if (members.size > MEMBERS_MAX) {
            membersAdapter.update(members.take(MEMBERS_MAX))
        } else {
            membersAdapter.update(members)
        }
    }

    protected fun onDocSelected(path: String) {
        viewModel.attachDoc(path) { pathAttached, attachment ->
            inputController?.removeItemAsLoaded(pathAttached)
            attachedAdapter.addWithOrder(attachment, attachedAdapter.maxOrder)
        }
        inputController?.addItemAsBeingLoaded(path)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ATTACH -> {
                data?.extras?.apply {
                    getParcelableArrayList<Attachment>(AttachFragment.ARG_ATTACHMENTS)
                            ?.let(::onAttachmentsSelected)
                    getParcelableArrayList<DeviceItem>(AttachFragment.ARG_PATHS)
                            ?.let(::onSelectedFromGallery)
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        chatToolbarController.hideActions()
    }

    override fun getAdapterSettings() = MessagesAdapter.Settings(
            isImportant = false
    )

    override fun getAdapterCallback() = MessageCallback()

    override fun getAttachmentsCallback() = AttachmentsCallback(requireContext())

    companion object {

        const val MEMBERS_MAX = 5

        const val ARG_PEER_ID = "peerId"
        const val ARG_TITLE = "title"
        const val ARG_FORWARDED = "forwarded"
        const val ARG_PHOTO = "photo"
        const val ARG_SHARE_TEXT = "shareText"
        const val ARG_SHARE_IMAGE = "shareImage"

        const val REQUEST_ATTACH = 2653
    }

    inner class MessageCallback : MessagesAdapter.Callback {
        override fun onClicked(message: Message) {
            val items = arrayListOf(
                    ContextPopupItem(R.drawable.ic_copy_popup, R.string.copy) {
                        copyToClip(message.text)
                    },
                    ContextPopupItem(R.drawable.ic_reply_popup, R.string.reply) {
                        attachedAdapter.fwdMessages = "${message.id}"
                        attachedAdapter.isReply = true
                    },
                    ContextPopupItem(R.drawable.ic_transfer_popup, R.string.forward) {
                        DialogsForwardActivity.launch(context, forwarded = "${message.id}")
                    },
                    ContextPopupItem(R.drawable.ic_delete_popup, R.string.delete) {
                        val callback = { forAll: Boolean ->
                            viewModel.deleteMessages(message.id.toString(), forAll)
                        }
                        if (message.isDeletableForAll()) {
                            showDeleteMessagesDialog(callback)
                        } else {
                            context?.let {
                                showDeleteDialog(it, getString(R.string.this_message)) { callback.invoke(false) }
                            }
                        }
                    }
            )

            if (!Prefs.markAsRead) {
                items.add(ContextPopupItem(R.drawable.ic_eye, R.string.mark_as_read) {
                    viewModel.markAsRead(listOf(message.id))
                })
            }

            items.add(if (message.important) {
                ContextPopupItem(R.drawable.ic_star_crossed, R.string.unmark) {
                    viewModel.unmarkAsImportant(message.id.toString())
                }
            } else {
                ContextPopupItem(R.drawable.ic_star_popup, R.string.markasimportant) {
                    viewModel.markAsImportant(message.id.toString())
                }
            })
            if (message.isEditable()) {
                items.add(ContextPopupItem(R.drawable.ic_edit_popup, R.string.edit) {
                    showEditMessageDialog(message)
                })
            }
            createContextPopup(context ?: return, items).show()
        }

        override fun onUserClicked(userId: Int) {
            ChatOwnerActivity.launch(context, userId)
        }
    }

    inner class AttachmentsCallback(context: Context) : AttachmentsInflater.DefaultCallback(context) {

        override fun onVideoClicked(video: Video) {
            viewModel.loadVideo(context ?: return, video, { playerUrl ->
                BrowsingUtils.openUrl(context, playerUrl)
            }, { error ->
                showError(context, error)
            })
        }

        override fun onEncryptedDocClicked(doc: Doc) {
            this@BaseChatMessagesFragment.onEncryptedDocClicked(doc)
        }
    }

    private inner class InputCallback : ChatInputController.ChatInputCallback {

        override fun onRichContentAdded(filePath: String) {
            if (filePath.endsWith("gif", ignoreCase = true)) {
                onDocSelected(filePath)
            } else {
                onImageSelected(filePath)
            }
        }

        override fun onStickerClicked(sticker: Sticker) {
            viewModel.sendSticker(sticker, attachedAdapter.replyTo)
            attachedAdapter.clear()
        }

        override fun onSendClick() {
            sendMessage()
        }

        override fun onScheduleClick(whenMs: Long) {
            viewModel.scheduleMessage(
                    context = requireContext(),
                    whenMs = whenMs,
                    text = etInput.asText(),
                    attachments = attachedAdapter.asString(),
                    forwardedMessages = attachedAdapter.fwdMessages
            )
            etInput.clear()
            attachedAdapter.clear()
        }

        override fun onSelfDeletingClick(timeToLive: Int) {
            sendMessage(timeToLive)
        }

        override fun hasMicPermissions(): Boolean {
            val hasPermissions = permissionHelper.hasRecordAudioPermissions()
            if (!hasPermissions) {
                permissionHelper.request(arrayOf(PermissionHelper.RECORD_AUDIO)) {}
            }
            return hasPermissions
        }

        override fun onAttachClick() {
            startFragmentForResult<AttachFragment>(REQUEST_ATTACH)
        }

        override fun onVoiceRecordingInvoke() {
            viewModel.setActivity(type = BaseChatMessagesViewModel.ACTIVITY_VOICE)
        }

        override fun onTypingInvoke() {
            viewModel.setActivity(type = BaseChatMessagesViewModel.ACTIVITY_TYPING)
        }

        override fun onVoiceRecorded(fileName: String) {
            inputController

                    ?.addItemAsBeingLoaded(fileName)
            viewModel.attachVoice(fileName) {
                inputController?.removeItemAsLoaded(it)
            }
        }

        override fun onStickersSuggested(stickers: List<Sticker>) {
            stickersAdapter.update(stickers)
            if (stickers.isEmpty() && rvStickersSuggestion.isShown) {
                rvStickersSuggestion.fadeOut(200L) {
                    rvStickersSuggestion?.hide()
                }
            } else if (stickers.isNotEmpty() && !rvStickersSuggestion.isShown) {
                rvStickersSuggestion.show()
                rvStickersSuggestion.fadeIn(200L)
            }
        }

        override fun onMention(query: String?) {
            if (peerId.matchesChatId() && query != null) {
                viewModel.getMatchingMembers(query)
            } else {
                this@BaseChatMessagesFragment.showMentionedMembers(listOf())
            }
        }

        private fun sendMessage(timeToLive: Int? = null) {
            val replyTo = attachedAdapter.replyTo
            val forwarded = if (replyTo == null) {
                attachedAdapter.fwdMessages
            } else {
                null
            }
            viewModel.sendMessage(
                    text = etInput.asText(),
                    attachments = attachedAdapter.asString(),
                    forwardedMessages = forwarded,
                    replyTo = replyTo,
                    timeToLive = timeToLive
            )
            etInput.clear()
            attachedAdapter.clear()
        }
    }
}