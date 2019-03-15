package com.twoeightnine.root.xvii.chats.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.BaseAdapter
import com.twoeightnine.root.xvii.adapters.CommonPagerAdapter
import com.twoeightnine.root.xvii.chats.BottomSheetController
import com.twoeightnine.root.xvii.chats.ChatInputController
import com.twoeightnine.root.xvii.chats.VoiceRecorder
import com.twoeightnine.root.xvii.chats.adapters.ChatAdapter
import com.twoeightnine.root.xvii.chats.fragments.attach.DocAttachFragment
import com.twoeightnine.root.xvii.chats.fragments.attach.PhotoAttachFragment
import com.twoeightnine.root.xvii.chats.fragments.attach.VideoAttachFragment
import com.twoeightnine.root.xvii.chats.fragments.attachments.AttachmentsFragment
import com.twoeightnine.root.xvii.dialogs.fragments.DialogFwFragment
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.Doc
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.mvp.presenter.ChatFragmentPresenter
import com.twoeightnine.root.xvii.mvp.view.ChatFragmentView
import com.twoeightnine.root.xvii.profile.fragments.ProfileFragment
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.FingerPrintAlertDialog
import com.twoeightnine.root.xvii.views.LoadingDialog
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
import com.twoeightnine.root.xvii.views.emoji.EmojiKeyboard
import com.twoeightnine.root.xvii.views.photoviewer.ImageViewerActivity
import kotlinx.android.synthetic.main.chat_input_panel.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.toolbar.*
import javax.inject.Inject

class ChatFragment : BaseFragment(), ChatFragmentView, BaseAdapter.OnMultiSelected {

    private lateinit var message: Message

    var fwdMessages = ""

    private var dialogLoading: LoadingDialog? = null

    private var lastOnline = ""
    private lateinit var imut: ImageUtils

    @Inject
    lateinit var presenter: ChatFragmentPresenter
    @Inject
    lateinit var apiUtils: ApiUtils

    private lateinit var pagerAdapter: CommonPagerAdapter
    private lateinit var bottomSheet: BottomSheetController<RelativeLayout>
    private lateinit var inputController: ChatInputController
    private lateinit var voiceController: VoiceRecorder
    private lateinit var adapter: ChatAdapter
    private lateinit var emojiKeyboard: EmojiKeyboard
    private lateinit var permissionHelper: PermissionHelper

    private val handler = Handler()

    companion object {

        fun newInstance(dialog: Message): ChatFragment {
            val fragment = ChatFragment()
            fragment.message = dialog
            return fragment
        }

        fun newInstance(dialog: Message, fwdMessages: String): ChatFragment {
            val fragment = ChatFragment()
            fragment.message = dialog
            fragment.fwdMessages = fwdMessages
            return fragment
        }

        const val REQUEST_PERMISSIONS = 3301

    }

    override fun getLayout() = R.layout.fragment_chat

    override fun bindViews(view: View) {
        initAdapter()
        initInput()
        initEmojiKb()
        initRefresh()
        initBottomSheet()
        initVoice()
        initMultiAction()
        App.appComponent?.inject(this)
        try {
            presenter.view = this
            presenter.dialog = message
            presenter.initCrypto()
            presenter.initAttachments(safeActivity, ::onAttachCounterChanged)
            presenter.subscribe()
        } catch (e: Exception) {
            Lg.i("bindViews: " + e.message)
            if (BuildConfig.DEBUG) {
                showError(activity, "" + e.message)
            }
            restartApp()
        }
        initPager()

        Style.forAll(rlMultiAction)
        Style.forViewGroupColor(rlHideBottom)
        Style.forFAB(fabHasMore)
        Style.forViewGroupColor(rlTyping)
        Style.forAll(rlBack)
        Style.forAll(rlMultiAction)
        Style.forTabLayout(tabsBottom)
        val d2 = rlInputContainer.background
        Style.forFrame(d2)
        rlInputContainer.background = d2

        if (fwdMessages.isNotEmpty()) {
            handler.postDelayed({ presenter.attachUtils.forwarded = fwdMessages }, 1000L)
        }
        if (Prefs.chatBack.isNotEmpty()) {
            try {
                flContainer.backgroundImage = Drawable.createFromPath(Prefs.chatBack)
            } catch (e: Exception) {
                Prefs.chatBack = ""
                showError(activity, e.message ?: "background not found")
            }
        }
        permissionHelper = PermissionHelper(this)
    }

