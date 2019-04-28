package com.twoeightnine.root.xvii.utils

import com.twoeightnine.root.xvii.accounts.models.Account
import com.twoeightnine.root.xvii.dialogs.models.Dialog

object FakeData {
    val dialogs = arrayListOf(Dialog(
            title = "Cetamine Person",
            timeStamp = time(),
            photo = "https://sftextures.com/texture/453/0/452/birch-yellow-pollen-closeup-image-256x256.png",
            text = "пока искусство будет ориентироваться на зрителя, оно будет в жопе",
            isOut = true,
            isOnline = true,
            isPinned = true
    ), Dialog(
            title = "Sandra Pollock",
            timeStamp = time() - 163,
            photo = "https://upload.wikimedia.org/wikipedia/commons/5/58/Sandra_Palermo.jpg",
            text = "стикер",
            isOut = false,
            isOnline = true,
            isPinned = false
    ), Dialog(
            title = "Fresno Yummy",
            timeStamp = time() - 1000,
            photo = "https://upload.wikimedia.org/wikipedia/commons/7/7f/Cheryl_Ann_Krause.jpg",
            text = "эй, ты чего?",
            unreadCount = 3,
            isRead = false,
            isMute = true,
            isOut = false,
            isOnline = false,
            isPinned = false
    ), Dialog(
            title = "FKDPLC сходка --289",
            timeStamp = time() - 1112,
            text = "bien sûr ахахахха",
            unreadCount = 17,
            isRead = false,
            isMute = true,
            isOut = false,
            isOnline = false,
            isPinned = false
    ), Dialog(
            title = "Yela Tricks",
            timeStamp = time() - 1313,
            photo = "https://upload.wikimedia.org/wikipedia/commons/5/52/Madame_Fortuna.png",
            text = "большое спасибо тебе!",
            isRead = false,
            isOut = true,
            isOnline = false,
            isPinned = false
    )
    )

    val accounts = arrayListOf(Account(
            uid = 1753175317,
            name = "Fresno Yummy",
            photo = "https://upload.wikimedia.org/wikipedia/commons/7/7f/Cheryl_Ann_Krause.jpg",
            isRunning = true
    ), Account(
            uid = 1717531753,
            name = "Pierre Gardin",
            photo = "https://upload.wikimedia.org/wikipedia/commons/f/f6/Colour_grid_showing_256_color_image_composed_of_true_color_values.png"
    ))
}