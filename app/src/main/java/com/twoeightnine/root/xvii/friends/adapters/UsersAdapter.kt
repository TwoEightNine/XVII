package com.twoeightnine.root.xvii.friends.adapters

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.PaginationAdapter
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.utils.load
import kotlinx.android.synthetic.main.item_user.view.*

class UsersAdapter(context: Context,
                   loader: (Int) -> Unit,
                   var listener: (Int) -> Unit) : PaginationAdapter<User>(context, loader) {

    override fun createHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return UserViewHolder(inflater.inflate(R.layout.item_user, null))
    }

    override var stubLoadItem: User? = User.stubLoad

    override fun isStubLoad(obj: User) = User.isStubLoad(obj)

    override fun isStubTry(obj: User) = User.isStubTry(obj)

    override var stubTryItem: User? = User.stubTry

    override fun onBindViewHolder(vholder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        (vholder as? UserViewHolder)?.bind(items[position])
    }

    inner class UserViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        fun bind(user: User) {
            with(itemView) {
                if (multiSelect.contains(user)) {
                    civPhoto.setImageResource(R.mipmap.ic_check_raster)
                } else {
                    civPhoto.load(user.photo100)
                }
                val d = ContextCompat.getDrawable(context, R.drawable.dotshape)
                Style.forDrawable(d, Style.MAIN_TAG)
                if (user.online == 1) {
                    ivOnlineDot.setImageDrawable(d)
                } else {
                    ivOnlineDot.setImageDrawable(null)
                }
                tvName.text = user.fullName
                if (!TextUtils.isEmpty(user.hometown)) {
                    tvInfo.text = user.hometown
                } else if (!TextUtils.isEmpty(user.bdate)) {
                    tvInfo.text = user.bdate
                } else if (!TextUtils.isEmpty(user.status)) {
                    tvInfo.text = user.status
                } else {
                    tvInfo.text = "@${user.getPageName()}"
                }
                rlItemContainer.setOnClickListener { listener.invoke(adapterPosition) }
            }
        }
    }
}