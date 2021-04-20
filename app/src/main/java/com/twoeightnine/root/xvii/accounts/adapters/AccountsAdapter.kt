package com.twoeightnine.root.xvii.accounts.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.extensions.getInitials
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.uikit.paint
import global.msnthrp.xvii.core.accounts.model.Account
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import global.msnthrp.xvii.uikit.extensions.lowerIf
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.item_account.view.*

class AccountsAdapter(
        context: Context,
        private val onDeleteClick: (Account) -> Unit,
        private val onViewClick: (Account) -> Unit,
        private val onActivateClick: (Account) -> Unit
) : BaseAdapter<Account, AccountsAdapter.AccountsViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AccountsViewHolder(inflater.inflate(R.layout.item_account, parent, false))

    override fun onBindViewHolder(holder: AccountsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class AccountsViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(account: Account) {
            with(view) {
                val accountType = getAccountTypeResId(adapterPosition)
                        .takeIf { it != 0 }
                        ?.let(context::getString)
                xlAccountType.setVisible(accountType != null)
                xlAccountType.text = accountType

                tvAccount.text = account.name
                tvAccount.lowerIf(Prefs.lowerTexts)

                tvId.text = "@id${account.userId}"
                civPhoto.load(account.photo, account.name.getInitials(), id = account.userId)

                groupButtons.setVisible(!account.isActive)
                listOf(tvView, tvActivate)
                        .flatMap { it.compoundDrawablesRelative.toList() }
                        .filterNotNull()
                        .forEach { it.paint(ContextCompat.getColor(context, R.color.main_text)) }

                ivDelete.setOnClickListener {
                    items.getOrNull(adapterPosition)?.let(onDeleteClick)
                }
                tvView.setOnClickListener {
                    items.getOrNull(adapterPosition)?.let(onViewClick)
                }
                tvActivate.setOnClickListener {
                    items.getOrNull(adapterPosition)?.let(onActivateClick)
                }
            }
        }

        private fun getAccountTypeResId(position: Int) = when (position) {
            0 -> R.string.accounts_active
            1 -> R.string.accounts_other
            else -> 0
        }
    }
}