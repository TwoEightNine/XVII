package com.twoeightnine.root.xvii.dagger

import io.reactivex.Flowable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicService {

    @GET("index.php?act=letsgo")
    fun startSearch(@Query("query") query: String): Flowable<ResponseBody>

    @GET("index.php?mod=query_res")
    fun finishSearch(@Query("id") id: Int,
                     @Query("r") r: Int): Flowable<ResponseBody>

    @GET("index.php?mod=content")
    fun selectTrack(@Query("one") one: Int,
                    @Query("two") two: Int,
                    @Query("three") three: String,
                    @Query("idcode") idCode: Int,
                    @Query("s") s: Int): Flowable<ResponseBody>


}