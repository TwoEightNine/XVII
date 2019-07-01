package com.twoeightnine.root.xvii.chats.messages.deepforwarded

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.messages.base.MessagesAdapter
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.profile.activities.ProfileActivity
import kotlinx.android.synthetic.main.fragment_deep_forwarded.*

class DeepForwardedFragment : BaseFragment() {

    private val message by lazy { arguments?.getParcelable<Message>(ARG_MESSAGE) }

    private val adapter by lazy {
        MessagesAdapter(contextOrThrow, {}, ForwardedCallback(), getSettings())
    }

    override fun getLayoutId() = R.layout.fragment_deep_forwarded

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvForwarded.layoutManager = LinearLayoutManager(context)
        rvForwarded.adapter = adapter
        message?.also { adapter.add(it) }
    }

    private fun getSettings() = MessagesAdapter.Settings(
            isImportant = false,
            fullDeepness = true
    )

    companion object {

        const val ARG_MESSAGE = "message"

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
            ProfileActivity.launch(context, userId)
        }

        override fun onEncryptedFileClicked(doc: Doc) {

        }

        override fun onPhotoClicked(photo: Photo) {
            ImageViewerActivity.viewImages(context, arrayListOf(photo))
        }

        override fun onVideoClicked(video: Video) {

        }
    }
}