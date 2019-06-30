package com.twoeightnine.root.xvii.network.datausage

data class DataUsageEvent(

        /**
         * name of request
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
         * time when the request was finished
         */
        val timeStamp: Int,

        val type: Type
) {
    /**
     * type of request
     */
    enum class Type {

        /**
         * usual call to vk api
         */
        API,

        /**
         * loading photo using [XviiPicasso]
         */
        PHOTO
    }
}