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

package global.msnthrp.xvii.data.report

import global.msnthrp.xvii.core.report.ReportDataSource
import global.msnthrp.xvii.core.report.model.ReportReason
import global.msnthrp.xvii.data.network.Retrofit
import global.msnthrp.xvii.data.network.model.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

class NetworkReportDataSource : ReportDataSource {

    private val api by lazy {
        Retrofit.createVkApiService(ReportsApiService::class.java)
    }

    override fun reportUser(userId: Int, reason: ReportReason, comment: String) {
        api.reportUser(userId, reason.asUserReason(), comment).execute().body()
    }

    override fun reportWallPost(ownerId: Int, postId: Int, reason: ReportReason) {
        api.reportWallPost(ownerId, postId, reason.asContentReason()).execute().body()
    }

    override fun reportPhoto(ownerId: Int, photoId: Int, reason: ReportReason) {
        api.reportPhoto(ownerId, photoId, reason.asContentReason()).execute().body()
    }

    private fun ReportReason.asUserReason(): String {
        return when (this) {
            ReportReason.PORN -> "porn"
            ReportReason.SPAM -> "spam"
            ReportReason.ABUSE -> "insult"
            ReportReason.ADS -> "advertisement"
            else -> ""
        }
    }

    private fun ReportReason.asContentReason(): Int {
        return when (this) {
            ReportReason.SPAM -> 0
            ReportReason.CP -> 1
            ReportReason.VIOLENCE -> 3
            ReportReason.DRUGS -> 4
            ReportReason.ADULT -> 5
            ReportReason.ABUSE -> 6

            ReportReason.PORN -> 5
            ReportReason.ADS -> 0
        }
    }


    interface ReportsApiService {

        @GET("users.report")
        fun reportUser(
                @Query("user_id") userId: Int,
                @Query("type") type: String,
                @Query("comment") comment: String
        ): Call<BaseResponse<Int>>

        @GET("wall.reportPost")
        fun reportWallPost(
                @Query("owner_id") ownerId: Int,
                @Query("post_id") postId: Int,
                @Query("reason") reason: Int
        ): Call<BaseResponse<Int>>

        @GET("photos.report")
        fun reportPhoto(
                @Query("owner_id") ownerId: Int,
                @Query("photo_id") photoId: Int,
                @Query("reason") reason: Int
        ): Call<BaseResponse<Int>>
    }
}