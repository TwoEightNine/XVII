package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Account : RealmObject() {

    companion object {
        const val TOKEN = "token"
        const val UID = "uid"
    }

    @PrimaryKey
    @SerializedName(UID)
    @Expose
    open var uid: Int = 0

    @SerializedName(TOKEN)
    @Expose
    open var token: String? = null

    @SerializedName("name")
    @Expose
    open var name: String? = null

    @SerializedName("photo")
    @Expose
    open var photo: String? = null

}