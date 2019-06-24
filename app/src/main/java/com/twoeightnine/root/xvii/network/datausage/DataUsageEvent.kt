package com.twoeightnine.root.xvii.network.datausage

data class DataUsageEvent(

        /**
         * ui-friendly identifier
         */
        val name: String,

        /**
         * size of request content in bytes
         */
        val requestSize: Long,

        /**
         * size of response content in bytes
         */
        val responseSize: Long,

        /**
         * time when the request was sent
         */
        val timeStamp: Int
)