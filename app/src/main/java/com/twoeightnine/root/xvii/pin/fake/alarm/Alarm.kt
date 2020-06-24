package com.twoeightnine.root.xvii.pin.fake.alarm

data class Alarm(

        /**
         * in minutes from 0:00 (123 == 02:03)
         */
        val time: Int,

        val onlyOnce: Boolean,

        var enabled: Boolean = true
)