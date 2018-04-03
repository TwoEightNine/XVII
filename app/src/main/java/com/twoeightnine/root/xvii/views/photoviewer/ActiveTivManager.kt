package com.twoeightnine.root.xvii.views.photoviewer

/**
 * Created by twoeightnine on 1/25/18.
 */
class ActiveTivManager(val count: Int = 3) {

    private val actualTivs by lazy {
        val res = arrayListOf<TouchImageView?>()
        IntRange(1, count).forEach { res.add(null) }
        res
    }

    fun saveTiv(pos: Int, tiv: TouchImageView) {
        actualTivs[pos % count] = tiv
    }

    fun getTiv(pos: Int) = actualTivs[pos % count]

}