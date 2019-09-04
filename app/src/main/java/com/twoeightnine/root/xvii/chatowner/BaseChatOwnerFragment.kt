package com.twoeightnine.root.xvii.chatowner

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.fragment_chat_owner.*
import kotlinx.android.synthetic.main.toolbar.*

abstract class BaseChatOwnerFragment<T : ChatOwner> : BaseFragment() {

    private val peerId by lazy {
        arguments?.getInt(ARG_PEER_ID) ?: 0
    }

    protected lateinit var viewModel: ChatOwnerViewModel

    abstract fun getChatOwnerClass(): Class<T>

    abstract fun bindChatOwner(chatOwner: T?)

    override fun getLayoutId() = R.layout.fragment_chat_owner

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val behavior = BottomSheetBehavior.from(nsvContent)
        behavior.setBottomSheetCallback(ProfileBottomSheetCallback(context?.resources ?: return))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.background = TransitionDrawable(arrayOf(
                ColorDrawable(Color.TRANSPARENT),
                ColorDrawable(ColorManager.mainColor)
        ))

        viewModel = ViewModelProviders.of(this)[ChatOwnerViewModel::class.java]
        viewModel.chatOwner.observe(viewLifecycleOwner, Observer(::onChatOwnerLoaded))
        viewModel.loadChatOwner(peerId, getChatOwnerClass())
    }

    @Suppress("UNCHECKED_CAST")
    private fun onChatOwnerLoaded(data: Wrapper<ChatOwner>) {
        if (data.data != null) {
            val chatOwner = data.data
            setTitle(chatOwner.getTitle())
            ivAvatar.load(chatOwner.getAvatar())
            tvTitle.text = chatOwner.getTitle()
            context?.also {
                tvInfo.text = chatOwner.getInfoText(it)
            }

            bindChatOwner(chatOwner as? T)
        }
    }

    companion object {

        const val ARG_PEER_ID = "peerId"
        const val BOTTOM_SHEET_TRIGGER_CALLBACK = 0.97
    }

    private inner class ProfileBottomSheetCallback(resources: Resources) : BottomSheetBehavior.BottomSheetCallback() {

        private var toolbarColored = false
        private val imageHeight = resources.getDimensionPixelSize(R.dimen.profile_avatar_height)

        override fun onSlide(p0: View, offset: Float) {
            when {
                offset <= 0f -> return
                offset > BOTTOM_SHEET_TRIGGER_CALLBACK -> if (!toolbarColored) {
                    (toolbar.background as? TransitionDrawable)?.startTransition(0)
                    vShadow.show()
                    toolbarColored = true
                }
                toolbarColored -> {
                    (toolbar.background as? TransitionDrawable)?.reverseTransition(0)
                    vShadow.hide()
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
    }
}