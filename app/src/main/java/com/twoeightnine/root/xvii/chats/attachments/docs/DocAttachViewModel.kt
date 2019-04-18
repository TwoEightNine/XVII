package com.twoeightnine.root.xvii.chats.attachments.docs

import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachViewModel
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.subscribeSmart

class DocAttachViewModel(private val api: ApiService) : BaseAttachViewModel<Doc>() {

    override fun loadAttach(offset: Int) {
        api.getDocs(COUNT, offset)
                .subscribeSmart({ response ->
                    onAttachmentsLoaded(offset, ArrayList(response.items))
                }, ::onErrorOccurred)
    }
}