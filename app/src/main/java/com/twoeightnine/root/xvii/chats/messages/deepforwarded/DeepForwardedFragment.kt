package com.twoeightnine.root.xvii.chats.messages.deepforwarded

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerActivity
import com.twoeightnine.root.xvii.chats.messages.Interaction
import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesViewModel
import com.twoeightnine.root.xvii.chats.messages.base.MessagesAdapter
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.utils.AppBarLifter
import com.twoeightnine.root.xvii.utils.hide
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.web.VideoViewerActivity
import kotlinx.android.synthetic.main.fragment_deep_forwarded.*
import javax.inject.Inject

class DeepForwardedFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseMessagesViewModel.Factory
    private lateinit var viewModel: DeepForwardedViewModel

    private val messageId by lazy { arguments?.getInt(ARG_MESSAGE_ID) }
    private val adapter by lazy {
        MessagesAdapter(requireContext(), ::loadMore, ForwardedCallback(), getSettings())
    }

    override fun getLayoutId() = R.layout.fragment_deep_forwarded

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[DeepForwardedViewModel::class.java]

        rvForwarded.layoutManager = LinearLayoutManager(context)
        rvForwarded.adapter = adapter
        rvForwarded.addOnScrollListener(AppBarLifter(xviiToolbar))
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

        const val ARG_MESSAGE_ID = "messageId"

        fun newInstance(arguments: Bundle?): DeepForwardedFragment {
            val fragment = DeepForwardedFragment()
            fragment.arguments = arguments
            return fragment
        }
    }

    private inner class ForwardedCallback : MessagesAdapter.Callback {

        override fun onClicked(message: Message) {

        }

        override fun onUserClicked(userId: Int) {
            ChatOwnerActivity.launch(context, userId)
        }

        override fun onEncryptedFileClicked(doc: Doc) {

        }

        override fun onPhotoClicked(position: Int, photos: ArrayList<Photo>) {
            ImageViewerActivity.viewImages(context, photos, position)
        }

        override fun onVideoClicked(video: Video) {
            context?.also {
                viewModel.loadVideo(it, video, { player ->
                    VideoViewerActivity.launch(it, player)
                }, { error ->
                    showError(it, error)
                })
            }
        }
    }
}