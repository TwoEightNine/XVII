package com.twoeightnine.root.xvii.chats.fragments.attach

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.model.Attachment
import javax.inject.Inject


open class BaseAttachFragment<T> : Fragment() {

    open val count = 200

    var listener: ((Attachment) -> Unit)? = null

    lateinit protected var adapter: SimplePaginationAdapter<T>

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?) = View.inflate(context, getLayout(), null)

    @Inject
    open protected lateinit var api: ApiService

    open fun getLayout() = R.layout.activity_root

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view!!)
        initAdapter()
        adapter.startLoading()
    }

    open fun initAdapter() {}
}

