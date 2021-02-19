package com.twoeightnine.root.xvii.storage

import com.twoeightnine.root.xvii.App
import global.msnthrp.xvii.core.session.SessionProvider
import global.msnthrp.xvii.data.session.EncryptedCompatSessionProvider

object SessionProvider : SessionProvider by EncryptedCompatSessionProvider(App.context)