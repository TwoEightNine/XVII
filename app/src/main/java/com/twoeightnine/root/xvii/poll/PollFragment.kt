package com.twoeightnine.root.xvii.poll

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Poll
import com.twoeightnine.root.xvii.utils.*
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

        progressBar.stylize()
        rvVotes.consumeInsets { _, bottom ->
            val extraPadding = context?.resources?.getDimensionPixelSize(R.dimen.toolbar_height)
                    ?: 0
            rvVotes.setBottomPadding(bottom + extraPadding)
            btnVote.setBottomMargin(bottom + BUTTON_MARGIN)
        }
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
        setTitle(getString(if (poll.anonymous) {
            R.string.poll_anon
        } else {
            R.string.poll_public
        }))
        tvQuestion.text = poll.question
        tvVotes.text = context?.resources?.getQuantityString(R.plurals.votes, poll.votes, poll.votes)
        tvDate.text = getTime(poll.created)
        initRecycler(poll)

        btnVote.setVisibleWithInvis(false)
        btnVote.setOnClickListener { onVoteClick() }
        btnVote.stylize()

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.poll, menu)
    }

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