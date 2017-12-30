package com.twoeightnine.root.xvii.adapters

import android.widget.BaseAdapter

abstract class SimpleAdapter<T> : BaseAdapter() {

    var items: MutableList<T> = mutableListOf()

    override fun getCount() = items.size

    override fun getItemId(p0: Int) = 0L

    override fun getItem(pos: Int) = items[pos]

    fun add(item: T) {
        items.add(item)
        notifyDataSetChanged()
    }

    fun add(items: MutableList<T>) {
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun isInList(item: T) = false

    fun addUnique(item: T) : Boolean {
        if(isInList(item)){
            return false
        }
        add(item)
        return true
    }

    fun remove(item: T) {
        items.remove(item)
        notifyDataSetChanged()
    }

    fun remove(pos: Int) {
        items.removeAt(pos)
        notifyDataSetChanged()
    }

    fun add(pos: Int, item: T) {
        items.add(pos, item)
        notifyDataSetChanged()
    }

    fun clear() = items.clear()

    //multiselect

    var multiListener: OnMultiSelected? = null

    var multiSelectRaw: MutableList<T> = mutableListOf()
        protected set

    val multiSelect: String
        get() = multiSelectRaw
                .map { it.toString() }
                .joinToString(separator = ",")

    fun multiSelect(item: T) {
        if (multiSelectRaw.contains(item)) {
            removeFromMultiSelect(item)
        } else {
            addToMultiSelect(item)
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

    private fun addToMultiSelect(id: T) {
        multiSelectRaw.add(id)
    }

    private fun removeFromMultiSelect(id: T) {
        multiSelectRaw.remove(id)
    }

    interface OnMultiSelected {
        fun onNonEmpty()
        fun onEmpty()
    }
}