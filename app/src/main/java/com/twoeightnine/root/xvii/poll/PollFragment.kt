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

package com.twoeightnine.root.xvii.poll

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Poll
import com.twoeightnine.root.xvii.utils.getTime
import com.twoeightnine.root.xvii.utils.showAlert
import global.msnthrp.xvii.uikit.extensions.*
import kotlinx.android.synthetic.main.fragment_poll.*
import javax.inject.Inject

class PollFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: PollViewModel.Factory
    private lateinit var viewModel: PollViewModel

    private val pollId by lazy {
        arguments?.getInt(ARG_POLL_ID) ?: 0
    }
    private val ownerId by lazy {
        arguments?.getInt(ARG_OWNER_ID) ?: 0
    }

    private lateinit var adapter: PollAnswersAdapter

    override fun getLayoutId() = R.layout.fragment_poll

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[PollViewModel::class.java]
        viewModel.voted.observe(viewLifecycleOwner, Observer(::onVotedChanged))
        viewModel.poll.observe(viewLifecycleOwner, Observer(::onPollLoaded))
        viewModel.loadPoll(pollId, ownerId)

        btnVote.applyBottomInsetMargin()
        rvVotes.applyBottomInsetPadding()
    }

    private fun onPollLoaded(data: Wrapper<Poll>) {
        if (data.data != null) {
            bindPoll(data.data)
            rlLoader.hide()
        } else {
            showAlert(context, data.error) {
                activity?.finish()
            }
        }
    }

    private fun bindPoll(poll: Poll) {
        xviiToolbar.title = getString(if (poll.anonymous) {
            R.string.poll_anon
        } else {
            R.string.poll_public
        })
        tvQuestion.text = poll.question
        tvVotes.text = context?.resources?.getQuantityString(R.plurals.votes, poll.votes, poll.votes)
        tvDate.text = getTime(poll.created)
        initRecycler(poll)

        btnVote.setVisibleWithInvis(false)
        btnVote.setOnClickListener { onVoteClick() }

//        when {
//            poll.photo != null -> {
//                ivBack.load(poll.photo.getOptimalPhotoUrl())
//                rlBack.background = ColorDrawable(poll.photo.getColor())
//            }
//            poll.background != null -> {
//                rlBack.background = GradientDrawable(
//                        GradientDrawable.Orientation.TL_BR,
//                        poll.background.getColors().toList().toIntArray()
//                )
//            }
//        }
    }

    override fun getMenu(): Int = R.menu.poll

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_clear_vote -> {
            viewModel.clearVotes()
            rlLoader.show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun initRecycler(poll: Poll) {
        val context = context ?: return

        adapter = PollAnswersAdapter(context, poll.multiple, poll.answerIds.isNotEmpty())
        adapter.update(poll.answers)
        adapter.invalidateSelected(poll.answerIds)
        adapter.multiListener = { isSomethingSelected ->
            btnVote.setVisibleWithInvis(isSomethingSelected && poll.canVote && !poll.closed)
        }
        rvVotes.layoutManager = LinearLayoutManager(context)
        rvVotes.adapter = adapter
    }

    private fun onVoteClick() {
        val answers = adapter.multiSelect
        if (answers.isNotEmpty()) {
            rlLoader.show()
            viewModel.vote(answers)
        }
    }

    private fun onVotedChanged(data: Wrapper<Boolean>) {
        val alertText = when {
            data.data == true -> getString(R.string.vote_added)
            else -> data.error ?: getString(R.string.unable_to_vote)
        }
        showAlert(context, alertText)
    }

    companion object {

        private const val BUTTON_MARGIN = 15

        const val ARG_POLL = "poll"
        const val ARG_POLL_ID = "pollId"
        const val ARG_OWNER_ID = "ownerId"

        fun newInstance(args: Bundle?) = PollFragment().apply {
            arguments = args ?: Bundle()
        }

        fun getArgs(poll: Poll) = Bundle().apply {
            putInt(ARG_POLL_ID, poll.id)
            putInt(ARG_OWNER_ID, poll.ownerId)
        }
    }

}