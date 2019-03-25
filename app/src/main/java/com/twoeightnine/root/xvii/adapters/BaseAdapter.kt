package com.twoeightnine.root.xvii.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.utils.setVisible

/**
 * Created by root on 8/30/16.
 */

abstract class BaseAdapter<T, VH : RecyclerView.ViewHolder>(protected var context: Context)
    : RecyclerView.Adapter<VH>() {

    open val items: MutableList<T> = mutableListOf()

    protected var inflater = LayoutInflater.from(context)

    /**
     * true when becomes non-empty
     * false when become empty
     */
    var multiListener: ((Boolean) -> Unit)? = null

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
    fun addAll(items: MutableList<T>, pos: Int = this.items.size) {
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

    open fun update(items: List<T>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
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

    //multiselect

    /**
     * for internal usage
     */
    var multiSelectMode = false

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
    }

    fun clearMultiSelect() {
        multiSelect.clear()
        multiListener?.invoke(false)
        notifyDataSetChanged()
    }

    private fun invalidateEmptiness() {
        emptyView?.setVisible(items.isEmpty())
    }

}
