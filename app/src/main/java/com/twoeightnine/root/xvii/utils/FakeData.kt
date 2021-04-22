package com.twoeightnine.root.xvii.utils

import global.msnthrp.xvii.core.accounts.model.Account
import global.msnthrp.xvii.data.dialogs.Dialog

object FakeData {

    const val ENABLED = true
    const val ENABLED_DIALOGS = false

    const val NAME_MINE = "mikhaïl bakounine"
    const val NAME_PERSON = "herzen"

    const val AVATAR_MINE = "https://i.pinimg.com/236x/14/ee/01/14ee015a8fde1a5d41f3f4536ff2ecd6.jpg"
    const val AVATAR_PERSON = "https://cdni.rbth.com/rbthmedia/images/web/en-rbth/images/2012-04/big/RIA-Novosti-gerzen-468.jpg"

    private const val TIME = 1619074800

    val dialogs = arrayListOf(Dialog(
            title = NAME_PERSON,
            timeStamp = TIME - 863,
            photo = AVATAR_PERSON,
            text = "life has taught me to think, but thinking has not taught me to live",
            isOut = true,
            isOnline = true,
            isPinned = true
    ), Dialog(
            peerId = 16,
            title = "philipp \uD83C\uDDE9\uD83C\uDDEA",
            timeStamp = TIME - 163,
            photo = "https://vk.com/images/camera_400.png",
            text = "sticker",
            isOut = false,
            isOnline = true,
            isPinned = false
    ), Dialog(
            peerId = 18,
            title = "kevin mittagessen",
            timeStamp = TIME - 666,
            photo = "https://live.staticflickr.com/3080/3140160186_7fc2e77eb3_b.jpg",
            text = "nothing new btw",
            isOut = true,
            isOnline = true,
            isPinned = false
    ), Dialog(
            title = NAME_MINE,
            timeStamp = TIME - 1000,
            photo = AVATAR_MINE,
            text = "3 photos",
            isRead = false,
            isOut = false,
            isOnline = false,
            isPinned = false
    ), Dialog(
            peerId = 777,
            title = "IWA 289",
            timeStamp = TIME - 1112,
            text = "bien sûr",
            unreadCount = 17,
            isRead = false,
            isMute = true,
            isOut = false,
            isOnline = false,
            isPinned = false
    ), Dialog(
            title = "first international",
            timeStamp = TIME - 1313,
            photo = "https://i.redd.it/uuaz0o89pf541.png",
            text = "and the United States of America was represented by Cameron",
            isRead = false,
            isOut = true,
            isOnline = false,
            isPinned = false
    )
    )

    val accounts = arrayListOf(Account(
            userId = 1753175317,
            token = "stub",
            name = NAME_MINE,
            photo = AVATAR_MINE,
            isActive = true
    ), Account(
            userId = 1753171753,
            token = "stub",
            name = "kriepie fank",
            photo = "https://www.shared.com/content/images/2018/09/CCTV.jpg",
            isActive = false
    ), Account(
            userId = 1717531753,
            name = "pierre gardin",
            token = "stub",
            photo = "https://upload.wikimedia.org/wikipedia/commons/f/f6/Colour_grid_showing_256_color_image_composed_of_true_color_values.png",
            isActive = false
    ))
}