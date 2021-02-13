package com.twoeightnine.root.xvii.accounts.adapters

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.extensions.getInitials
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import global.msnthrp.xvii.data.accounts.Account
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import global.msnthrp.xvii.uikit.extensions.lowerIf
import kotlinx.android.synthetic.main.item_account.view.*

class AccountsAdapter(
        context: Context,
        private val onClick: (Account) -> Unit,
        private val onLongClick: (Account) -> Unit
) : BaseAdapter<Account, AccountsAdapter.AccountsViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AccountsViewHolder(inflater.inflate(R.layout.item_account, null))

    override fun onBindViewHolder(holder: AccountsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class AccountsViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(account: Account) {
            with(view) {
                tvAccount.text = account.name
                tvAccount.lowerIf(Prefs.lowerTexts)
                tvId.text = "@id${account.uid}"
                if (Session.token == account.token) {
                    tvAccount.paintFlags = tvAccount.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                } else {
                    tvAccount.paintFlags = tvAccount.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
                }
                civPhoto.load(account.photo, account.name?.getInitials(), id = account.uid)
                setOnClickListener { onClick(items[adapterPosition]) }
                setOnLongClickListener {
                    onLongClick(items[adapterPosition])
                    true
                }
            }
        }
    }
}