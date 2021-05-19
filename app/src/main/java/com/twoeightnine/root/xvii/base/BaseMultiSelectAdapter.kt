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

package com.twoeightnine.root.xvii.base

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter

abstract class BaseMultiSelectAdapter<T, VH : RecyclerView.ViewHolder>(context: Context)
    : BaseAdapter<T, VH>(context) {

    /**
     * true when becomes non-empty
     * false when become empty
     */
    var multiListener: ((Boolean) -> Unit)? = null

    /**
     * emits number of selected items
     */
    var multiSelectListener: ((Int) -> Unit)? = null

    /**
     * for internal usage
     */
    var multiSelectMode = false
        set(value) {
            field = value
            if (!value) {
                clearMultiSelect()
            }
        }

    val multiSelect = arrayListOf<T>()

    fun multiSelect(item: T) {
        if (multiSelect.contains(item)) {
            multiSelect.remove(item)
        } else {
            multiSelect.add(item)
        }
        notifyMultiSelect()
    }

    protected open fun notifyMultiSelect() {
        when (multiSelect.size) {
            0 -> multiListener?.invoke(false)
            1 -> multiListener?.invoke(true)
        }
        multiSelectListener?.invoke(multiSelect.size)
    }

    fun clearMultiSelect() {
        multiSelect.clear()
        notifyMultiSelect()
        notifyDataSetChanged()
    }
}