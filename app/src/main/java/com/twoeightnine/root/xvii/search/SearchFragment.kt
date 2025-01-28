/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.search

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerFactory
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.main.MainActivity
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
    var lastAdded: Int = 0

    private val selectedFriends by lazy {
        arguments?.getBoolean(MainActivity.SELECTED_FRIENDS)?: false
    }
    private val searchString by lazy {
        arguments?.getString(MainActivity.SEARCH_TEXT)
    }

    private val adapter by lazy {
        SearchAdapter(requireContext(), ::onClick, ::onLongClick)
    }

    override fun getLayoutId() = R.layout.fragment_search

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[SearchViewModel::class.java]

        viewModel.setFrom(selectedFriends)
        etSearch.subscribeSearch(true, viewModel::search)
        searchString?.let {
            etSearch.setText(it.toString())
        }

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

    private fun updateResults(data: Wrapper<ArrayList<SearchDialog>>) {
        if (data.data != null) {
            adapter.update(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun onClick(sDialog: SearchDialog) {
        var dialog = Dialog(
                peerId = sDialog.peerId,
                messageId = sDialog.messageId,
                title = sDialog.title,
                text = sDialog.text,
                photo = sDialog.photo,
                isOnline = sDialog.isOnline,
                isOut = sDialog.isOut
            )
        if (sDialog.isChat){
            ChatActivity.launch(context, dialog, true)
        }else {
            ChatOwnerFactory.launch(context, dialog.peerId)
        }
    }

    private fun onLongClick(sDialog: SearchDialog) {
        if (BuildConfig.DEBUG) {
            var dialog = Dialog(
                peerId = sDialog.peerId,
                messageId = sDialog.messageId,
                title = sDialog.title,
                text = sDialog.text,
                photo = sDialog.photo,
                isOnline = sDialog.isOnline,
                isOut = sDialog.isOut
            )
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
        rvSearch.addOnScrollListener(SearchScrollListener())
        adapter.emptyView = llEmptyView
    }

    companion object {

        fun newInstance() = SearchFragment()
    }

    private inner class SearchScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (rvSearch!=null && lastAdded < adapter.itemCount - 1 &&
                adapter.lastVisiblePosition(rvSearch.layoutManager) == adapter.itemCount - 1) {
                viewModel.search(etSearch.text.toString(), adapter.itemCount)
                lastAdded = adapter.itemCount - 1
            }
        }
    }

}