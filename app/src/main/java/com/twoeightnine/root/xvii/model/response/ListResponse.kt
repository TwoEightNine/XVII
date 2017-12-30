package com.twoeightnine.root.xvii.response

/**
 * Created by root on 8/31/16.
 */

class ListResponse<T> {
    val items: MutableList<T> = mutableListOf()
    val count: Long = 0
}
