package com.twoeightnine.root.xvii.friends.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseReachAdapter
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.item_user.view.*

class FriendsAdapter(context: Context,
                     private val onClick: (User) -> Unit,
                     onLoaded: (Int) -> Unit
) : BaseReachAdapter<User, FriendsAdapter.FriendViewHolder>(context, onLoaded) {

    var firstItemPadding = 0

    override fun createHolder(parent: ViewGroup, viewType: Int) =
            FriendViewHolder(inflater.inflate(R.layout.item_user, null))


    override fun bind(holder: FriendViewHolder, item: User) {
        holder.bind(item, items[0] == item)
    }

    override fun createStubLoadItem() = User()

    inner class FriendViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(user: User, isFirst: Boolean) {
            with(view) {
                val topPadding = if (isFirst) firstItemPadding else 0
                setPadding(0, topPadding, 0, 0)

                civPhoto.load(user.photo100)
                val d = ContextCompat.getDrawable(context, R.drawable.dotshape)
                d?.stylize(ColorManager.MAIN_TAG)
                ivOnlineDot.setImageDrawable(if (user.isOnline) d else null)
                tvName.text = user.fullName
                if (Prefs.lowerTexts) {
                    tvName.lower()
                }

                user.lastSeen?.also {
                    tvInfo.text = getLastSeenText(resources, user.isOnline, it.time, it.platform)
                }
                setOnClickListener {
                    items.getOrNull(adapterPosition)?.also(onClick)
                }
            }
        }
    }

}