package com.twoeightnine.root.xvii.chats.fragments.attachments

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.adapters.attachments.AudioAttachmentsAdapter
import com.twoeightnine.root.xvii.model.Audio
import com.twoeightnine.root.xvii.network.response.AttachmentsResponse
import kotlinx.android.synthetic.main.fragment_attachments_audio.*

class AudioAttachmentsFragment : BaseAttachmentsFragment<Audio>() {

    override fun getLayout() = R.layout.fragment_attachments_audio

    override fun getMedia() = "audio"

    override fun initAdapter() {
        App.appComponent?.inject(this)
        adapter = AudioAttachmentsAdapter({ loadMore() }, {

        })
        lvAudios.adapter = adapter
    }

    override fun onLoaded(response: AttachmentsResponse) {
        adapter.stopLoading(response.items
                .mapNotNull { it.attachment?.audio }
                .toMutableList())
    }

    companion object {

        fun newInstance(peerId: Int): AudioAttachmentsFragment {
            val farg = AudioAttachmentsFragment()
            farg.peerId = peerId
            return farg
        }
    }
}