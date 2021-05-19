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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Poll
import com.twoeightnine.root.xvii.model.attachments.PollAnswer
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.subscribeSmart
import javax.inject.Inject

class PollViewModel(private val api: ApiService) : ViewModel() {

    private val votedLiveData = WrappedMutableLiveData<Boolean>()

    private val pollLiveData = WrappedMutableLiveData<Poll>()

    val voted: WrappedLiveData<Boolean>
        get() = votedLiveData

    val poll: WrappedLiveData<Poll>
        get() = pollLiveData

    fun loadPoll(pollId: Int, ownerId: Int) {
        api.getPoll(ownerId, pollId)
                .subscribeSmart({ poll ->
                    pollLiveData.value = Wrapper(poll)
                }, { error ->
                    pollLiveData.value = Wrapper(error = error)
                })
    }

    fun clearVotes() {
        val poll = pollLiveData.value?.data ?: return

        api.clearVote(poll.ownerId, poll.id)
                .subscribeSmart({ response ->
                    if (response == 1) {
                        loadPoll(poll.id, poll.ownerId)
                    } else {
                        pollLiveData.value = pollLiveData.value
                    }
                }, {})
    }

    fun vote(answers: List<PollAnswer>) {
        val poll = pollLiveData.value?.data ?: return

        api.addVote(poll.ownerId, poll.id, answers.map { it.id }.joinToString(separator = ","))
                .subscribeSmart({ response ->
                    val success = response == 1
                    votedLiveData.value = Wrapper(success)
                    if (success) {
                        loadPoll(poll.id, poll.ownerId)
                    }
                }, { error ->
                    votedLiveData.value = Wrapper(error = error)
                })
    }

    class Factory @Inject constructor(private val api: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = PollViewModel(api) as T
    }

}