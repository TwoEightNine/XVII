package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass


@RealmClass
open class GroupDb(): RealmObject() {

    constructor(group: Group): this() {
        this.id = group.id
        this.name = group.name
        this.photo = group.photo100
    }

    @PrimaryKey
    @SerializedName("id")
    @Expose
    open var id: Int = 0

    @SerializedName("name")
    @Expose
    open var name: String = ""

    @SerializedName("photo")
    @Expose
    open var photo: String = ""

    override fun toString() = "Group{id: $id, name: $name, photo: $photo}"
}