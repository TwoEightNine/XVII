package com.twoeightnine.root.xvii.features.notifications.color

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import kotlinx.android.synthetic.main.item_color.view.*

class ColorAdapter(
        context: Context,
        private val onClick: (Color) -> Unit
) : BaseAdapter<Color, ColorAdapter.ColorViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = ColorViewHolder(inflater.inflate(R.layout.item_color, parent, false))

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(color: Color) {
            with(itemView) {
                tvColor.text = resources.getString(color.titleRes)
                ivColor.setBackgroundColor(color.color)
                setOnClickListener { onClick(items[adapterPosition]) }
            }
        }
    }
}