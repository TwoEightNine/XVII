package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.utils.time
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Meme(): RealmObject() {

    constructor(path: String) : this() {
        this.path = path
    }

    constructor(time: Int, path: String) : this(path) {
        this.id = time
    }

    @PrimaryKey
    @SerializedName("id")
    @Expose
    open var id: Int = time()

    @SerializedName("path")
    @Expose
    open var path: String? = null

    companion object {
        val MARKER = "meme"
        val addMeme = Meme(MARKER)
    }

    fun isAddMeme() = MARKER == path

}