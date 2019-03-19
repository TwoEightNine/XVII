package com.twoeightnine.root.xvii.friends.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.BaseAdapter
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.utils.getTime
import com.twoeightnine.root.xvii.utils.load
import kotlinx.android.synthetic.main.item_user.view.*

class FriendsAdapter(context: Context,
                     private val onClick: (User) -> Unit
) : BaseAdapter<User, FriendsAdapter.FriendViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FriendViewHolder(inflater.inflate(R.layout.item_user, null))

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class FriendViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(user: User) {
            with(view) {
                civPhoto.load(user.photo100)
                val d = ContextCompat.getDrawable(context, R.drawable.dotshape)
                Style.forDrawable(d, Style.MAIN_TAG)
                ivOnlineDot.setImageDrawable(if (user.isOnline) d else null)
                tvName.text = user.fullName

                val stringRes = if (user.isOnline) R.string.online_seen else R.string.last_seen
                tvInfo.text = resources.getString(stringRes,
                        getTime(user.lastSeen?.time ?: 0, full = true))
                setOnClickListener { onClick(items[adapterPosition]) }
            }
        }
    }

}