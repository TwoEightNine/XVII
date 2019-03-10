package com.twoeightnine.root.xvii.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater

/**
 * Created by root on 8/30/16.
 */

abstract class BaseAdapter<T, VH : androidx.recyclerview.widget.RecyclerView.ViewHolder>(protected var context: Context) : androidx.recyclerview.widget.RecyclerView.Adapter<VH>() {

    open val items: MutableList<T> = mutableListOf()

    protected var inflater = LayoutInflater.from(context)

    var multiListener: OnMultiSelected? = null

    fun add(item: T, pos: Int) {
        items.add(pos, item)
        notifyItemInserted(pos)
    }

    open fun add(item: T) {
        add(item, items.size)
    }

    @JvmOverloads fun addAll(items: MutableList<T>, pos: Int = this.items.size) {
        val size = items.size
        this.items.addAll(pos, items)
        notifyItemRangeInserted(pos, size)
    }

    fun removeAt(pos: Int): T {
        val removed = items.removeAt(pos)
        notifyItemRemoved(pos)
        return removed
    }

    fun remove(obj: T) {
        for (i in items.indices) {
            if (obj == items[i]) {
                removeAt(i)
                break
            }
        }
    }

    fun update(pos: Int, item: T): T {
        val oldItem = items[pos]
        items[pos] = item
        notifyItemChanged(pos)
        return oldItem
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    val isEmpty: Boolean
        get() = items.isEmpty()


    override fun getItemCount(): Int {
        return items.size
    }

    //multiselect

    var multiSelectRaw: MutableList<Int> = mutableListOf()
        protected set

    val multiSelect: String
        get() = multiSelectRaw
                .map { it.toString() }
                .joinToString(separator = ",")

    fun multiSelect(id: Int) {
        if (multiSelectRaw.contains(id)) {
            removeFromMultiSelect(id)
        } else {
            addToMultiSelect(id)
        }
        if (multiListener != null) {
            notifyMultiSelect()
        }
    }

    open fun notifyMultiSelect() {
        if (multiSelectRaw.size == 0) {
            multiListener!!.onEmpty()
        } else if (multiSelectRaw.size == 1) {
            multiListener!!.onNonEmpty()
        }
    }

    fun clearMultiSelect() {
        multiSelectRaw.clear()
        if (multiListener != null) {
            multiListener!!.onEmpty()
        }
        notifyDataSetChanged()
    }

    private fun addToMultiSelect(id: Int) {
        multiSelectRaw.add(id)
    }

    private fun removeFromMultiSelect(id: Int) {
        multiSelectRaw.remove(id)
    }

    interface OnMultiSelected {
        fun onNonEmpty()
        fun onEmpty()
    }

}
