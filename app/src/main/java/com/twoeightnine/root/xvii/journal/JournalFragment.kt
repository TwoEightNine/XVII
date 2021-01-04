package com.twoeightnine.root.xvii.journal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.utils.AppBarLifter
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import kotlinx.android.synthetic.main.fragment_journal.*

class JournalFragment : BaseFragment() {

    private val viewModel by viewModels<JournalViewModel>()

    private val adapter by lazy {
        JournalAdapter(requireContext())
    }

    override fun getLayoutId(): Int = R.layout.fragment_journal

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.events.observe(adapter::update)
        viewModel.loadEvents()
    }

    private fun initRecyclerView() {
        rvEvents.layoutManager = LinearLayoutManager(requireContext())
        rvEvents.adapter = adapter
        rvEvents.addOnScrollListener(AppBarLifter(xviiToolbar))
        rvEvents.applyBottomInsetPadding()
    }
}