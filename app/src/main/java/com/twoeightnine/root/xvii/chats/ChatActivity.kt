package com.twoeightnine.root.xvii.chats

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.chats.messages.chat.ChatMessagesFragment
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.model.User

class ChatActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun createFragment(intent: Intent?): Fragment {
        val args = intent?.extras
        val forwarded = args?.getString(FORWARDED)
        val shareText = args?.getString(SHARE_TEXT)
        val shareImage = args?.getString(SHARE_IMAGE)
        val dialog = args?.getParcelable(DIALOG) ?: Dialog()
//        return ChatFragment.newInstance(dialog, forwarded, shareText, shareImage)
        return ChatMessagesFragment.newInstance(dialog, forwarded, shareText, shareImage)
    }

    companion object {
        const val DIALOG = "dialog"
        const val FORWARDED = "forwarded"
        const val SHARE_TEXT = "shareText"
        const val SHARE_IMAGE = "shareImage"

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