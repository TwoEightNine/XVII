package com.twoeightnine.root.xvii.settings.adapters

import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimpleAdapter
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.Account
import com.twoeightnine.root.xvii.utils.load
import kotlinx.android.synthetic.main.item_account.view.*

class AccountsAdapter : SimpleAdapter<Account>() {

    override fun getView(position: Int, v: View?, parent: ViewGroup): View {
        val account = items[position]
        var view = v
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_account, null)
            view.tag = AccountsViewHolder(view)
        }
        val holder = view?.tag as AccountsViewHolder
        holder.bind(account)
        return view
    }

    inner class AccountsViewHolder(private val view: View) {

        fun bind(account: Account) {
            with(view) {
                tvAccount.text = account.name
                tvId.text = "@id${account.uid}"
                if (Session.token == account.token) {
                    tvAccount.setTypeface(null, Typeface.BOLD)
                    tvAccount.paintFlags = tvAccount.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                } else {
                    tvAccount.setTypeface(null, Typeface.NORMAL)
                    tvAccount.paintFlags = tvAccount.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
                }
                civPhoto.load(account.photo)
            }
        }
    }
}