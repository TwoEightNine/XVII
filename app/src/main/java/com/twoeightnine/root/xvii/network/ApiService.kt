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

package com.twoeightnine.root.xvii.network

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.background.longpoll.models.LongPollServer
import com.twoeightnine.root.xvii.background.longpoll.models.LongPollUpdate
import com.twoeightnine.root.xvii.chatowner.model.api.MembersResponse
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.Pack
import com.twoeightnine.root.xvii.model.*
import com.twoeightnine.root.xvii.model.attachments.*
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
    }

    @GET("messages.getConversations?filter=all&extended=1")
    fun getConversations(
            @Query("count") count: Int,
            @Query("offset") offset: Int = 0
    ): Flowable<BaseResponse<ConversationsResponse>>

    @GET("messages.getConversationsById?extended=1")
    fun getConversationsById(
            @Query("peer_ids") peerIds: String,
            @Query("fields") fields: String = Conversation.FIELDS
    ): Flowable<BaseResponse<com.twoeightnine.root.xvii.chatowner.model.api.ConversationsResponse>>

    @GET("messages.deleteConversation")
    fun deleteConversation(
            @Query("peer_id") peerId: Int
    ): Flowable<BaseResponse<Any>>

    @GET("messages.getHistory?extended=1")
    fun getMessages(
            @Query("peer_id") peerId: Int,
            @Query("count") count: Int,
            @Query("offset") offset: Int = 0,
            @Query("fields") fields: String = User.FIELDS
    ): Flowable<BaseResponse<MessagesHistoryResponse>>

    @GET("messages.getHistory")
    fun getMessagesLite(
            @Query("peer_id") peerId: Int,
            @Query("count") count: Int,
            @Query("offset") offset: Int = 0
    ): Flowable<BaseResponse<MessagesHistoryResponse>>


    @GET("messages.markAsRead")
    fun markAsRead(
            @Query("peer_id") peerId: Int
    ): Flowable<BaseResponse<Int>>


    @GET("messages.delete")
    fun deleteMessages(
            @Query("message_ids") messageIds: String,
            @Query("delete_for_all") deleteForAll: Int
    ): Flowable<BaseResponse<JSONObject>>


    @FormUrlEncoded
    @POST("messages.edit")
    fun editMessage(
            @Field("peer_id") peerId: Int,
            @Field("message") message: String,
            @Field("message_id") messageId: Int,
            @Field("attachment") attachments: String? = null,
            @Field("keep_snippets") keepSnippets: Int = 1,
            @Field("keep_forward_messages") keepForwardedMessages: Int = 1
    ): Flowable<BaseResponse<Int>>


    @GET("messages.editChat")
    fun editChatTitle(
            @Query("chat_id") chatId: Int,
            @Query("title") newTitle: String
    ): Flowable<BaseResponse<Int>>


    @GET("messages.removeChatUser")
    fun kickUser(
            @Query("chat_id") chatId: Int,
            @Query("user_id") userId: Int
    ): Flowable<BaseResponse<Int>>


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


    @GET("messages.getById?extended=1")
    fun getMessageById(
            @Query("message_ids") messageIds: String
    ): Flowable<BaseResponse<MessagesHistoryResponse>>


    @GET("messages.markAsImportant")
    fun markMessagesAsImportant(
            @Query("message_ids") messageIds: String,
            @Query("important") important: Int
    ): Flowable<BaseResponse<MutableList<Int>>>


    @GET("messages.getHistoryAttachments")
    fun getHistoryAttachments(
            @Query("peer_id") peerId: Int,
            @Query("media_type") mediaType: String,
            @Query("count") count: Int,
            @Query("start_from") startFrom: String?
    ): Flowable<BaseResponse<AttachmentsResponse>>

    @GET("messages.search?extended=1")
    fun search(
        @Query("q") q: String,
        @Query("count") count: Int,
        @Query("offset") offset: Int,
        @Query("fields") fields: String = User.FIELDS
    ): Flowable<BaseResponse<SearchResponse>>

    @GET("messages.searchConversations?extended=1")
    fun searchConversations(
            @Query("q") q: String,
            @Query("count") count: Int,
            @Query("fields") fields: String = User.FIELDS
    ): Flowable<BaseResponse<SearchConversationsResponse>>


    @GET("messages.getConversationMembers?fields=last_seen,photo_100,online,domain")
    fun getConversationMembers(
            @Query("peer_id") peerId: Int
    ): Flowable<BaseResponse<MembersResponse>>


    @GET("messages.getImportantMessages?extended=1")
    fun getStarredMessages(
            @Query("count") count: Int,
            @Query("offset") offset: Int = 0,
            @Query("fields") fields: String = User.FIELDS
    ): Flowable<BaseResponse<MessagesResponse>>


    @GET("store.getStickersKeywords")
    fun getStickersKeywords(): Flowable<BaseResponse<StickersResponse>>


    @GET("store.getProducts?extended=1&filters=active&type=stickers")
    fun getStickers(): Flowable<BaseResponse<ListResponse<Pack>>>


    @GET("messages.setActivity")
    fun setActivity(
            @Query("peer_id") peerId: Int,
            @Query("type") type: String
    ): Flowable<BaseResponse<Int>>

    //users

    @GET("users.get")
    fun getUsers(@Query("user_ids") userIds: String,
                 @Query("fields") fields: String = User.FIELDS): Flowable<BaseResponse<MutableList<User>>>

    @Headers(NO_TOKEN_HEADER)
    @GET("users.get")
    fun checkUser(
            @Query("user_ids") userIds: String,
            @Query("access_token") token: String?,
            @Query("v") version: String = App.VERSION,
            @Query("fields") fields: String = User.FIELDS
    ): Flowable<BaseResponse<List<User>>>


    @GET("users.search")
    fun searchUsers(@Query("q") q: String,
                    @Query("fields") fields: String,
                    @Query("count") count: Int,
                    @Query("offset") offset: Int): Flowable<BaseResponse<ListResponse<User>>>


    @GET("account.ban")
    fun blockUser(
            @Query("owner_id") ownerId: Int
    ): Flowable<BaseResponse<Int>>


    @GET("account.unban")
    fun unblockUser(
            @Query("owner_id") ownerId: Int
    ): Flowable<BaseResponse<Int>>


    @GET("account.setPrivacy")
    fun setPrivacy(
            @Query("key") key: String,
            @Query("value") value: String
    ): Flowable<BaseResponse<Any>>

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


    @GET("photos.saveMessagesPhoto")
    fun saveMessagePhoto(@Query("photo") photo: String,
                         @Query("hash") hash: String,
                         @Query("server") server: Int): Flowable<BaseResponse<MutableList<Photo>>>

    //groups

    @GET("groups.getById")
    fun getGroups(
            @Query("group_ids") groupIds: String,
            @Query("fields") fields: String = Group.FIELDS
    ): Flowable<BaseResponse<MutableList<Group>>>


    @GET("groups.join")
    fun joinGroup(@Query("group_id") groupId: Int): Flowable<BaseResponse<Int>>


    @GET("groups.isMember")
    fun isGroupMember(@Query("group_id") groupId: Int,
                      @Query("user_id") userId: Int): Flowable<BaseResponse<Int>>

    //account

    @GET("account.setOffline")
    fun setOffline(): Flowable<BaseResponse<Int>>


    @GET("account.setOnline")
    fun setOnline(): Flowable<BaseResponse<Int>>

    // polls

    @GET("polls.addVote")
    fun addVote(
            @Query("owner_id") ownerId: Int,
            @Query("poll_id") pollId: Int,
            @Query("answer_ids") answerIds: String
    ): Flowable<BaseResponse<Int>>


    @GET("polls.getById")
    fun getPoll(
            @Query("owner_id") ownerId: Int,
            @Query("poll_id") pollId: Int
    ): Flowable<BaseResponse<Poll>>


    @GET("polls.deleteVote")
    fun clearVote(
            @Query("owner_id") ownerId: Int,
            @Query("poll_id") pollId: Int
    ): Flowable<BaseResponse<Int>>

    //video

    @GET("video.get")
    fun getVideos(@Query("videos") videos: String,
                  @Query("access_key") accessKey: String?,
                  @Query("count") count: Int = 1,
                  @Query("offset") offset: Int = 0
    ): Flowable<BaseResponse<ListResponse<Video>>>


    @GET("video.save?is_private=1")
    fun getVideoUploadServer(): Flowable<BaseResponse<UploadServer>>

    @Multipart
    @POST
    @Headers(NO_TOKEN_HEADER)
    fun uploadVideo(@Url url: String,
                    @Part file: MultipartBody.Part): Flowable<UploadedVideo>

    //docs
    @GET("docs.getMessagesUploadServer")
    fun getDocUploadServer(@Query("type") type: String): Flowable<BaseResponse<UploadServer>>


    @GET("docs.delete")
    fun deleteDoc(
            @Query("owner_id") ownerId: Int,
            @Query("doc_id") docId: Int
    ): Flowable<BaseResponse<Int>>

    @Multipart
    @POST
    @Headers(NO_TOKEN_HEADER)
    fun uploadDoc(@Url url: String,
                  @Part file: MultipartBody.Part): Flowable<Uploaded>


    @GET("docs.save")
    fun saveDoc(@Query("file") file: String): Flowable<BaseResponse<Attachment>>


    @GET("docs.get")
    fun getDocs(@Query("count") count: Int,
                @Query("offset") offset: Int): Flowable<BaseResponse<ListResponse<Doc>>>


    @GET("docs.add")
    fun addDoc(@Query("owner_id") ownerId: Int,
               @Query("doc_id") docId: Int,
               @Query("access_key") accessKey: String): Flowable<BaseResponse<Int>>

    //wall

    @GET("wall.getById?extended=1")
    fun getWallPostById(@Query("posts") posts: String): Flowable<BaseResponse<WallPostResponse>>


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
            @Query("mode") mode: Int = 130,
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