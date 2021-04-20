package com.twoeightnine.root.xvii.features.datausage

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.network.datausage.DataUsageInterceptor
import com.twoeightnine.root.xvii.utils.getSize
import kotlinx.android.synthetic.main.fragment_data_usage.*

class DataUsageFragment : BaseFragment() {

    private val adapter by lazy {
        DataUsageAdapter(requireContext())
    }

    override fun getLayoutId() = R.layout.fragment_data_usage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val events = DataUsageInterceptor.events
        rvUsage.layoutManager = LinearLayoutManager(context)
        rvUsage.adapter = adapter
        adapter.addAll(events)

        context?.resources?.also {
            tvOutgoing.text = getSize(it, events.map { it.requestSize }.sum().toInt())
            tvIncoming.text = getSize(it, events.map { it.responseSize }.sum().toInt())
        }
    }

    companion object {
        fun newInstance() = DataUsageFragment()
    }
}