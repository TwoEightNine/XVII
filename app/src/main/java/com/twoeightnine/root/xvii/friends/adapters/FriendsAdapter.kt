package com.twoeightnine.root.xvii.friends.adapters

import android.view.View
import android.view.ViewGroup
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.utils.loadRounded
import kotlinx.android.synthetic.main.item_friend.view.*

class FriendsAdapter(loader: ((Int) -> Unit)?,
                     listener: ((User) -> Unit)?): SimplePaginationAdapter<User>(loader, listener) {

    override fun getView(position: Int, v: View?, p2: ViewGroup?): View {
        var view = v
        val user = items[position]
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_friend, null)
            view.tag = FriendViewHolder(view)
        }
        val holder = view?.tag as FriendViewHolder
        holder.bind(user)
        return view
    }

    inner class FriendViewHolder(private val view: View) {

        fun bind(user: User) {
            with(view) {
                civPhoto.loadRounded(user.photo100)
                tvName.text = user.firstName
                ivOnlineDot.visibility = if (user.online == 1) View.VISIBLE else View.GONE
            }
        }
    }
}