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

    val voted: WrappedLiveData<Boolean>
        get() = votedLiveData

    fun vote(poll: Poll, answers: List<PollAnswer>) {
        api.addVote(poll.ownerId, poll.id, answers.map { it.id }.joinToString(separator = ","))
                .subscribeSmart({ response ->
                    votedLiveData.value = Wrapper(response == 1)
                }, { error ->
                    votedLiveData.value = Wrapper(error = error)
                })
    }

    class Factory @Inject constructor(private val api: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = PollViewModel(api) as T
    }

}