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
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.stylize

fun createContextPopup(context: Context, items: List<ContextPopupItem>): AlertDialog =

        AlertDialog.Builder(context).create().apply {
            val itemHeight = context.resources.getDimensionPixelSize(R.dimen.context_popup_item_height)
            val adapter = ContextPopupAdapter(context, this).apply {
                addAll(items.toMutableList())
            }
            RecyclerView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, itemHeight * items.size)
                layoutManager = LinearLayoutManager(context)
                this.adapter = adapter
                setView(this)
            }
            stylize()
        }
