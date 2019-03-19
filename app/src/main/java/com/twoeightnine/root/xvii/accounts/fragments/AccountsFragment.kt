package com.twoeightnine.root.xvii.accounts.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.accounts.adapters.AccountsAdapter
import com.twoeightnine.root.xvii.accounts.models.Account
import com.twoeightnine.root.xvii.accounts.viewmodel.AccountsViewModel
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.utils.restartApp
import com.twoeightnine.root.xvii.utils.showDeleteDialog
import com.twoeightnine.root.xvii.utils.showError
import kotlinx.android.synthetic.main.fragment_accounts.*
import javax.inject.Inject

class AccountsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: AccountsViewModel.Factory
    private lateinit var viewModel: AccountsViewModel

    private val adapter by lazy {
        AccountsAdapter(contextOrThrow, ::onClick, ::onLongClick)
    }

    private var selectedAccount: Account? = null

    override fun getLayoutId() = R.layout.fragment_accounts

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[AccountsViewModel::class.java]
        viewModel.getAccounts().observe(this, Observer { updateAccounts(it) })
        viewModel.loadAccounts()

        rlAddAccount.setOnClickListener {
            Session.token = ""
            restartApp(getString(R.string.restart_app))
        }
        Style.forAll(rlAddAccount)
    }

    private fun updateAccounts(accounts: ArrayList<Account>) {
        adapter.update(accounts)
    }

    private fun initRecyclerView() {
        rvAccounts.layoutManager = LinearLayoutManager(context)
        rvAccounts.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.accounts))
    }

    private fun onClick(account: Account) {
        if (account.token == Session.token) {
            showError(activity, R.string.already_acc)
        } else {
            selectedAccount = account
            viewModel.switchTo(account)
            restartApp(getString(R.string.restart_app))
        }
    }

    private fun onLongClick(account: Account) {
        if (account.token == Session.token) {
            showError(activity, R.string.cannot_delete_acc)
        } else {
            showDeleteDialog(context) { viewModel.deleteAccount(account) }
        }
    }

    companion object {
        fun newInstance() = AccountsFragment()
    }
}