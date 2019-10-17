package com.twoeightnine.root.xvii.poll

import android.os.Bundle
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
import com.twoeightnine.root.xvii.utils.stylize
import kotlinx.android.synthetic.main.fragment_poll.*
import javax.inject.Inject

class PollFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: PollViewModel.Factory
    private lateinit var viewModel: PollViewModel

    private val poll by lazy { arguments?.getParcelable<Poll>(ARG_POLL) }
    private lateinit var adapter: PollAnswersAdapter

    override fun getLayoutId() = R.layout.fragment_poll

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[PollViewModel::class.java]
        viewModel.voted.observe(viewLifecycleOwner, Observer(::onVotedChanged))
        poll?.also(::bindPoll)
    }

    private fun bindPoll(poll: Poll) {
        setTitle(getString(if (poll.anonymous) {
            R.string.poll_anon
        } else {
            R.string.poll_public
        }))
        tvQuestion.text = poll.question
        tvVotes.text = context?.resources?.getQuantityString(R.plurals.votes, poll.votes, poll.votes)
        tvDate.text = getTime(poll.created)
        initRecycler(poll)

        btnVote.isEnabled = poll.canVote && !poll.closed
        btnVote.setOnClickListener { onVoteClick() }
//        btnVote.stylize()
    }

    private fun initRecycler(poll: Poll) {
        val context = context ?: return

        adapter = PollAnswersAdapter(context, poll.multiple)
        adapter.update(poll.answers)
        adapter.invalidateSelected(poll.answerIds)
        rvVotes.layoutManager = LinearLayoutManager(context)
        rvVotes.adapter = adapter
    }

    private fun onVoteClick() {
        val answers = adapter.multiSelect
        if (answers.isNotEmpty()) {
            viewModel.vote(poll ?: return, answers)
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

        const val ARG_POLL = "poll"

        fun newInstance(args: Bundle?) = PollFragment().apply {
            arguments = args ?: Bundle()
        }

        fun getArgs(poll: Poll) = Bundle().apply { putParcelable(ARG_POLL, poll) }
    }

}