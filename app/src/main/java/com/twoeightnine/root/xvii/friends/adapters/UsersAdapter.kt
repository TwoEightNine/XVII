package com.twoeightnine.root.xvii.friends.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.PaginationAdapter
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.utils.loadUrl

class UsersAdapter(context: Context,
                   loader: (Int) -> Unit,
                   var listener: (Int) -> Unit) : PaginationAdapter<User>(context, loader) {

    override fun createHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserViewHolder(inflater.inflate(R.layout.item_user, null))
    }

    override var stubLoadItem: User? = User.stubLoad

    override fun isStubLoad(obj: User) = User.isStubLoad(obj)

    override fun isStubTry(obj: User) = User.isStubTry(obj)

    override var stubTryItem: User? = User.stubTry

    override fun onBindViewHolder(vholder: RecyclerView.ViewHolder, position: Int) {
        val user = items[position]
        val holder: UserViewHolder
        if (vholder is UserViewHolder) {
            holder = vholder
        } else {
            return
        }
        if (multiSelectRaw.contains(user.id)) {
            holder.civPhoto.setImageResource(R.mipmap.ic_check_raster)
        } else {
            holder.civPhoto.loadUrl(user.photo100)
        }
        val d = ContextCompat.getDrawable(context, R.drawable.dotshape)
        Style.forDrawable(d, Style.MAIN_TAG)
        if (user.online == 1) {
            holder.ivOnlineDot.setImageDrawable(d)
        } else {
            holder.ivOnlineDot.setImageDrawable(null)
        }
        holder.tvName.text = user.fullName()
        if (!TextUtils.isEmpty(user.hometown)) {
            holder.tvInfo.text = user.hometown
        } else if (!TextUtils.isEmpty(user.bdate)) {
            holder.tvInfo.text = user.bdate
        } else if (!TextUtils.isEmpty(user.status)) {
            holder.tvInfo.text = user.status
        } else {
            holder.tvInfo.text = "@${user.getDomain()}"
        }

    }

    override fun notifyMultiSelect() {
        if (multiSelectRaw.size == 0) {
            multiListener!!.onEmpty()
        } else if (multiSelectRaw.size >= 1) {
            multiListener!!.onNonEmpty()
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.civPhoto)
        lateinit var civPhoto: ImageView
        @BindView(R.id.ivOnlineDot)
        lateinit var ivOnlineDot: ImageView
        @BindView(R.id.tvName)
        lateinit var tvName: TextView
        @BindView(R.id.tvInfo)
        lateinit var tvInfo: TextView
        @BindView(R.id.rlItemContainer)
        lateinit var rlItemContainer: RelativeLayout


        init {
            ButterKnife.bind(this, itemView)
            rlItemContainer.setOnClickListener { listener.invoke(adapterPosition) }
        }
    }
}