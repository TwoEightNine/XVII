/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.utils.contextpopup

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.item_context_popup.view.*

class ContextPopupAdapter(
        context: Context,
        private val dialog: AlertDialog
) : BaseAdapter<ContextPopupItem, ContextPopupAdapter.ContextPopupItemHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ContextPopupItemHolder(inflater.inflate(R.layout.item_context_popup, parent, false))

    override fun onBindViewHolder(holder: ContextPopupItemHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ContextPopupItemHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: ContextPopupItem) {
            with(itemView) {
                tvTitle.text = context.getString(item.textRes)

                val hasIcon = item.iconRes != 0
                ivIcon.setVisible(hasIcon)
                if (hasIcon) {
                    ivIcon.setImageResource(item.iconRes)
                    ivIcon.paint(Munch.color.color)
                }
                rlBack.setOnClickListener {
                    dialog.dismiss()
                    item.onClick()
                }
            }
        }
    }
}