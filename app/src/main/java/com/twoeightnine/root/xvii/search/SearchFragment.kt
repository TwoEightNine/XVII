package com.twoeightnine.root.xvii.search

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerActivity
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.hideKeyboard
import com.twoeightnine.root.xvii.utils.notifications.NotificationUtils
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.utils.subscribeSearch
import global.msnthrp.xvii.data.dialogs.Dialog
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import global.msnthrp.xvii.uikit.extensions.applyTopInsetMargin
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.view_search.*
import javax.inject.Inject

class SearchFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: SearchViewModel.Factory
    private lateinit var viewModel: SearchViewModel

    private val adapter by lazy {
        SearchAdapter(requireContext(), ::onClick, ::onLongClick)
    }

    override fun getLayoutId() = R.layout.fragment_search

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[SearchViewModel::class.java]

        etSearch.subscribeSearch(true, viewModel::search)
        ivDelete.setOnClickListener { etSearch.setText("") }
        ivEmptyView.paint(Munch.color.color50)

        ivDelete.paint(Munch.color.colorDark(50))
        ivBack.paint(Munch.color.colorDark(50))
        ivBack.setOnClickListener { onBackPressed() }
        rlSearch.background.paint(Munch.color.color20)

        rvSearch.applyBottomInsetPadding()
        rlSearch.applyTopInsetMargin()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getResult().observe(viewLifecycleOwner, ::updateResults)
    }

    private fun updateResults(data: Wrapper<ArrayList<Dialog>>) {
        if (data.data != null) {
            adapter.update(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun onClick(dialog: Dialog) {
        ChatOwnerActivity.launch(context, dialog.peerId)
    }

    private fun onLongClick(dialog: Dialog) {
        if (BuildConfig.DEBUG) {
            NotificationUtils.showTestMessageNotification(requireContext(), dialog)
        }
    }

    private fun initRecycler() {
        rvSearch.layoutManager = LinearLayoutManager(context)
        rvSearch.adapter = adapter
        rvSearch.setOnTouchListener { _, _ ->
            activity?.let { hideKeyboard(it) }
            false
        }
        adapter.emptyView = llEmptyView
    }

    companion object {

        fun newInstance() = SearchFragment()
    }
}