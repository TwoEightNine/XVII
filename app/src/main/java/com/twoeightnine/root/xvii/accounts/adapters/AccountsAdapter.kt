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