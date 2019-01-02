package com.twoeightnine.root.xvii.settings.fragments

import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.RelativeLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Account
import com.twoeightnine.root.xvii.model.LongPollServer
import com.twoeightnine.root.xvii.settings.adapters.AccountsAdapter
import com.twoeightnine.root.xvii.utils.*
import io.realm.Realm
import io.realm.RealmQuery
import javax.inject.Inject

class AccountsFragment: BaseFragment() {

    @BindView(R.id.rlAddAccount)
    lateinit var rlAddAccount: RelativeLayout
    @BindView(R.id.lvAccounts)
    lateinit var lvAccounts: ListView

    @Inject
    lateinit var apiUtils: ApiUtils

    private lateinit var adapter: AccountsAdapter

    private var selectedAccount: Account? = null
    private var selectedPosition = -1

    override fun bindViews(view: View) {
        ButterKnife.bind(this, view)
        initAdapter()
        App.appComponent?.inject(this)
        rlAddAccount.setOnClickListener {
            Session.token = ""
            restartApp(getString(R.string.restart_app))
        }
        Style.forAll(rlAddAccount)
    }

    private fun initAdapter() {
        adapter = AccountsAdapter()
        lvAccounts.adapter = adapter
        val realm = Realm.getDefaultInstance()
        val accounts = RealmQuery
                .createQuery(realm, Account::class.java)
                .findAll()
                .toMutableList()
        adapter.add(accounts)
        lvAccounts.setOnItemClickListener {
             _, _, pos, _ ->
            val account = adapter.items[pos]
            if (account.token == Session.token) {
                showError(activity, R.string.already_acc)
            } else {
                selectedAccount = account
                selectedPosition = pos
                apiUtils.checkAccount(account.token ?: "", account.uid, ::onSuccess, ::onFail, { onSuccess() })
            }
        }
        lvAccounts.setOnItemLongClickListener {
            _, _, i, _ ->
            val account = adapter.items[i]
            if (account.token == Session.token) {
                showError(activity, R.string.cannot_delete_acc)
            } else {
                showDeleteDialog(safeActivity, { deleteAccount(i) })
            }
            true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.accounts))
    }

    private fun onSuccess() {
        if (selectedAccount != null) {
            Session.token = selectedAccount!!.token
            Session.uid = selectedAccount!!.uid
            Session.fullName = selectedAccount!!.name
            Session.photo = selectedAccount!!.photo
            Session.longPoll = LongPollServer("", "", 0) //resetting
            CacheHelper.deleteAllMessagesAsync()
            restartApp(getString(R.string.restart_app))
        }
    }

    private fun onFail(error: String) {
        showError(activity, error)
        deleteAccount(selectedPosition)
    }

    private fun deleteAccount(position: Int) {
        val acc = adapter.items[position]
        if (position != -1) {
            adapter.remove(position)
        }
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.where(Account::class.java)
                .equalTo(Account.UID, acc.uid)
                .findFirst()
                .deleteFromRealm()
        realm.commitTransaction()
    }

    override fun getLayout() = R.layout.fragment_accounts
}