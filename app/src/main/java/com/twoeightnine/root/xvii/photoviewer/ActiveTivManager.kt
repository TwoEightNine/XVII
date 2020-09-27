package com.twoeightnine.root.xvii.photoviewer

/**
 * Created by twoeightnine on 1/25/18.
 */
class ActiveTivManager(val count: Int = 3) {

    private val actualTivs by lazy {
        val res = arrayListOf<TouchImageView?>()
        repeat(count) { res.add(null) }
        res
    }

    fun saveTiv(pos: Int, tiv: TouchImageView) {
        actualTivs[pos % count] = tiv
    }

    fun getTiv(pos: Int) = actualTivs[pos % count]

}