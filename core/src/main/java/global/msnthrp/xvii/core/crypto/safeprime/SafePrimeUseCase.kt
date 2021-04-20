package global.msnthrp.xvii.core.crypto.safeprime

import global.msnthrp.xvii.core.crypto.safeprime.entity.SafePrime

interface SafePrimeUseCase {

        fun loadSafePrime(): SafePrime
}