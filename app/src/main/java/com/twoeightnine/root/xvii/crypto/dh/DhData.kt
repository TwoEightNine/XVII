package com.twoeightnine.root.xvii.crypto.dh

import java.math.BigInteger

data class DhData(
        val generator: BigInteger,
        val modulo: BigInteger,
        val public: BigInteger
)