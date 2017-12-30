package com.twoeightnine.root.xvii.adapters

import android.widget.AbsListView

abstract class SimplePaginationAdapter<T>(private val loader: ((Int) -> Unit)?,
                                          var listener: ((T) -> Unit)?) : SimpleAdapter<T>() {
    var noMoreItems: Boolean = false
    var isLoading: Boolean = false

    fun setAdapter(listView: AbsListView) {
        listView.adapter = this
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(absListView: AbsListView, i: Int) {

            }

            override fun onScroll(absListView: AbsListView, firstVisible: Int, visibleCount: Int, totalCount: Int) {
                val lastItem = firstVisible + visibleCount
                if (lastItem > 0 && totalCount > 0) {
                    if (count - lastItem < THRES && !noMoreItems && !isLoading) {
                        if (loader != null) {
                            loader.invoke(count)
                            isLoading = true
                        }
                    }
                }
            }
        })
    }

    fun stopLoading(items: MutableList<T>) {
        add(items)
        isLoading = false
        if (items.size == 0) {
            noMoreItems = true
        }
    }

    fun startLoading() {
        loader?.invoke(0)
        isLoading = true
    }

    companion object {

        val THRES = 9
    }
}
