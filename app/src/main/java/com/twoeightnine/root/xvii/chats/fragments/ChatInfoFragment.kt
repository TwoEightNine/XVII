package com.twoeightnine.root.xvii.chats.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.friends.adapters.UsersAdapter
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.presenter.ChatInfoFragmentPresenter
import com.twoeightnine.root.xvii.mvp.view.ChatInfoFragmentView
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.profile.fragments.ProfileFragment
import com.twoeightnine.root.xvii.utils.load
import com.twoeightnine.root.xvii.utils.showCommon
import kotlinx.android.synthetic.main.fragment_chat_info.*
import javax.inject.Inject

class ChatInfoFragment: BaseOldFragment(), ChatInfoFragmentView {

    companion object {
        fun newInstance(message: Message): ChatInfoFragment {
            val frag = ChatInfoFragment()
            frag.message = message
            return frag
        }
    }

    lateinit private var adapter: UsersAdapter
    lateinit var message: Message

    @Inject
    lateinit var api: ApiService
    lateinit var presenter: ChatInfoFragmentPresenter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.chat))
    }

    override fun bindViews(view: View) {
        super.bindViews(view)
        civPhoto.load(message.photo)
        etTitle.setText(message.title)
        initAdapter()
        App.appComponent?.inject(this)
        presenter = ChatInfoFragmentPresenter(api)
        presenter.view = this
        presenter.message = message
        presenter.loadUsers()
        rlRename.setOnClickListener { presenter.renameChat(etTitle.text.toString()) }
        rlLeave.setOnClickListener { presenter.leaveChat() }

        Style.forViewGroup(rlLeave, false)
        Style.forViewGroup(rlRename, false)
        Style.forEditText(etTitle, Style.MAIN_TAG)
    }

    fun initAdapter() {
        adapter = UsersAdapter(safeActivity, {}, {
            val user = adapter.items[it]
            rootActivity.loadFragment(ProfileFragment.newInstance(user.id))
        })
        rvUsers.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        rvUsers.adapter = adapter
    }

    override fun getLayout() = R.layout.fragment_chat_info

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    override fun showLoading() {
        adapter.startLoading()
    }

    override fun hideLoading() {
        //
    }

    override fun showError(error: String) {
        adapter.setErrorLoading()
    }

    override fun onUsersLoaded(users: MutableList<User>) {
        adapter.stopLoading(users)
    }

    override fun onChatRenamed(title: String) {
        showCommon(activity, getString(R.string.chat_title_updated, title))
    }

    override fun onUserLeft() {
        showCommon(activity, R.string.left_chat)
    }
}