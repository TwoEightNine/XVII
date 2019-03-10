package com.twoeightnine.root.xvii.profile.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.BaseAdapter
import com.twoeightnine.root.xvii.model.UserField

class ProfileAdapter(context: Context) : BaseAdapter<UserField, ProfileAdapter.UserFieldViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserFieldViewHolder {
        return UserFieldViewHolder(View.inflate(context, R.layout.item_user_field, null))
    }

    override fun onBindViewHolder(holder: UserFieldViewHolder, position: Int) {
        val field = items[position]
        holder.tvTitle.text = field.title
        holder.tvValue.text = field.value
        holder.rlItem.setOnClickListener(field.onClick)
        holder.rlItem.setOnLongClickListener(field.onLongClick)
    }

    inner class UserFieldViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        @BindView(R.id.rlItem)
        lateinit var rlItem: RelativeLayout
        @BindView(R.id.tvTitle)
        lateinit var tvTitle: TextView
        @BindView(R.id.tvValue)
        lateinit var tvValue: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }

}