package com.twoeightnine.root.xvii.dagger

import com.twoeightnine.root.xvii.consts.Api
import com.twoeightnine.root.xvii.model.*
import com.twoeightnine.root.xvii.model.response.LongPollHistoryResponse
import com.twoeightnine.root.xvii.model.response.LongPollResponse
import com.twoeightnine.root.xvii.response.*
import io.reactivex.Flowable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.http.*

interface ApiService {

    //dialog
    @GET("messages.getDialogs?v=${Api.VERSION}")
    fun getDialogs(@Query("access_token") token: String,
                   @Query("offset") offset: Int,
                   @Query("count") count: Int): Flowable<ServerResponse<ListResponse<MessageContainer>>>

    @GET("messages.getHistory?v=${Api.VERSION}")
    fun getHistory(@Query("access_token") token: String,
                   @Query("count") count: Int,
                   @Query("offset") offset: Int,
                   @Query("user_id") userId: Int): Flowable<ServerResponse<ListResponse<Message>>>

    @GET("messages.markAsRead?v=${Api.VERSION}")
    fun markAsRead(@Query("access_token") token: String,
                   @Query("message_ids") messageIds: String): Flowable<ServerResponse<Int>>

    @GET("messages.deleteDialog?v=${Api.VERSION}")
    fun deleteDialogUser(@Query("access_token") token: String,
                         @Query("user_id") userId: Int,
                         @Query("count") count: Int): Flowable<ServerResponse<Int>>

    @GET("messages.deleteDialog?v=${Api.VERSION}")
    fun deleteDialogChat(@Query("access_token") token: String,
                         @Query("chat_id") chatId: Int,
                         @Query("count") count: Int): Flowable<ServerResponse<Int>>

    @GET("messages.delete?v=${Api.VERSION}")
    fun deleteMessages(@Query("access_token") token: String,
                       @Query("message_ids") messageIds: String): Flowable<ServerResponse<JSONObject>>

    @GET("messages.send?v=${Api.VERSION}")
    fun send(@Query("access_token") token: String,
             @Query("user_id") userId: Int,
             @Query("message") message: String,
             @Query("forward_messages") fwdMessages: String?,
             @Query("attachment") attachment: String?,
             @Query("sticker_id") stickerId: Int,
             @Query("captcha_sid") captchaSid: String?,
             @Query("captcha_key") captchaKey: String?): Flowable<ServerResponse<Int>>

    @GET("messages.send?v=${Api.VERSION}")
    fun sendChat(@Query("access_token") token: String,
                 @Query("chat_id") chatId: Int,
                 @Query("message") message: String,
                 @Query("forward_messages") fwdMessages: String?,
                 @Query("attachment") attachment: String?,
                 @Query("sticker_id") stickerId: Int,
                 @Query("captcha_sid") captchaSid: String?,
                 @Query("captcha_key") captchaKey: String?): Flowable<ServerResponse<Int>>

    @GET("messages.getById?v=${Api.VERSION}")
    fun getMessageById(@Query("access_token") token: String,
                       @Query("message_ids") messageIds: String): Flowable<ServerResponse<ListResponse<Message>>>

    @GET("messages.markAsImportant?v=${Api.VERSION}")
    fun markMessagesAsImportant(@Query("access_token") token: String,
                        @Query("message_ids") messageIds: String,
                        @Query("important") important: Int): Flowable<ServerResponse<MutableList<Int>>>

    @GET("messages.getHistoryAttachments?v=${Api.VERSION}")
    fun getHistoryAttachments(@Query("access_token") token: String,
                              @Query("peer_id") peerId: Int,
                              @Query("media_type") mediaType: String,
                              @Query("count") count: Int,
                              @Query("start_from") startFrom: String): Flowable<ServerResponse<AttachmentsResponse>>

    @GET("messages.searchDialogs?v=${Api.VERSION}")
    fun searchDialogs(@Query("access_token") token: String,
                      @Query("q") q: String,
                      @Query("limit") limit: Int,
                      @Query("fields") fields: String): Flowable<ServerResponse<MutableList<MessageSearchModel>>>

    @GET("messages.createChat?v=${Api.VERSION}")
    fun createChat(@Query("access_token") token: String,
                   @Query("user_ids") userIds: String): Flowable<ServerResponse<Int>>

