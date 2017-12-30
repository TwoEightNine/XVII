package com.twoeightnine.root.xvii.settings.adapters

import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimpleAdapter
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.Account
import com.twoeightnine.root.xvii.utils.loadPhoto
import de.hdodenhof.circleimageview.CircleImageView

class AccountsAdapter : SimpleAdapter<Account>() {

    override fun getView(position: Int, v: View?, parent: ViewGroup): View {
        val account = items[position]
        var view = v
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_account, null)
            view.tag = AccountsViewHolder(view)
        }
        val holder = view?.tag as AccountsViewHolder
        holder.tvAccount.text = account.name
        holder.tvId.text = "@id${account.uid}"
        if (Session.token == account.token) {
            holder.tvAccount.setTypeface(null, Typeface.BOLD)
            holder.tvAccount.paintFlags = holder.tvAccount.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        } else {
            holder.tvAccount.setTypeface(null, Typeface.NORMAL)
            holder.tvAccount.paintFlags = holder.tvAccount.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        }
        holder.civPhoto.loadPhoto(account.photo)
        return view
    }

    inner class AccountsViewHolder(var view: View) {
        @BindView(R.id.civPhoto)
        lateinit var civPhoto: CircleImageView
        @BindView(R.id.tvAccount)
        lateinit var tvAccount: TextView
        @BindView(R.id.tvId)
        lateinit var tvId: TextView

        init {
            ButterKnife.bind(this, view)
        }

    }
}