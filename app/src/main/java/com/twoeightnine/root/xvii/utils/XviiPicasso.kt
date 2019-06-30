package com.twoeightnine.root.xvii.utils

import android.annotation.SuppressLint
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.network.datausage.DataUsageEvent
import com.twoeightnine.root.xvii.network.datausage.DataUsageInterceptor
import okhttp3.OkHttpClient

object XviiPicasso {

    @SuppressLint("StaticFieldLeak")
    private var instance: Picasso? = null

    fun get(): Picasso = instance ?: synchronized(this) {
        instance ?: create().also {
            instance = it
        }
    }

    private fun create() = Picasso.Builder(App.context)
            .downloader(OkHttp3Downloader(
                    OkHttpClient.Builder()
                            .addInterceptor(DataUsageInterceptor(DataUsageEvent.Type.PHOTO))
                            .build()
            ))
            .build()

}