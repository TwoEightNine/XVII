package com.twoeightnine.root.xvii.network.response

/**
 * Created by root on 8/31/16.
 */

data class ListResponse<T>(
        val items: MutableList<T> = mutableListOf(),
        val count: Long = 0
)
