package com.twoeightnine.root.xvii.dagger

import com.twoeightnine.root.xvii.background.longpoll.models.LongPollServer
import com.twoeightnine.root.xvii.background.longpoll.models.LongPollUpdate
import com.twoeightnine.root.xvii.model.*
import com.twoeightnine.root.xvii.model.response.LongPollHistoryResponse
import com.twoeightnine.root.xvii.response.*
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
    }

    //dialog
    @GET("messages.getDialogs")
    fun getDialogs(@Query("offset") offset: Int,
                   @Query("count") count: Int): Flowable<ServerResponse<ListResponse<MessageContainer>>>

    @GET("messages.getHistory")
    fun getHistory(@Query("count") count: Int,
                   @Query("offset") offset: Int,
                   @Query("user_id") userId: Int): Flowable<ServerResponse<ListResponse<Message>>>

    @GET("messages.markAsRead")
    fun markAsRead(@Query("message_ids") messageIds: String): Flowable<ServerResponse<Int>>

    @GET("messages.deleteDialog")
    fun deleteDialogUser(@Query("user_id") userId: Int,
                         @Query("count") count: Int): Flowable<ServerResponse<Int>>

    @GET("messages.deleteDialog")
    fun deleteDialogChat(@Query("chat_id") chatId: Int,
                         @Query("count") count: Int): Flowable<ServerResponse<Int>>

    @GET("messages.delete")
    fun deleteMessages(@Query("message_ids") messageIds: String,
                       @Query("delete_for_all") deleteForAll: Int): Flowable<ServerResponse<JSONObject>>

    @GET("messages.edit?keep_snippets=1&keep_forward_messages=1")
    fun editMessage(@Query("peer_id") peerId: Int,
                    @Query("message") message: String,
                    @Query("message_id") messageId: Int): Flowable<ServerResponse<Int>>

    @GET("messages.send")
    fun send(@Query("user_id") userId: Int,
             @Query("message") message: String,
             @Query("forward_messages") fwdMessages: String?,
             @Query("attachment") attachment: String?,
             @Query("sticker_id") stickerId: Int,
             @Query("captcha_sid") captchaSid: String?,
             @Query("captcha_key") captchaKey: String?): Flowable<ServerResponse<Int>>

    @GET("messages.send")
    fun sendChat(@Query("chat_id") chatId: Int,
                 @Query("message") message: String,
                 @Query("forward_messages") fwdMessages: String?,
                 @Query("attachment") attachment: String?,
                 @Query("sticker_id") stickerId: Int,
                 @Query("captcha_sid") captchaSid: String?,
                 @Query("captcha_key") captchaKey: String?): Flowable<ServerResponse<Int>>

    @GET("messages.getById")
    fun getMessageById(@Query("message_ids") messageIds: String): Flowable<ServerResponse<ListResponse<Message>>>

    @GET("messages.markAsImportant")
    fun markMessagesAsImportant(@Query("message_ids") messageIds: String,
                                @Query("important") important: Int): Flowable<ServerResponse<MutableList<Int>>>

    @GET("messages.getHistoryAttachments")
    fun getHistoryAttachments(@Query("peer_id") peerId: Int,
                              @Query("media_type") mediaType: String,
                              @Query("count") count: Int,
                              @Query("start_from") startFrom: String): Flowable<ServerResponse<AttachmentsResponse>>

    @GET("messages.searchDialogs")
    fun searchDialogs(@Query("q") q: String,
                      @Query("limit") limit: Int,
                      @Query("fields") fields: String): Flowable<ServerResponse<MutableList<MessageSearchModel>>>

    @GET("messages.createChat")
    fun createChat(@Query("user_ids") userIds: String): Flowable<ServerResponse<Int>>

    @GET("messages.get?filters=8")
    fun getImportantMessages(@Query("count") count: Int,
                             @Query("offset") offset: Int): Flowable<ServerResponse<ListResponse<Message>>>

    @GET("store.getStickersKeywords")
    fun getStickers(): Flowable<ServerResponse<StickersResponse>>

    @GET("messages.setActivity")
    fun setActivity(
            @Query("user_id") userId: Int,
            @Query("type") type: String
    ): Flowable<ServerResponse<Int>>

    @GET("messages.removeChatUser")
    fun removeUser(@Query("chat_id") chatId: Int,
                   @Query("user_id") userId: Int): Flowable<ServerResponse<Int>>

    @GET("messages.editChat")
    fun renameChat(@Query("chat_id") chatId: Int,
                   @Query("title") title: String): Flowable<ServerResponse<Int>>

    //users
    @GET("users.get")
    fun getUsers(@Query("user_ids") userIds: String,
                 @Query("fields") fields: String): Flowable<ServerResponse<MutableList<User>>>

    @GET("users.search")
    fun searchUsers(@Query("q") q: String,
                    @Query("fields") fields: String,
                    @Query("count") count: Int,
                    @Query("offset") offset: Int): Flowable<ServerResponse<ListResponse<User>>>

    //friends
    @GET("friends.get?order=hints")
    fun getFriends(@Query("fields") fields: String,
                   @Query("count") count: Int,
                   @Query("offset") offset: Int): Flowable<ServerResponse<ListResponse<User>>>

    @GET("friends.getOnline")
    fun getOnlineFriends(@Query("count") count: Int,
                         @Query("offset") offset: Int): Flowable<ServerResponse<List<Int>>>

    @GET("friends.search")
    fun searchFriends(@Query("q") q: String,
                      @Query("fields") fields: String,
                      @Query("count") count: Int,
                      @Query("offset") offset: Int): Flowable<ServerResponse<ListResponse<User>>>

    //photos
    @GET("photos.getById")
    fun getPhotoById(@Query("photos") photos: String,
                     @Query("access_key") accessKey: String): Flowable<ServerResponse<MutableList<Photo>>>

    @GET("photos.copy")
    fun copyPhoto(@Query("owner_id") ownerId: Int,
                  @Query("photo_id") photoId: Int,
                  @Query("access_key") accessKey: String): Flowable<ServerResponse<Int>>

    @GET("photos.get?rev=1")
    fun getPhotos(@Query("owner_id") ownerId: Int,
                  @Query("album_id") albumId: String,
                  @Query("count") count: Int,
                  @Query("offset") offset: Int): Flowable<ServerResponse<ListResponse<Photo>>>

    @GET("photos.getMessagesUploadServer")
    fun getPhotoUploadServer(): Flowable<ServerResponse<UploadServer>>

    @Multipart
    @POST
    @Headers(NO_TOKEN_HEADER)
    fun uploadPhoto(@Url url: String,
                    @Part file: MultipartBody.Part): Flowable<Uploaded>

    @GET("photos.saveMessagesPhoto")
    fun saveMessagePhoto(@Query("photo") photo: String,
                         @Query("hash") hash: String,
                         @Query("server") server: Int): Flowable<ServerResponse<MutableList<Photo>>>

    //groups
    @GET("groups.getById")
    fun getGroups(@Query("group_ids") groupIds: String): Flowable<ServerResponse<MutableList<Group>>>

    @GET("groups.join")
    fun joinGroup(@Query("group_id") groupId: Int): Flowable<ServerResponse<Int>>

    @GET("groups.isMember")
    fun isGroupMember(@Query("group_id") groupId: Int,
                      @Query("user_id") userId: Int): Flowable<ServerResponse<Int>>

    //account
    @GET("account.setOffline")
    fun setOffline(): Flowable<ServerResponse<Int>>

    //video
    @GET("video.get")
    fun getVideos(@Query("videos") videos: String,
                  @Query("access_key") accessKey: String,
                  @Query("count") count: Int,
                  @Query("offset") offset: Int): Flowable<ServerResponse<ListResponse<Video>>>

    //docs
    @GET("docs.getMessagesUploadServer")
    fun getDocUploadServer(@Query("type") type: String): Flowable<ServerResponse<UploadServer>>

    @Multipart
    @POST
    @Headers(NO_TOKEN_HEADER)
    fun uploadDoc(@Url url: String,
                  @Part file: MultipartBody.Part): Flowable<Uploaded>

    @GET("docs.save")
    fun saveDoc(@Query("file") file: String): Flowable<ServerResponse<MutableList<Doc>>>

    @GET("docs.get")
    fun getDocs(@Query("count") count: Int,
                @Query("offset") offset: Int): Flowable<ServerResponse<ListResponse<Doc>>>

    @GET("docs.add")
    fun addDoc(@Query("owner_id") ownerId: Int,
               @Query("doc_id") docId: Int,
               @Query("access_key") accessKey: String): Flowable<ServerResponse<Int>>

    //wall
    @GET("wall.getById?extended=1")
    fun getWallPostById(@Query("posts") posts: String): Flowable<ServerResponse<WallPostResponse>>

    @GET("likes.add?type=post")
    fun like(@Query("owner_id") ownerId: Int,
             @Query("item_id") itemId: Int): Flowable<ServerResponse<LikesResponse>>

    @GET("likes.delete?type=post")
    fun unlike(@Query("owner_id") ownerId: Int,
               @Query("item_id") itemId: Int): Flowable<ServerResponse<LikesResponse>>

    @GET("wall.post")
    fun postToWall(@Query("owner_id") ownerId: Int,
                   @Query("message") message: String,
                   @Query("attachments") attachments: String): Flowable<ServerResponse<JSONObject>>

    @GET("wall.repost")
    fun repost(@Query("object") obj: String): Flowable<ServerResponse<JSONObject>>

    @GET("newsfeed.get?filters=post")
    fun getFeed(@Query("count") count: Int,
                @Query("start_from") startFrom: String): Flowable<ServerResponse<FeedResponse>>

    @GET("newsfeed.getRecommended?max_photos=10")
    fun getRecommended(@Query("count") count: Int): Flowable<ServerResponse<FeedResponse>>

    @GET("newsfeed.search?extended=1")
    fun searchFeed(@Query("q") q: String,
                   @Query("count") count: Int): Flowable<ServerResponse<FeedResponse>>

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
    fun getLongPollServer(): Flowable<ServerResponse<LongPollServer>>

    @GET("messages.getLongPollHistory")
    fun getLongPollHistory(@Query("ts") ts: Int,
                           @Query("events_limit") eventsLimit: Int): Flowable<ServerResponse<LongPollHistoryResponse>>

    //stats
    @GET("stats.trackVisitor")
    fun trackVisitor(): Flowable<ServerResponse<Int>>

    @GET
    @Headers(NO_TOKEN_HEADER)
    fun getFoaf(@Url url: String,
                @Query("id") id: Int): Flowable<ResponseBody>

    //other
    @GET
    @Headers(NO_TOKEN_HEADER)
    fun downloadFile(@Url url: String): Flowable<ResponseBody>

}