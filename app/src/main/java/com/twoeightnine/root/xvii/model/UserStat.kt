package com.twoeightnine.root.xvii.model

import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.utils.time

data class UserStat(
        var uid: Int = Session.uid,
        var timeStamp: Int = time(),
        var version: String = BuildConfig.VERSION_NAME,
        var info: String = "",
        var device: String = "??",
        var count: Int = Prefs.count,
        var name: String = Session.fullName,
        var photo: String = Session.photo
        )