    private fun onAttachCounterChanged(count: Int) {
        inputController.setAttachedCount(count)
    }

    override fun onNew(view: View) {
        try {
            presenter.loadCachedHistory()
        } catch (e: Exception) {
            e.printStackTrace()
            restartApp()
        }
    }

    override fun onRecovered(view: View) {
        adapter.stopLoading(presenter.getSaved(), true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try {
            rootActivity.title = message.title
            onChangeOnline(message.online == 1)
            imut = ImageUtils(rootActivity)
            toolbar?.setOnClickListener {
                hideKeyboard(safeActivity)
                if (message.chatId == 0 && message.userId > 0) {
                    rootActivity.loadFragment(ProfileFragment.newInstance(message.userId))
                } else if (message.chatId != 0) {
                    rootActivity.loadFragment(ChatInfoFragment.newInstance(message))
                }
            }
        } catch (e: UninitializedPropertyAccessException) {
            restartApp()
        }
    }

    fun initAdapter() {
        adapter = ChatAdapter(
                safeActivity,
                ::loadMore,
                ::onClick,
                ::onLongClick,
                { rootActivity.loadFragment(ProfileFragment.newInstance(it)) },
                ::onDocDecryptClicked,
                { apiUtils.showPhoto(safeActivity, it.photoId, it.accessKey) },
                { apiUtils.openVideo(safeActivity, it) }
        )
        adapter.trier = { loadMore(adapter.itemCount) }
        adapter.multiListener = this
        val llm = androidx.recyclerview.widget.LinearLayoutManager(activity)
        llm.stackFromEnd = true
        rvChatList.layoutManager = llm
        rvChatList.adapter = adapter
        rvChatList.itemAnimator = null

        fabHasMore.setOnClickListener { rvChatList.scrollToPosition(adapter.itemCount - 1) }
        rvChatList.setOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (fabHasMore.visibility != View.VISIBLE &&
                        adapter.lastVisiblePosition() != adapter.itemCount - 1) {
                    fabHasMore.visibility = View.VISIBLE
                    val alpha = AlphaAnimation(0f, 1f)
                    alpha.duration = 200
                    fabHasMore.startAnimation(alpha)
                } else if (fabHasMore.visibility != View.INVISIBLE
                        && adapter.lastVisiblePosition() == adapter.itemCount - 1) {
                    val alpha = AlphaAnimation(1f, 0f)
                    alpha.duration = 200
                    fabHasMore.startAnimation(alpha)
                    fabHasMore.visibility = View.INVISIBLE
                }
            }
        })
    }

    private fun initPager() {
        pagerAdapter = CommonPagerAdapter(childFragmentManager)
        pagerAdapter.add(AttachedFragment.newInstance(presenter.attachUtils), getString(R.string.attached))
        pagerAdapter.add(GalleryFragment.newInstance(::onImagesSelected), getString(R.string.device_photos))
        pagerAdapter.add(StickersFragment.newInstance(::onStickerSelected), getString(R.string.stickers))
        pagerAdapter.add(PhotoAttachFragment.newInstance(::onAttachmentsSelected), getString(R.string.photos))
        pagerAdapter.add(VideoAttachFragment.newInstance { onAttachmentsSelected(mutableListOf(it)) }, getString(R.string.videos))
        pagerAdapter.add(DocAttachFragment.newInstance { onAttachmentsSelected(mutableListOf(it)) }, getString(R.string.docs))
        vpAttach.adapter = pagerAdapter
        vpAttach.offscreenPageLimit = 5
        tabsBottom.setupWithViewPager(vpAttach, true)
        vpAttach.currentItem = 1 // gallery
    }

    private fun initEmojiKb() {
        emojiKeyboard = EmojiKeyboard(flContainer, safeActivity, inputController::addEmoji)
        emojiKeyboard.setSizeForSoftKeyboard()
        emojiKeyboard.onSoftKeyboardOpenCloseListener =
                object : EmojiKeyboard.OnSoftKeyboardOpenCloseListener {
                    override fun onKeyboardOpen(keyBoardHeight: Int) {}

                    override fun onKeyboardClose() {
                        if (emojiKeyboard.isShowing) {
                            emojiKeyboard.dismiss()
                        }
                    }
                }
    }

    private fun initRefresh() {
        swipeContainer.direction = SwipyRefreshLayoutDirection.BOTTOM
        swipeContainer.setOnRefreshListener { presenter.loadHistory(withClear = true) }
        swipeContainer.setDistanceToTriggerSync(50)
    }

    private fun initBottomSheet() {
        bottomSheet = BottomSheetController(rlBottom, rlHideBottom) { vpAttach.currentItem = 1 } // reset to gallery
    }

    private fun initVoice() {
        voiceController = VoiceRecorder(safeContext, object : VoiceRecorder.RecorderCallback {
            override fun onVisibilityChanged(visible: Boolean) {
                rlRecord.visibility = if (visible) View.VISIBLE else View.GONE
            }

            override fun onTimeUpdated(time: Int) {
                tvRecord.text = secToTime(time)
                if (time % 5 == 1) {
                    presenter.setAudioMessaging()
                }
            }

            override fun onRecorded(fileName: String) {
                inputController.addItemAsBeingLoaded(fileName)
                presenter.attachVoice(fileName)
            }

            override fun onError(error: String) {
                showError(context, error)
            }
        })
    }

    private fun initInput() {
        inputController = ChatInputController(
                ivSend, ivMic, ivAttach, pbAttach, rlAttachCount,
                tvAttachCount, ivEmoji, etInput,
                { onEmojiClicked() },
                { onSend(etInput.text.toString()) },
                {
                    permissionHelper.doOrRequest(
                            PermissionHelper.RECORD_AUDIO,
                            R.string.no_access_to_mic,
                            R.string.need_access_to_mic
                    ) {
                        voiceController.startRecording()
                    }
                },
                { voiceController.stopRecording(it) },
                { bottomSheet.open() },
                { presenter.setTyping() }
        )
    }

    private fun initMultiAction() {
        ivCancelMulti.setOnClickListener {
            adapter.clearMultiSelect()
        }
        ivMenuMulti.setOnClickListener { showMultiSelectPopup() }
        ivForwardMulti.setOnClickListener { rootActivity.loadFragment(DialogFwFragment.newInstance(adapter.multiSelect)) }
        ivReplyMulti.setOnClickListener {
            presenter.attachUtils.forwarded = adapter.multiSelect
            adapter.clearMultiSelect()
        }
    }

    private fun loadMore(offset: Int) {
        if (isOnline()) {
            presenter.loadHistory(offset)
        }
    }

    private fun onClick(position: Int) {
        if (position !in adapter.items.indices) return
        val message = adapter.items[position]
        adapter.multiSelect(message.id)
        adapter.notifyItemChanged(position)
    }

    private fun onLongClick(position: Int): Boolean {
        if (position !in adapter.items.indices) return true

        val message = adapter.items[position]
        getContextPopup(safeActivity, R.layout.popup_message) {
            when (it.id) {
                R.id.llCopy -> copyToClip(message.body ?: "")
                R.id.llEdit -> showEditMessageDialog(message)
                R.id.llReply -> presenter.attachUtils.forwarded = "${message.id}"
                R.id.llForward -> {
                    rootActivity.loadFragment(DialogFwFragment.newInstance("${message.id}"))
                }
                R.id.llDelete -> {
                    val callback = { forAll: Boolean ->
                        presenter.deleteMessages(mutableListOf(message.id), forAll)
                        CacheHelper.deleteMessagesAsync(mutableListOf(message.id))
                    }
                    if (message.isOut && time() - message.date < 3600 * 24) {
                        showDeleteMessagesDialog(callback)
                    } else {
                        showDeleteDialog(safeActivity, { callback.invoke(false) })
                    }
                }
                R.id.llDecrypt -> {
                    message.body = getDecrypted(message.body)
                    adapter.notifyItemChanged(position)
                }
                R.id.llMarkImportant ->
                    presenter.markAsImportant(
                            mutableListOf(message.id),
                            if (message.isImportant) 0 else 1
                    )
            }
        }.show()
        return true
    }

    private fun showMultiSelectPopup() {
        val selectedList = adapter.multiSelectRaw
        getContextPopup(safeActivity, R.layout.popup_message_multiselect) {
            when (it.id) {
                R.id.llDecrypt -> {
                    decrypt(selectedList)
                    adapter.clearMultiSelect()
                }
                R.id.llDelete -> {
                    showDeleteMessagesDialog {
                        // i haven't found how to make copy
                        val mids = MutableList(selectedList.size) { selectedList[it] }
                        presenter.deleteMessages(mids, it)
                        CacheHelper.deleteMessagesAsync(mids)
                        adapter.clearMultiSelect()
                    }
                }
                R.id.llMarkImportant -> {
                    presenter.markAsImportant(selectedList, 1)
                    adapter.clearMultiSelect()
                }
            }
        }.show()
    }

    private fun showDeleteMessagesDialog(callback: (Boolean) -> Unit) {
        val dialog = AlertDialog.Builder(safeContext)
                .setMessage(R.string.wanna_delete_messages)
                .setNeutralButton(R.string.delete_for_all) { _, _ -> callback.invoke(true) }
                .setPositiveButton(R.string.delete_only_for_me) { _, _ -> callback.invoke(false) }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        dialog.show()
        Style.forDialog(dialog)
    }

    private fun showEditMessageDialog(message: Message) {
        if (message.isOut && time() - message.date < 3600 * 24) {
            TextInputAlertDialog(
                    safeContext,
                    getString(R.string.edit_message), "",
                    message.body ?: "",
                    { presenter.editMessage(message.id, it) }
            ).show()
        } else {
            showError(context, R.string.unable_to_edit_message)
        }
    }

    private fun onDocDecryptClicked(doc: Doc) {
        dialogLoading?.dismiss()
        dialogLoading = LoadingDialog(
                safeContext,
                safeContext.getString(R.string.decrypting_image)
        )
        dialogLoading?.show()
        presenter.decryptDoc(safeContext, doc) {
            dialogLoading?.dismiss()
            if (it.isNotEmpty()) {
                val fileForLog = if (BuildConfig.DEBUG) it else ""
                Lg.i("show decrypted $fileForLog")
                ImageViewerActivity.viewImage(safeContext, "file://$it")
            } else {
                showError(context, R.string.invalid_file)
            }
        }
    }

    override fun onNonEmpty() {
        rlMultiAction.visibility = View.VISIBLE
    }

    override fun onEmpty() {
        rlMultiAction.visibility = View.GONE
    }

    private fun decrypt(mids: MutableList<Int>) {
        adapter.items
                .filter { it.id in mids }
                .forEach { it.body = getDecrypted(it.body) }
        adapter.notifyDataSetChanged()
    }

    private fun onEmojiClicked() {
        if (!emojiKeyboard.isShowing) {
            if (emojiKeyboard.isKeyBoardOpen) {
                emojiKeyboard.showAtBottom()
            } else {
                etInput.isFocusableInTouchMode = true
                etInput.requestFocus()
                val inputMethodManager = safeContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(etInput, InputMethodManager
                        .SHOW_IMPLICIT)
                emojiKeyboard.showAtBottomPending()
            }
        } else {
            emojiKeyboard.dismiss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.menu_chat, menu)
        menu?.findItem(R.id.menu_fingerprint)?.isVisible = message.chatId == 0 && message.userId > 0 && message.userId < 1000000000
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        menu?.findItem(R.id.menu_encrypt)?.isChecked = presenter.isEncrypted
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_encrypt -> {
                item.isChecked = !item.isChecked
                presenter.isEncrypted = item.isChecked
                showCommon(activity, if (item.isChecked) R.string.enc_on else R.string.enc_off)
                true
            }
            R.id.menu_keys -> {
                hideKeyboard(safeActivity)
                showKeysDialog()
                true
            }
            R.id.menu_attachments -> {
                rootActivity.loadFragment(AttachmentsFragment.newInstance(
                        if (message.chatId == 0)
                            message.userId
                        else
                            2000000000 + message.chatId
                ))
                hideKeyboard(safeActivity)
                true
            }
            R.id.menu_fingerprint -> {
                val fingerprint = presenter.crypto.getFingerPrint()
                val keyType = presenter.crypto.keyType
                FingerPrintAlertDialog(safeContext, fingerprint, keyType).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showKeysDialog() {
        getContextPopup(safeActivity, R.layout.popup_keys) {
            when (it.id) {
                R.id.llRandomKey -> {
                    if (message.chatId == 0 && message.userId > 0 && message.userId < 1000000000) {
                        presenter.isEncrypted = false
                        safeActivity.invalidateOptionsMenu()
                        keyGenerationHint()
                    } else {
                        showError(activity, R.string.no_exchg_in_chats)
                    }
                }
                R.id.llUserKey -> showKeyInputDialog()
                R.id.llDefaultKey -> {
                    presenter.setDefaultKey()
                    showCommon(activity, R.string.key_reset)
                }
            }
        }.show()
    }

    private fun showKeyInputDialog() {
        TextInputAlertDialog(
                safeActivity,
                getString(R.string.user_key),
                getString(R.string.secure_length), "", {
            presenter.setUserKey(it)
            presenter.isEncrypted = true
            safeActivity.invalidateOptionsMenu()
            showCommon(activity, getString(R.string.key_set))
        }
        ).show()
    }

    private fun onSend(text: String) {
        if (text.isNotEmpty() || presenter.attachUtils.count > 0) {
            presenter.send(text)
            etInput.setText("")
        }
    }

    private fun showPermissionDialog() {
        val dialog = AlertDialog.Builder(safeActivity)
                .setMessage(R.string.permissions_info)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    requestPermissions(arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO
                    ), REQUEST_PERMISSIONS)
                }
                .create()
        dialog.show()
        Style.forDialog(dialog)
    }

    private fun onAttachmentsSelected(attachments: MutableList<Attachment>) {
        attachments.forEach {
            presenter.attachUtils.add(it)
        }
        bottomSheet.close()
    }

    private fun onImagesSelected(paths: MutableList<String>) {
        paths.forEach {
            presenter.attachPhoto(it, context = context)
            inputController.addItemAsBeingLoaded(it)
        }
        bottomSheet.close()
    }

    override fun onVoiceUploaded(path: String) {
        inputController.removeItemAsLoaded(path)
        onSend(etInput.text.toString())
    }

    private fun onStickerSelected(sticker: Attachment.Sticker) {
        presenter.sendSticker(sticker)
        bottomSheet.close()
    }

    private fun getDecrypted(text: String?) = getString(R.string.decrypted, presenter.crypto.decrypt(text
            ?: ""))


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val path = imut.getPath(requestCode, data)
            presenter.attachPhoto(path ?: "")
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.isShown = true
        if (Prefs.markAsRead && adapter.items.isNotEmpty()) {
            presenter.markAsRead(adapter.items[adapter.items.size - 1].id)
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.isShown = false
    }

    override fun showLoading() {
        adapter.startLoading()
    }

    override fun hideLoading() {
        adapter.stopLoading()
    }

    override fun showError(error: String) {
        adapter.setErrorLoading()
        showError(activity, error)
        swipeContainer?.isRefreshing = false
        Lg.wtf("in chat error: $error")
    }

    override fun onPhotoUploaded(path: String) {
        inputController.removeItemAsLoaded(path)
    }

    override fun onHistoryLoaded(history: MutableList<Message>) {
        val needToScroll = adapter.itemCount < 2
        adapter.stopLoading(history, true)
        swipeContainer?.isRefreshing = false

        if (needToScroll) {
            var unreadPos = adapter.itemCount - 1
            for (pos in adapter.items.indices.reversed()) {
                if (!adapter.items[pos].isRead &&
                        !adapter.items[pos].isOut) {
                    unreadPos = pos
                } else {
                    break
                }
            }
            rvChatList?.scrollToPosition(unreadPos)
        }
    }

    override fun onMessageAdded(message: Message) {
        if (presenter.isEncrypted && message.body?.matchesXviiKey() ?: false) {
            message.body = getDecrypted(message.body)
        }
        val wasAtEnd = adapter.isAtEnd
        adapter.add(message)
        if (wasAtEnd) {
            rvChatList.scrollToPosition(adapter.itemCount - 1)
        }
    }

    override fun onHistoryClear() {
        adapter.clear()
    }

    override fun onSentError(text: String) {
        etInput.setText(text)
    }

    override fun onShowTyping() {
        rlTyping?.visibility = View.VISIBLE
        handler.postDelayed({ onHideTyping() }, 4800L)
    }

    override fun onHideTyping() {
        rlTyping?.visibility = View.INVISIBLE
    }

    override fun onShowRecordingVoice() {
        rlRecordingVoice?.visibility = View.VISIBLE
        handler.postDelayed({ onHideRecordingVoice() }, 4800L)
    }

    override fun onHideRecordingVoice() {
        rlRecordingVoice?.visibility = View.INVISIBLE
    }

    override fun onChangeOnline(isOnline: Boolean) {
        if (message.chatId != 0) {
            lastOnline = getString(R.string.conversation)
        } else if (message.userId < 0) {
            lastOnline = getString(R.string.community)
        } else {
            lastOnline = if (isOnline) getString(R.string.online) else getString(R.string.offline)
        }
        rootActivity.supportActionBar?.subtitle = lastOnline
    }

    override fun onReadOut(mid: Int) {
        for (position in adapter.items.indices) {
            val message = adapter.items[position]
            if (message.id <= mid && !message.isRead) {
                message.setRead(1)
                CacheHelper.saveMessageAsync(message)
                adapter.notifyItemChanged(position)
            }
        }
    }

    override fun onMessagesDeleted(mids: MutableList<Int>) {
        Lg.i("mids = $mids")
        adapter.items
                .filter { it.id in mids }
                .forEach { adapter.remove(it) }

    }

    override fun onMessageEdited(mid: Int, newText: String) {
        for (pos in adapter.items.indices) {
            if (adapter.items[pos].id == mid) {
                val mess = adapter.items[pos]
                mess.body = newText
                adapter.update(pos, mess)
            }
        }
    }

    private fun keyGenerationHint() {
        val alertDialog = AlertDialog.Builder(safeContext)
                .setMessage(R.string.generation_dh_hint)
                .setPositiveButton(android.R.string.ok, { _, _ -> presenter.startKeyExchange() })
                .create()
        alertDialog.show()
        Style.forDialog(alertDialog)
    }

    override fun onKeyGenerating() {
        dialogLoading?.dismiss()
        dialogLoading = LoadingDialog(
                safeActivity,
                getString(R.string.generating_keys),
                false
        )
        dialogLoading?.show()
    }

    override fun onKeySent() {
        dialogLoading?.dismiss()
    }

    override fun onKeyReceived(key: String, isWaiting: Boolean) {
        if (isWaiting) {
            presenter.finishKeyExchange(key)
        } else {
            presenter.supportKeyExchange(key)
        }
    }

    override fun onKeysExchanged() {
        presenter.isEncrypted = true
        safeActivity.invalidateOptionsMenu()
        showCommon(activity, R.string.key_exchanged)
    }

    override fun onKeyExchangeFailed() {
        showError(context, R.string.key_xchg_failed)
    }

    override fun onCacheRestored() {
        if (isOnline()) {
            swipeContainer.isRefreshing = true
            presenter.loadHistory(withClear = true)
        } else {
            swipeContainer.isRefreshing = false
        }
    }

    override fun onDetach() {
        super.onDetach()
        presenter.unsubscribe()
    }

    override fun onBackPressed(): Boolean {
        if (bottomSheet.isOpen()) {
            bottomSheet.close()
            return true
        }
        return false
    }
}