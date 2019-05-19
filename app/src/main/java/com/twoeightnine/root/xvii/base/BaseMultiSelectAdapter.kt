package com.twoeightnine.root.xvii.base

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.adapters.BaseAdapter

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