    @GET("messages.get?filters=8&v=${Api.VERSION}")
    fun getImportantMessages(@Query("access_token") token: String,
                             @Query("count") count: Int,
                             @Query("offset") offset: Int): Flowable<ServerResponse<ListResponse<Message>>>

    @GET("store.getStickersKeywords?v=${Api.VERSION}")
    fun getStickers(@Query("access_token") token: String): Flowable<ServerResponse<StickersResponse>>

    @GET("messages.setActivity?type=typing&v=${Api.VERSION}")
    fun setActivity(@Query("access_token") token: String,
                    @Query("user_id") userId: Int): Flowable<ServerResponse<Int>>

    @GET("messages.removeChatUser?v=${Api.VERSION}")
    fun removeUser(@Query("access_token") token: String,
                   @Query("chat_id") chatId: Int,
                   @Query("user_id") userId: Int): Flowable<ServerResponse<Int>>

    @GET("messages.editChat?v=${Api.VERSION}")
    fun renameChat(@Query("access_token") token: String,
                   @Query("chat_id") chatId: Int,
                   @Query("title") title: String): Flowable<ServerResponse<Int>>

    //users
    @GET("users.get?v=${Api.VERSION}")
    fun getUsers(@Query("access_token") token: String,
                 @Query("user_ids") userIds: String,
                 @Query("fields") fields: String): Flowable<ServerResponse<MutableList<User>>>

    @GET("users.search?v=${Api.VERSION}")
    fun search(@Query("access_token") token: String,
               @Query("q") q: String,
               @Query("fields") fields: String,
               @Query("count") count: Int,
               @Query("offset") offset: Int): Flowable<ServerResponse<ListResponse<User>>>

    //friends
    @GET("friends.get?order=hints&v=${Api.VERSION}")
    fun getFriends(@Query("access_token") token: String,
                   @Query("fields") fields: String,
                   @Query("count") count: Int,
                   @Query("offset") offset: Int): Flowable<ServerResponse<ListResponse<User>>>

    @GET("friends.getOnline?v=${Api.VERSION}")
    fun getOnlineFriends(@Query("access_token") token: String,
                         @Query("count") count: Int,
                         @Query("offset") offset: Int): Flowable<ServerResponse<List<Int>>>

    //photos
    @GET("photos.getById?v=${Api.VERSION}")
    fun getPhotoById(@Query("access_token") token: String,
                     @Query("photos") photos: String,
                     @Query("access_key") accessKey: String): Flowable<ServerResponse<MutableList<Photo>>>

    @GET("photos.copy?v=${Api.VERSION}")
    fun copyPhoto(@Query("access_token") token: String,
                  @Query("owner_id") ownerId: Int,
                  @Query("photo_id") photoId: Int,
                  @Query("access_key") accessKey: String): Flowable<ServerResponse<Int>>

    @GET("photos.get?rev=1&v=${Api.VERSION}")
    fun getPhotos(@Query("access_token") token: String,
                  @Query("owner_id") ownerId: Int,
                  @Query("album_id") albumId: String,
                  @Query("count") count: Int,
                  @Query("offset") offset: Int): Flowable<ServerResponse<ListResponse<Photo>>>

    @GET("photos.getMessagesUploadServer?v=${Api.VERSION}")
    fun getPhotoUploadServer(@Query("access_token") token: String): Flowable<ServerResponse<UploadServer>>

    @Multipart
    @POST
    fun uploadPhoto(@Url url: String,
                    @Part file: MultipartBody.Part): Flowable<Uploaded>

    @GET("photos.saveMessagesPhoto?v=${Api.VERSION}")
    fun saveMessagePhoto(@Query("access_token") token: String,
                         @Query("photo") photo: String,
                         @Query("hash") hash: String,
                         @Query("server") server: Int): Flowable<ServerResponse<MutableList<Photo>>>

    //groups
    @GET("groups.getById?v=${Api.VERSION}")
    fun getGroups(@Query("access_token") token: String,
                  @Query("group_ids") groupIds: String): Flowable<ServerResponse<MutableList<Group>>>

    @GET("groups.join?v=${Api.VERSION}")
    fun joinGroup(@Query("access_token") token: String,
                  @Query("group_id") groupId: Int): Flowable<ServerResponse<Int>>

