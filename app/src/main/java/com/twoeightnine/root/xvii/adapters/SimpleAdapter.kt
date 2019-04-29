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

    fun addUnique(item: T): Boolean {
        if (isInList(item)) {
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

    fun update(items: MutableList<T>) {
        clear()
        add(items)
    }

    fun clear() = items.clear()
}