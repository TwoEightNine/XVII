package com.twoeightnine.root.xvii.network

import com.twoeightnine.root.xvii.background.longpoll.models.LongPollServer
import com.twoeightnine.root.xvii.background.longpoll.models.LongPollUpdate
import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.UploadServer
import com.twoeightnine.root.xvii.model.Uploaded
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.network.response.*
import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.http.*

interface ApiService {

    companion object {
        const val NO_TOKEN_HEADER_KEY = "No-Token"
        const val NO_TOKEN_HEADER = "$NO_TOKEN_HEADER_KEY: 1"

        const val NEW_VERSION_HEADER_KEY = "New-Version"
        const val NEW_VERSION_HEADER = "$NEW_VERSION_HEADER_KEY: 1"
    }

    @Headers(NEW_VERSION_HEADER)
    @GET("messages.getConversations?filter=all&extended=1")
    fun getConversations(
            @Query("count") count: Int,
            @Query("offset") offset: Int = 0
    ): Flowable<BaseResponse<ConversationsResponse>>

    @Headers(NEW_VERSION_HEADER)
    @GET("messages.deleteConversation")
    fun deleteConversation(
            @Query("peer_id") peerId: Int,
            @Query("count") count: Int
    ): Flowable<BaseResponse<Any>>

    @GET("messages.getHistory?extended=1")
    @Headers(NEW_VERSION_HEADER)
    fun getMessages(
            @Query("peer_id") peerId: Int,
            @Query("count") count: Int,
            @Query("offset") offset: Int = 0,
            @Query("fields") fields: String = User.FIELDS
    ): Flowable<BaseResponse<MessagesHistoryResponse>>

    @GET("messages.getHistory")
    @Headers(NEW_VERSION_HEADER)
    fun getMessagesLite(
            @Query("peer_id") peerId: Int,
            @Query("count") count: Int,
            @Query("offset") offset: Int = 0
    ): Flowable<BaseResponse<MessagesHistoryResponse>>

    /**
     * since 5.80 it's not documented and doesn't support [messageIds]
     */
    @GET("messages.markAsRead")
    fun markAsRead(
            @Query("message_ids") messageIds: String
    ): Flowable<BaseResponse<Int>>

    @Headers(NEW_VERSION_HEADER)
    @GET("messages.delete")
    fun deleteMessages(
            @Query("message_ids") messageIds: String,
            @Query("delete_for_all") deleteForAll: Int
    ): Flowable<BaseResponse<JSONObject>>

    @Headers(NEW_VERSION_HEADER)
    @FormUrlEncoded
    @POST("messages.edit?keep_snippets=1&keep_forward_messages=1")
    fun editMessage(
            @Field("peer_id") peerId: Int,
            @Field("message") message: String,
            @Field("message_id") messageId: Int
    ): Flowable<BaseResponse<Int>>

    @Headers(NEW_VERSION_HEADER)
    @FormUrlEncoded
    @POST("messages.send")
    fun sendMessage(
            @Field("peer_id") peerId: Int,
            @Field("random_id") randomId: Int,
            @Field("message") text: String? = null,
            @Field("forward_messages") forwardedMessages: String? = null,
            @Field("attachment") attachments: String? = null,
            @Field("reply_to") replyTo: Int? = null,
            @Field("sticker_id") stickerId: Int? = null
    ): Flowable<BaseResponse<Int>>

    @Headers(NEW_VERSION_HEADER)
    @GET("messages.getById?extended=1")
    fun getMessageById(
            @Query("message_ids") messageIds: String
    ): Flowable<BaseResponse<MessagesHistoryResponse>>

    @Headers(NEW_VERSION_HEADER)
    @GET("messages.markAsImportant")
    fun markMessagesAsImportant(
            @Query("message_ids") messageIds: String,
            @Query("important") important: Int
    ): Flowable<BaseResponse<MutableList<Int>>>

    @Headers(NEW_VERSION_HEADER)
    @GET("messages.getHistoryAttachments")
    fun getHistoryAttachments(
            @Query("peer_id") peerId: Int,
            @Query("media_type") mediaType: String,
            @Query("count") count: Int,
            @Query("start_from") startFrom: String?
    ): Flowable<BaseResponse<AttachmentsResponse>>

    @GET("messages.searchConversations?extended=1")
    fun searchConversations(
            @Query("q") q: String,
            @Query("count") count: Int,
            @Query("fields") fields: String = User.FIELDS
    ): Flowable<BaseResponse<SearchConversationsResponse>>

    @Headers(NEW_VERSION_HEADER)
    @GET("messages.getImportantMessages?extended=1")
    fun getStarredMessages(
            @Query("count") count: Int,
            @Query("offset") offset: Int = 0,
            @Query("fields") fields: String = User.FIELDS
    ): Flowable<BaseResponse<MessagesResponse>>

    @GET("store.getStickersKeywords")
    fun getStickers(): Flowable<BaseResponse<StickersResponse>>

    @GET("messages.setActivity")
    fun setActivity(
            @Query("peer_id") peerId: Int,
            @Query("type") type: String
    ): Flowable<BaseResponse<Int>>

    //users
    @Headers(NEW_VERSION_HEADER)
    @GET("users.get")
    fun getUsers(@Query("user_ids") userIds: String,
                 @Query("fields") fields: String = User.FIELDS): Flowable<BaseResponse<MutableList<User>>>

