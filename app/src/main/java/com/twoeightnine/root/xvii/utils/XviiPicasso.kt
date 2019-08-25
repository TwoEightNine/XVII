package com.twoeightnine.root.xvii.utils

import android.annotation.SuppressLint
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.App

object XviiPicasso {

    @SuppressLint("StaticFieldLeak")
    private var instance: Picasso? = null

    fun get(): Picasso = instance ?: synchronized(this) {
        instance ?: create().also {
            instance = it
        }
    }

    private fun create() = Picasso.Builder(App.context)
            // not convenient and applicable
//            .downloader(OkHttp3Downloader(
//                    OkHttpClient.Builder()
//                            .addInterceptor(DataUsageInterceptor(DataUsageEvent.Type.PHOTO))
//                            .build()
//            ))
            .build()

}