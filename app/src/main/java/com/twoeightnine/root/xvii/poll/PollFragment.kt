package com.twoeightnine.root.xvii.poll

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.model.attachments.Poll
import com.twoeightnine.root.xvii.utils.getTime
import kotlinx.android.synthetic.main.fragment_poll.*

class PollFragment : BaseFragment() {

    private val poll by lazy { arguments?.getParcelable<Poll>(ARG_POLL) }
    private lateinit var adapter: PollAnswersAdapter

    override fun getLayoutId() = R.layout.fragment_poll

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
        btnVote.isEnabled = poll.canVote && !poll.closed

        initRecycler(poll)
    }

    private fun initRecycler(poll: Poll) {
        val context = context ?: return

        adapter = PollAnswersAdapter(context, poll.multiple)
        adapter.update(poll.answers)
        adapter.invalidateSelected(poll.answerIds)
        rvVotes.layoutManager = LinearLayoutManager(context)
        rvVotes.adapter = adapter
    }

    companion object {

        const val ARG_POLL = "poll"

        fun newInstance(args: Bundle?) = PollFragment().apply {
            arguments = args ?: Bundle()
        }

        fun getArgs(poll: Poll) = Bundle().apply { putParcelable(ARG_POLL, poll) }
    }

}