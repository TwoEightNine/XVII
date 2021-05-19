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

package com.twoeightnine.root.xvii.analyzer.dialog

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.Wrapper
import kotlinx.android.synthetic.main.fragment_analyse_dialog_all.*
import javax.inject.Inject

class AnalyzeDialogFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: AnalyseDialogViewModel.Factory
    private lateinit var viewModel: AnalyseDialogViewModel

    private val peerId by lazy { arguments?.getInt(ARG_PEER_ID) ?: 0 }

    override fun getLayoutId() = R.layout.fragment_analyse_dialog_all

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[AnalyseDialogViewModel::class.java]
        viewModel.peerId = peerId
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getUser().observe(viewLifecycleOwner, ::updateUser)
        viewModel.getProgress().observe(viewLifecycleOwner, ::updateProgress)
        viewModel.analyse()
    }

    private fun updateUser(data: Wrapper<User>) {
        val text = tvData.text.toString()
        tvData.text = text + "\n" + data.data
    }

    private fun updateProgress(data: Wrapper<Pair<Int, Int>>) {
        val text = tvData.text.toString()
        if (data.data != null) {
            tvData.text = text + "\n" + data.data.first + "/" + data.data.second
        }
    }

    companion object {

        const val ARG_PEER_ID = "peerId"

        fun newInstance(peerId: Int): AnalyzeDialogFragment {
            val fragment = AnalyzeDialogFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}