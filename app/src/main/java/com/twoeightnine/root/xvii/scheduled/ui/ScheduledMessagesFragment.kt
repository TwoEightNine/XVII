package com.twoeightnine.root.xvii.scheduled.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.scheduled.core.ScheduledMessage
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.fragment_scheduled_messages.*

class ScheduledMessagesFragment : BaseFragment() {

    private val viewModel by lazy {
        ViewModelProviders.of(this)[ScheduledMessagesViewModel::class.java]
    }
    private val adapter by lazy {
        ScheduledMessagesAdapter(contextOrThrow, ::onClicked)
    }
    private val translateHorizontal by lazy {
        context?.resources
                ?.getDimensionPixelSize(R.dimen.scheduled_messages_finger_translation_horizontal)
                ?.toFloat() ?: 100f
    }
    private val translateVertical by lazy {
        context?.resources
                ?.getDimensionPixelSize(R.dimen.scheduled_messages_finger_translation_vertical)
                ?.toFloat() ?: 16f
    }

    override fun getLayoutId(): Int = R.layout.fragment_scheduled_messages

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivSend.setOnClickListener {
            showToast(context, R.string.scheduled_messages_not_here)
        }

        rvMessages.layoutManager = LinearLayoutManager(context)
        rvMessages.adapter = adapter
        adapter.emptyView = rlHint

        ivSend.stylizeAnyway(tag = ColorManager.MAIN_TAG)
        rvMessages.setBottomInsetPadding()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTitle(getString(R.string.scheduled_messages_title))
        viewModel.scheduledMessages
                .observe(viewLifecycleOwner, Observer(this::onScheduledMessagesLoaded))
        viewModel.peersMap
                .observe(viewLifecycleOwner, Observer(adapter::updatePeersMap))
        viewModel.loadScheduledMessages()
    }

    private fun onScheduledMessagesLoaded(messages: List<ScheduledMessage>) {
        adapter.update(messages)
        if (messages.isEmpty()) {
            animateFinger()
        }
    }

    private fun onClicked(scheduledMessage: ScheduledMessage) {
        showConfirm(context, getString(R.string.scheduled_messages_cancel_prompt)) { yes ->
            if (yes) viewModel.cancelScheduledMessage(requireContext(), scheduledMessage)
        }
    }

    private fun animateFinger() {
        ivFinger?.animate()
                ?.scaleX(-SCALE_PRESSED)
                ?.scaleY(SCALE_PRESSED)
                ?.translationX(-translateHorizontal)
                ?.translationY(-translateVertical)
                ?.setDuration(DURATION)
                ?.setListener(EndListener {
                    ivFinger?.animate()
                            ?.setStartDelay(DELAY)
                            ?.setDuration(DURATION)
                            ?.translationX(0f)
                            ?.translationY(0f)
                            ?.scaleY(1f)
                            ?.scaleX(-1f)
                            ?.start()
                })
                ?.start()
    }

    companion object {

        private const val SCALE_PRESSED = 0.75f
        private const val DURATION = 800L
        private const val DELAY = 500L

        fun newInstance() = ScheduledMessagesFragment()
    }
}