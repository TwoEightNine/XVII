package com.twoeightnine.root.xvii.crypto.dh

import com.twoeightnine.root.xvii.crypto.fromBase64
import com.twoeightnine.root.xvii.crypto.toBase64
import java.math.BigInteger

data class DhData(
        val generator: BigInteger,
        val modulo: BigInteger,
        val public: BigInteger
) {
    companion object {

        fun serialize(d: DhData) = "${toBase64(d.modulo.toByteArray())}," +
                "${toBase64(d.generator.toByteArray())}," +
                toBase64(d.public.toByteArray())

        fun deserialize(s: String): DhData {
            val numArr = s.split(",")
                    .map { fromBase64(it)  }
                    .map { BigInteger(it) }
                    .toTypedArray()
            return DhData(
                    modulo = numArr[0],
                    generator = numArr[1],
                    public = numArr[2]
            )
        }
    }
}