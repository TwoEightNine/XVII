package com.twoeightnine.root.xvii.accounts.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.accounts.adapters.AccountsAdapter
import com.twoeightnine.root.xvii.accounts.models.Account
import com.twoeightnine.root.xvii.accounts.viewmodel.AccountsViewModel
import com.twoeightnine.root.xvii.background.longpoll.services.NotificationService
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.utils.*
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
        setHasOptionsMenu(true)
        initRecyclerView()
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[AccountsViewModel::class.java]
        viewModel.getAccounts().observe(this, Observer { updateAccounts(it) })
        viewModel.loadAccounts()

        fabAdd.setOnClickListener {
            viewModel.updateRunningAccount()
            Session.token = ""
            restartApp(context, getString(R.string.restart_app))
        }
        fabAdd.stylize()
        fabAdd.setBottomInsetMargin(context?.resources?.getDimensionPixelSize(R.dimen.accounts_fab_add_margin) ?: 0)
    }

    private fun updateAccounts(accounts: ArrayList<Account>) {
        adapter.update(accounts)
//        adapter.update(FakeData.accounts)
    }

    private fun initRecyclerView() {
        rvAccounts.layoutManager = LinearLayoutManager(context)
        rvAccounts.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.accounts))
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.menu_accounts, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.menu_log_out -> {
            showConfirm(context, getString(R.string.wanna_logout)) { logout ->
                if (logout) {
                    NotificationService.stop(context)
                    viewModel.logOut()
                    restartApp(context, getString(R.string.restart_app))
                }
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun onClick(account: Account) {
        if (account.isRunning) {
            showError(activity, R.string.already_acc)
        } else {
            selectedAccount = account
            viewModel.switchTo(account)
            restartApp(context, getString(R.string.restart_app))
        }
    }

    private fun onLongClick(account: Account) {
        if (account.isRunning) {
            showError(activity, R.string.cannot_delete_acc)
        } else {
            showDeleteDialog(context) { viewModel.deleteAccount(account) }
        }
    }

    companion object {
        fun newInstance() = AccountsFragment()
    }
}