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

package com.twoeightnine.root.xvii.chats.messages.deepforwarded

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerFactory
import com.twoeightnine.root.xvii.chats.attachments.AttachmentsInflater
import com.twoeightnine.root.xvii.chats.messages.Interaction
import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesViewModel
import com.twoeightnine.root.xvii.chats.messages.base.MessagesAdapter
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.utils.AppBarLifter
import com.twoeightnine.root.xvii.utils.BrowsingUtils
import com.twoeightnine.root.xvii.utils.PermissionHelper
import com.twoeightnine.root.xvii.utils.showError
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import global.msnthrp.xvii.uikit.extensions.hide
import kotlinx.android.synthetic.main.fragment_deep_forwarded.*
import javax.inject.Inject

class DeepForwardedFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseMessagesViewModel.Factory
    private lateinit var viewModel: DeepForwardedViewModel

    private val messageId by lazy { arguments?.getInt(ARG_MESSAGE_ID) }
    private val adapter by lazy {
        MessagesAdapter(
                requireContext(),
                ::loadMore,
                ForwardedMessageCallback(),
                ForwardedAttachmentsCallback(requireContext()),
                getSettings()
        )
    }

    override fun getLayoutId() = R.layout.fragment_deep_forwarded

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[DeepForwardedViewModel::class.java]

        rvForwarded.layoutManager = LinearLayoutManager(context)
        rvForwarded.adapter = adapter
        rvForwarded.addOnScrollListener(AppBarLifter(xviiToolbar))
        rvForwarded.applyBottomInsetPadding()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getInteraction().observe(viewLifecycleOwner, ::onMessageLoaded)
        viewModel.loadMessage(messageId ?: 0)
    }

    private fun onMessageLoaded(data: Wrapper<Interaction>) {
        if (data.data != null) {
            if (data.data.type == Interaction.Type.ADD) {
                progressBar.hide()
                adapter.add(data.data.messages.first())
            }
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun loadMore(offset: Int) {
        adapter.stopLoading(finished = true)
    }

    private fun getSettings() = MessagesAdapter.Settings(
            isImportant = false,
            fullDeepness = true
    )

    companion object {

        private const val ARG_MESSAGE_ID = "messageId"

        fun newInstance(arguments: Bundle?): DeepForwardedFragment {
            val fragment = DeepForwardedFragment()
            fragment.arguments = arguments
            return fragment
        }

        fun createArgs(messageId: Int) = Bundle().apply {
            putInt(ARG_MESSAGE_ID, messageId)
        }
    }

    private inner class ForwardedMessageCallback : MessagesAdapter.Callback {

        override fun onClicked(message: Message) {

        }

        override fun onUserClicked(userId: Int) {
            ChatOwnerFactory.launch(context, userId)
        }
    }

    private inner class ForwardedAttachmentsCallback(context: Context)
        : AttachmentsInflater.DefaultCallback(context, PermissionHelper(this)) {

        override fun onVideoClicked(video: Video) {
            context?.also {
                viewModel.loadVideo(it, video, { player ->
                    BrowsingUtils.openUrl(it, player, ignoreNative = true)
                }, { error ->
                    showError(it, error)
                })
            }
        }

        override fun onEncryptedDocClicked(doc: Doc) {
        }
    }
}