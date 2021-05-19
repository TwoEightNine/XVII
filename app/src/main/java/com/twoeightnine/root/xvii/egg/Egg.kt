/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.egg

import com.twoeightnine.root.xvii.R
import java.io.Serializable

sealed class Egg : Serializable {

    sealed class ImageEgg(val imageUrl: String) : Egg() {

        object Letov : ImageEgg("https://s00.yaplakal.com/pics/pics_original/4/7/2/6998274.jpg")

        object Orwell : ImageEgg("https://i.mycdn.me/i?r=AyH4iRPQ2q0otWIFepML2LxRMdpJEMt4on5ckZd4Axx7rQ")

        object OHara : ImageEgg("https://best-quote.ru/wp-content/uploads/2020/12/8896cdeb915224765111ab51b6ef1f9a40c6d828-e1606945077305.jpeg")

        object Earth : ImageEgg("https://64.media.tumblr.com/223308dd59bf9ea67fa90d46797f5ffc/tumblr_my48lc5cb61r4yvo9o1_500.jpg")

        object Hugo : ImageEgg("https://i.pinimg.com/originals/63/4e/dd/634edd1dd9579e0fbdb36bbbee2b6f9d.png")
    }

    sealed class ImageAndTextEgg(val textRes: Int, val imageUrl: String) : Egg() {
        object Poverty : ImageAndTextEgg(R.string.egg_poverty, "https://i.pinimg.com/originals/31/aa/ec/31aaec3dcbd3c4d3358444c29ae3543f.jpg")
    }

    companion object {

        private var nextIndex = 0

        fun getRandomEgg(): Egg {
            val nextEgg = allEggs[nextIndex]
            nextIndex = (nextIndex + 1) % allEggs.size
            return nextEgg
        }

        private val allEggs = listOf<Egg>(
                ImageEgg.Orwell,
                ImageEgg.OHara,
                ImageEgg.Earth,
                ImageEgg.Hugo,
                ImageAndTextEgg.Poverty
        ).shuffled()
    }
}

