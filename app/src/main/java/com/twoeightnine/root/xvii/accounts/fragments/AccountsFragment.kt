package com.twoeightnine.root.xvii.accounts.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.accounts.adapters.AccountsAdapter
import com.twoeightnine.root.xvii.accounts.viewmodel.AccountsViewModel
import com.twoeightnine.root.xvii.background.longpoll.services.NotificationService
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerActivity
import com.twoeightnine.root.xvii.login.LoginActivity
import com.twoeightnine.root.xvii.utils.restartApp
import com.twoeightnine.root.xvii.utils.showDeleteDialog
import com.twoeightnine.root.xvii.utils.showWarnConfirm
import global.msnthrp.xvii.core.accounts.model.Account
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import global.msnthrp.xvii.uikit.extensions.fadeIn
import global.msnthrp.xvii.uikit.extensions.show
import kotlinx.android.synthetic.main.fragment_accounts.*
import javax.inject.Inject

class AccountsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: AccountsViewModel.Factory
    private lateinit var viewModel: AccountsViewModel

    private val adapter by lazy {
        AccountsAdapter(requireContext(), ::onDeleteClick, ::onViewClick, ::onActivateClick)
    }

    override fun getLayoutId() = R.layout.fragment_accounts

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initRecyclerView()
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[AccountsViewModel::class.java]

        btnAddAccount.setOnClickListener {
            LoginActivity.launchForNewAccount(context)
        }
        btnLogOutAll.setOnClickListener { onLogOutAll() }
        nsvContent.applyBottomInsetPadding()
    }

    private fun updateAccounts(accounts: List<Account>) {
        adapter.update(accounts)
//        adapter.update(FakeData.accounts)
        llContent.show()
        llContent.fadeIn(200L)
    }

    private fun initRecyclerView() {
        rvAccounts.layoutManager = LinearLayoutManager(context)
        rvAccounts.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getAccounts().observe(::updateAccounts)
        viewModel.getAccountSwitched().observe {
            restartApp(context, getString(R.string.restart_app))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAccounts()
    }

    private fun onDeleteClick(account: Account) {
        if (account.isActive) return

        showDeleteDialog(context, getString(R.string.this_account)) { viewModel.deleteAccount(account) }
    }

    private fun onViewClick(account: Account) {
        if (account.isActive) return

        ChatOwnerActivity.launch(context, account.userId)
    }

    private fun onActivateClick(account: Account) {
        if (account.isActive) return

        viewModel.switchTo(account)
    }

    private fun onLogOutAll() {
        showWarnConfirm(context, getString(R.string.accounts_log_out_all_accounts), getString(R.string.logout)) { logout ->
            if (logout) {
                NotificationService.stop(context)
                viewModel.logOutAll()
                restartApp(context, getString(R.string.restart_app))
            }
        }
    }

    companion object {
        fun newInstance() = AccountsFragment()
    }
}