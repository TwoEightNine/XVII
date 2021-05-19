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

package global.msnthrp.xvii.uikit.base.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import global.msnthrp.xvii.uikit.extensions.setVisible

/**
 * Created by root on 8/30/16.
 */

abstract class BaseAdapter<T, VH : RecyclerView.ViewHolder>(protected var context: Context)
    : RecyclerView.Adapter<VH>() {

    open val items: MutableList<T> = mutableListOf()

    protected var inflater: LayoutInflater = LayoutInflater.from(context)

    var emptyView: View? = null

    fun add(item: T, pos: Int) {
        items.add(pos, item)
        notifyItemInserted(pos)
        invalidateEmptiness()
    }

    open fun add(item: T) {
        add(item, items.size)
    }

    @JvmOverloads
    open fun addAll(items: MutableList<T>, pos: Int = this.items.size) {
        val size = items.size
        this.items.addAll(pos, items)
        notifyItemRangeInserted(pos, size)
        invalidateEmptiness()
    }

    fun removeAt(pos: Int): T {
        val removed = items.removeAt(pos)
        notifyItemRemoved(pos)
        invalidateEmptiness()
        return removed
    }

    fun remove(obj: T): Int {
        for (i in items.indices) {
            if (obj == items[i]) {
                removeAt(i)
                return i
            }
        }
        return -1
    }

    fun update(pos: Int, item: T): T {
        val oldItem = items[pos]
        items[pos] = item
        notifyItemChanged(pos)
        return oldItem
    }

    open fun update(items: List<T>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
        invalidateEmptiness()
    }

    fun update(items: List<T>, pos: Int) {
        var i = 0
        while (pos + i < itemCount && i < items.size) {
            this.items[pos + i] = items[i]
            i++
        }
        notifyItemRangeChanged(pos, i)
        invalidateEmptiness()
    }

    open fun clear() {
        items.clear()
        notifyDataSetChanged()
        invalidateEmptiness()
    }

    val isEmpty: Boolean
        get() = items.isEmpty()


    override fun getItemCount(): Int {
        return items.size
    }

    fun lastVisiblePosition(layoutManager: RecyclerView.LayoutManager?) = when (layoutManager) {
        is LinearLayoutManager -> layoutManager.findLastVisibleItemPosition()
        is GridLayoutManager -> layoutManager.findLastVisibleItemPosition()
        else -> -1
    }


    fun firstVisiblePosition(layoutManager: RecyclerView.LayoutManager?) = when (layoutManager) {
        is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
        is GridLayoutManager -> layoutManager.findFirstVisibleItemPosition()
        else -> -1
    }

    fun isAtTop(layoutManager: LinearLayoutManager?) = firstVisiblePosition(layoutManager) == 0

    fun isAtBottom(layoutManager: LinearLayoutManager?) = lastVisiblePosition(layoutManager) == items.size - 1

    private fun invalidateEmptiness() {
        emptyView?.setVisible(items.isEmpty())
    }

}
