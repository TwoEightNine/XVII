package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.utils.getPeerId
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class UserDb(): RealmObject() {

    constructor(user: User): this() {
        id = user.id
        firstName = user.firstName
        lastName = user.lastName
        photoMax = user.photoMax
    }

    constructor(message: Message): this() {
        id = getPeerId(message.userId, message.chatId)
        firstName = message.title
        lastName = message.title
        photoMax = message.photo
    }

    @PrimaryKey
    @SerializedName("id")
    @Expose
    open var id: Int = 0

    @SerializedName("first_name")
    @Expose
    open var firstName: String? = null

    @SerializedName("last_name")
    @Expose
    open var lastName: String? = null

    @SerializedName("photo_max_orig")
    @Expose
    open var photoMax: String? = null

    override fun toString() = "User{id: $id, firstName: $firstName, lastName: $lastName, photo: $photoMax}"
}