package com.twoeightnine.root.xvii.friends.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.utils.loadUrl
import de.hdodenhof.circleimageview.CircleImageView

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
        Picasso
                .with(App.context)
                .loadUrl(user.photo100)
                .into(holder.civPhoto)
        holder.tvName.text = user.firstName
        holder.ivOnline.visibility = if (user.online == 1) View.VISIBLE else View.GONE
        return view
    }

    inner class FriendViewHolder(view: View) {

        @BindView(R.id.llItem)
        lateinit var llItem: LinearLayout
        @BindView(R.id.civPhoto)
        lateinit var civPhoto: CircleImageView
        @BindView(R.id.ivOnlineDot)
        lateinit var ivOnline: ImageView
        @BindView(R.id.tvName)
        lateinit var tvName: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }
}