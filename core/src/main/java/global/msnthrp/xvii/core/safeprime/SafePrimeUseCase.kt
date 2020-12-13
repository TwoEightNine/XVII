package global.msnthrp.xvii.core.safeprime

import global.msnthrp.xvii.core.safeprime.entity.SafePrime

interface SafePrimeUseCase {

        fun loadSafePrime(): SafePrime
}