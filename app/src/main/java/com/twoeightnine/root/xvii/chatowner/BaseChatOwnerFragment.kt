package com.twoeightnine.root.xvii.chatowner

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.DrawableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.fragment_chat_owner.ivAvatar
import kotlinx.android.synthetic.main.fragment_chat_owner.nsvContent
import kotlinx.android.synthetic.main.fragment_chat_owner.tvInfo
import kotlinx.android.synthetic.main.fragment_chat_owner.tvTitle
import kotlinx.android.synthetic.main.fragment_chat_owner.vShadow
import kotlinx.android.synthetic.main.fragment_chat_owner_user.*
import kotlinx.android.synthetic.main.item_chat_owner_field.view.*
import kotlinx.android.synthetic.main.toolbar.*

abstract class BaseChatOwnerFragment<T : ChatOwner> : BaseFragment() {

    private val peerId by lazy {
        arguments?.getInt(ARG_PEER_ID) ?: 0
    }
    private var chatOwner: ChatOwner? = null

    protected lateinit var viewModel: ChatOwnerViewModel

    abstract fun getChatOwnerClass(): Class<T>

    abstract fun bindChatOwner(chatOwner: T?)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val behavior = BottomSheetBehavior.from(nsvContent)
        behavior.setBottomSheetCallback(ProfileBottomSheetCallback(activity ?: return))
        fabOpenChat.setOnClickListener {
            chatOwner?.also {
                ChatActivity.launch(context, it)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.background = TransitionDrawable(arrayOf(
                ColorDrawable(Color.TRANSPARENT),
                ColorDrawable(ColorManager.toolbarColor)
        ))
        setTitle("")

        viewModel = ViewModelProviders.of(this)[ChatOwnerViewModel::class.java]
        viewModel.chatOwner.observe(viewLifecycleOwner, Observer(::onChatOwnerLoaded))
        viewModel.loadChatOwner(peerId, getChatOwnerClass())
    }

    @Suppress("UNCHECKED_CAST")
    private fun onChatOwnerLoaded(data: Wrapper<ChatOwner>) {
        if (data.data != null) {
            chatOwner = data.data
            chatOwner?.apply {
                ivAvatar.load(getAvatar())
                tvTitle.text = getTitle()
                context?.also {
                    tvInfo.text = getInfoText(it)
                    getPrivacyInfo(it).also { privacyInfo ->
                        ivWarning.setVisible(privacyInfo != null)
                        tvPrivacy.setVisible(privacyInfo != null)
                        privacyInfo?.also { tvPrivacy.text = it }
                    }
                }
                if (Prefs.lowerTexts) {
                    tvTitle.lower()
                    tvInfo.lower()
                }
                swNotifications.isChecked = viewModel.getShowNotifications(getPeerId())
                resetValues()
                bindChatOwner(this as? T)
            }
        }
    }

    private fun resetValues() {
        llContainer.removeAllViews()
    }

    protected fun addValue(
            @DrawableRes icon: Int,
            text: String?,
            onClick: ((String) -> Unit)? = null,
            onLongClick: ((String) -> Unit)? = null
    ) {
        if (text.isNullOrBlank()) return

        with(View.inflate(context, R.layout.item_chat_owner_field, null)) {
            if (icon != 0) {
                ivIcon.setImageResource(icon)
            }
            tvValue.text = text.toLowerCase()
            onClick?.also {
                rlItem.setOnClickListener { onClick(text) }
            }
            onLongClick?.also {
                rlItem.setOnLongClickListener { onLongClick(text); true }
            }
            llContainer.addView(this)
        }
    }

    override fun onDestroyView() {
        chatOwner?.getPeerId()?.also { peerId ->
            viewModel.setShowNotifications(peerId, swNotifications.isChecked)
        }
        super.onDestroyView()
    }

    companion object {

        const val ARG_PEER_ID = "peerId"
        const val BOTTOM_SHEET_TRIGGER_CALLBACK = 0.97
    }

    private inner class ProfileBottomSheetCallback(activity: Activity) : BottomSheetBehavior.BottomSheetCallback() {

        private var toolbarColored = false
        private val imageHeight = resources.getDimensionPixelSize(R.dimen.profile_avatar_height)
        private val screenHeight = activity.let {
            val displayMetrics = DisplayMetrics()
            it.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }

        override fun onSlide(p0: View, offset: Float) {
            when {
                offset <= 0f -> return
                offset > BOTTOM_SHEET_TRIGGER_CALLBACK -> if (!toolbarColored && shouldColorToolbar()) {
                    (toolbar.background as? TransitionDrawable)?.startTransition(0)
                    vShadow.show()
                    var title = chatOwner?.getTitle()
                    if (Prefs.lowerTexts) {
                        title = title?.toLowerCase()
                    }
                    setTitle(title ?: "")
                    toolbarColored = true
                }
                toolbarColored -> {
                    (toolbar.background as? TransitionDrawable)?.reverseTransition(0)
                    vShadow.hide()
                    setTitle("")
                    toolbarColored = false
                }
                else -> {
                    val margin = imageHeight * -offset * 0.5f
                    (ivAvatar.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
                        topMargin = margin.toInt()
                        ivAvatar.layoutParams = this
                    }
                }
            }
        }

        @SuppressLint("SwitchIntDef")
        override fun onStateChanged(p0: View, state: Int) {

        }

        private fun shouldColorToolbar() = screenHeight <= cvInfo.height
    }
}