    @GET("groups.isMember?v=${Api.VERSION}")
    fun isGroupMember(@Query("access_token") token: String,
                      @Query("group_id") groupId: Int,
                      @Query("user_id") userId: Int): Flowable<ServerResponse<Int>>

    //account
    @GET("account.setOffline?v=${Api.VERSION}")
    fun setOffline(@Query("access_token") token: String): Flowable<ServerResponse<Int>>

    //video
    @GET("video.get?v=${Api.VERSION}")
    fun getVideos(@Query("access_token") token: String,
                  @Query("videos") videos: String,
                  @Query("access_key") accessKey: String,
                  @Query("count") count: Int,
                  @Query("offset") offset: Int): Flowable<ServerResponse<ListResponse<Video>>>

    //docs
    @GET("docs.getMessagesUploadServer?v=${Api.VERSION}")
    fun getDocUploadServer(@Query("access_token") token: String,
                           @Query("type") type: String): Flowable<ServerResponse<UploadServer>>

    @Multipart
    @POST
    fun uploadDoc(@Url url: String,
                  @Part file: MultipartBody.Part): Flowable<Uploaded>

    @GET("docs.save?v=${Api.VERSION}")
    fun saveDoc(@Query("access_token") token: String,
                @Query("file") file: String): Flowable<ServerResponse<MutableList<Doc>>>

    @GET("docs.get?v=${Api.VERSION}")
    fun getDocs(@Query("access_token") token: String,
                @Query("count") count: Int,
                @Query("offset") offset: Int): Flowable<ServerResponse<ListResponse<Doc>>>

    @GET("docs.add?v=${Api.VERSION}")
    fun addDoc(@Query("access_token") token: String,
               @Query("owner_id") ownerId: Int,
               @Query("doc_id") docId: Int,
               @Query("access_key") accessKey: String): Flowable<ServerResponse<Int>>

    //wall
    @GET("wall.getById?extended=1&v=${Api.VERSION}")
    fun getWallPostById(@Query("access_token") token: String,
                        @Query("posts") posts: String): Flowable<ServerResponse<WallPostResponse>>

    @GET("likes.add?type=post&v=${Api.VERSION}")
    fun like(@Query("access_token") token: String,
             @Query("owner_id") ownerId: Int,
             @Query("item_id") itemId: Int): Flowable<ServerResponse<LikesResponse>>

    @GET("wall.post?v=${Api.VERSION}")
    fun postToWall(@Query("access_token") token: String,
                   @Query("owner_id") ownerId: Int,
                   @Query("message") message: String,
                   @Query("attachments") attachments: String): Flowable<ServerResponse<JSONObject>>

    @GET("newsfeed.get?filters=post&v=${Api.VERSION}")
    fun getFeed(@Query("access_token") token: String,
                         @Query("count") count: Int,
                         @Query("start_from") startFrom: String): Flowable<ServerResponse<FeedResponse>>

    @GET("newsfeed.getRecommended?max_photos=10&v=${Api.VERSION}")
    fun getRecommended(@Query("access_token") token: String,
                @Query("count") count: Int): Flowable<ServerResponse<FeedResponse>>

    //longpoll
    @GET
    fun connect(@Url server: String,
                @Query("key") key: String,
                @Query("ts") ts: Int,
                @Query("act") act: String,
                @Query("wait") wait: Int,
                @Query("mode") mode: Int): Flowable<LongPollResponse>

    @GET("messages.getLongPollServer?v=${Api.VERSION}")
    fun getLongPollServer(@Query("access_token") token: String): Flowable<ServerResponse<LongPollServer>>

    @GET("messages.getLongPollHistory?v=${Api.VERSION}")
    fun getLongPollHistory(@Query("access_token") token: String,
                           @Query("ts") ts: Int,
                           @Query("events_limit") eventsLimit: Int): Flowable<ServerResponse<LongPollHistoryResponse>>

    //stats
    @GET("stats.trackVisitor?v=${Api.VERSION}")
    fun trackVisitor(@Query("access_token") token: String): Flowable<ServerResponse<Int>>

    @GET
    fun getFoaf(@Url url: String,
                @Query("id") id: Int): Flowable<ResponseBody>

    //other
    @GET()
    fun downloadFile(@Url url: String): Flowable<ResponseBody>

}