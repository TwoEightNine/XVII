package com.twoeightnine.root.xvii.storage

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.utils.equalsDevUids
import global.msnthrp.xvii.core.session.SessionProvider
import global.msnthrp.xvii.data.session.EncryptedCompatSessionProvider

object SessionProvider : SessionProvider by EncryptedCompatSessionProvider(App.context) {

    fun hasToken() = token.isNullOrBlank().not()

    fun isTokenTheSame(token: String?) = token == this.token

    fun isUserIdTheSame(userId: Int) = userId == this.userId

    fun isDevUserId() = equalsDevUids(this.userId)

}