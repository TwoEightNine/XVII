package com.twoeightnine.root.xvii.chats.fragments.attachments

import android.view.View
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.response.AttachmentsResponse
import com.twoeightnine.root.xvii.utils.ApiUtils
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.utils.subscribeSmart
import javax.inject.Inject


abstract class BaseAttachmentsFragment<T> : BaseOldFragment() {

    var peerId: Int = 0
    private var nextFrom: String? = null
    protected lateinit var adapter: SimplePaginationAdapter<T>

    @Inject
    lateinit var api: ApiService

    @Inject
    lateinit var apiUtils: ApiUtils

    val count = 200

    override fun bindViews(view: View) {
        initAdapter()
        adapter.startLoading()
    }

    abstract fun initAdapter()

    abstract fun getMedia(): String

    fun loadMore(offset: Int = 0) {
        getAttachments()
    }

    private fun getAttachments() {
        api.getHistoryAttachments(peerId, getMedia(), count, nextFrom ?: "")
                .subscribeSmart({
                    response ->
                    onLoaded(response)
                    nextFrom = response.nextFrom
                }, {
                    error ->
                    showError(activity, error)
                    adapter.isLoading = false
                })
    }

    abstract fun onLoaded(response: AttachmentsResponse)
}
