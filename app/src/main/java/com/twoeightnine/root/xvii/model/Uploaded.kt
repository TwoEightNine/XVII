package com.twoeightnine.root.xvii.model

/**
 * Created by root on 10/14/16.
 */

class Uploaded {
    val server: Int = 0
    val photo: String? = null
    val hash: String? = null
    val file: String? = null

    override fun toString() = "{$server ;  $hash ;\n$photo}"
}
