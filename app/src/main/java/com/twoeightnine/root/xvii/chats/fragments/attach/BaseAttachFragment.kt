package com.twoeightnine.root.xvii.chats.fragments.attach

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.network.ApiService
import javax.inject.Inject


open class BaseAttachFragment<T> : androidx.fragment.app.Fragment() {

    open val count = 200

    var listener: ((Attachment) -> Unit)? = null

    protected lateinit var adapter: SimplePaginationAdapter<T>

    protected val safeActivity: androidx.fragment.app.FragmentActivity
        get() = activity ?: throw Exception()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?) = View.inflate(context, getLayout(), null)

    @Inject
    protected open lateinit var api: ApiService

    open fun getLayout() = R.layout.activity_root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        adapter.startLoading()
    }

    open fun initAdapter() {}
}

