package com.twoeightnine.root.xvii.chats.messages.chat.usual

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.model.User

class ChatActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun createFragment(intent: Intent?): Fragment {
        val args = intent?.extras
        val forwarded = args?.getString(FORWARDED)
        val shareText = args?.getString(SHARE_TEXT)
        val shareImage = args?.getString(SHARE_IMAGE)
        val dialog = args?.getParcelable(DIALOG) ?: Dialog(
                peerId = args?.getInt(PEER_ID) ?: 0,
                title = args?.getString(TITLE) ?: "",
                photo = args?.getString(AVATAR)
        )
        return ChatMessagesFragment.newInstance(dialog, forwarded, shareText, shareImage)
    }

    override fun getDraggableBottomMargin(): Int = 200

    override fun getNavigationBarColor() = Color.TRANSPARENT

    companion object {
        const val DIALOG = "dialog"
        const val FORWARDED = "forwarded"
        const val SHARE_TEXT = "shareText"
        const val SHARE_IMAGE = "shareImage"
        const val PEER_ID = "peerId"
        const val TITLE = "title"
        const val AVATAR = "avatar"

        fun launch(context: Context?, chatOwner: ChatOwner) {
            launch(context, Dialog(
                    peerId = chatOwner.getPeerId(),
                    title = chatOwner.getTitle(),
                    photo = chatOwner.getAvatar()
            ))
        }

        fun launch(context: Context?, userId: Int, title: String,
                   avatar: String? = null, forwarded: String = "") {
            launch(context, Dialog(
                    peerId = userId,
                    title = title,
                    photo = avatar
            ), forwarded)

        }

        fun launch(context: Context?, dialog: Dialog,
                   forwarded: String? = null, shareText: String? = null,
                   shareImage: String? = null) {
            context ?: return

            context.startActivity(Intent(context, ChatActivity::class.java).apply {
                if (!forwarded.isNullOrEmpty()) {
                    putExtra(FORWARDED, forwarded)
                }
                if (!shareText.isNullOrEmpty()) {
                    putExtra(SHARE_TEXT, shareText)
                }
                if (!shareImage.isNullOrEmpty()) {
                    putExtra(SHARE_IMAGE, shareImage)
                }
                putExtra(DIALOG, dialog)
                flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }

        fun launch(context: Context?, user: User) {
            launch(context, Dialog(
                    peerId = user.id,
                    title = user.fullName,
                    photo = user.photo100
            ))
        }
    }
}