    @GET("users.search")
    fun searchUsers(@Query("q") q: String,
                    @Query("fields") fields: String,
                    @Query("count") count: Int,
                    @Query("offset") offset: Int): Flowable<BaseResponse<ListResponse<User>>>

    //friends
    @GET("friends.get?order=hints")
    fun getFriends(
            @Query("count") count: Int,
            @Query("offset") offset: Int = 0,
            @Query("fields") fields: String = User.FIELDS
    ): Flowable<BaseResponse<ListResponse<User>>>

    @GET("friends.search")
    fun searchFriends(@Query("q") q: String,
                      @Query("fields") fields: String,
                      @Query("count") count: Int,
                      @Query("offset") offset: Int): Flowable<BaseResponse<ListResponse<User>>>

    //photos
    @GET("photos.copy")
    fun copyPhoto(@Query("owner_id") ownerId: Int,
                  @Query("photo_id") photoId: Int,
                  @Query("access_key") accessKey: String): Flowable<BaseResponse<Int>>

    @Headers(NEW_VERSION_HEADER)
    @GET("photos.get?rev=1&photo_sizes=1")
    fun getPhotos(@Query("owner_id") ownerId: Int,
                  @Query("album_id") albumId: String,
                  @Query("count") count: Int,
                  @Query("offset") offset: Int = 0): Flowable<BaseResponse<ListResponse<Photo>>>

    @GET("photos.getMessagesUploadServer")
    fun getPhotoUploadServer(): Flowable<BaseResponse<UploadServer>>

    @Multipart
    @POST
    @Headers(NO_TOKEN_HEADER)
    fun uploadPhoto(@Url url: String,
                    @Part file: MultipartBody.Part): Flowable<Uploaded>

    @Headers(NEW_VERSION_HEADER)
    @GET("photos.saveMessagesPhoto")
    fun saveMessagePhoto(@Query("photo") photo: String,
                         @Query("hash") hash: String,
                         @Query("server") server: Int): Flowable<BaseResponse<MutableList<Photo>>>

    //groups
    @GET("groups.getById")
    fun getGroups(@Query("group_ids") groupIds: String): Flowable<BaseResponse<MutableList<Group>>>

    @GET("groups.join")
    fun joinGroup(@Query("group_id") groupId: Int): Flowable<BaseResponse<Int>>

    @GET("groups.isMember")
    fun isGroupMember(@Query("group_id") groupId: Int,
                      @Query("user_id") userId: Int): Flowable<BaseResponse<Int>>

    //account
    @GET("account.setOffline")
    fun setOffline(): Flowable<BaseResponse<Int>>

    //video
    @GET("video.get")
    fun getVideos(@Query("videos") videos: String,
                  @Query("access_key") accessKey: String?,
                  @Query("count") count: Int = 1,
                  @Query("offset") offset: Int = 0
    ): Flowable<BaseResponse<ListResponse<Video>>>

    //docs
    @GET("docs.getMessagesUploadServer")
    fun getDocUploadServer(@Query("type") type: String): Flowable<BaseResponse<UploadServer>>

    @Multipart
    @POST
    @Headers(NO_TOKEN_HEADER)
    fun uploadDoc(@Url url: String,
                  @Part file: MultipartBody.Part): Flowable<Uploaded>

    @GET("docs.save")
    fun saveDoc(@Query("file") file: String): Flowable<BaseResponse<MutableList<Doc>>>

    @GET("docs.get")
    fun getDocs(@Query("count") count: Int,
                @Query("offset") offset: Int): Flowable<BaseResponse<ListResponse<Doc>>>

    @GET("docs.add")
    fun addDoc(@Query("owner_id") ownerId: Int,
               @Query("doc_id") docId: Int,
               @Query("access_key") accessKey: String): Flowable<BaseResponse<Int>>

    //wall
    @Headers(NEW_VERSION_HEADER)
    @GET("wall.getById?extended=1")
    fun getWallPostById(@Query("posts") posts: String): Flowable<BaseResponse<WallPostResponse>>

    @GET("likes.add?type=post")
    fun like(@Query("owner_id") ownerId: Int,
             @Query("item_id") itemId: Int): Flowable<BaseResponse<LikesResponse>>

    @GET("likes.delete?type=post")
    fun unlike(@Query("owner_id") ownerId: Int,
               @Query("item_id") itemId: Int): Flowable<BaseResponse<LikesResponse>>

    @GET("wall.repost")
    fun repost(@Query("object") obj: String): Flowable<BaseResponse<JSONObject>>

    //longpoll
    @GET
    @Headers(NO_TOKEN_HEADER)
    fun connectLongPoll(
            @Url server: String,
            @Query("key") key: String,
            @Query("ts") ts: Int,
            @Query("act") act: String = "a_check",
            @Query("wait") wait: Int = 40,
            @Query("mode") mode: Int = 2,
            @Query("version") version: Int = 2
    ): Single<LongPollUpdate>

    @GET("messages.getLongPollServer")
    fun getLongPollServer(): Flowable<BaseResponse<LongPollServer>>

    //stats
    @GET("stats.trackVisitor")
    fun trackVisitor(): Flowable<BaseResponse<Int>>

    @GET
    @Headers(NO_TOKEN_HEADER)
    fun getFoaf(@Url url: String,
                @Query("id") id: Int): Flowable<ResponseBody>

    //other
    @GET
    @Headers(NO_TOKEN_HEADER)
    fun downloadFile(@Url url: String): Flowable<ResponseBody>

}