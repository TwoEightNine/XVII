package com.twoeightnine.root.xvii.background.longpoll

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseActivity
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.utils.NotificationChannels
import com.twoeightnine.root.xvii.utils.setBottomInsetPadding
import com.twoeightnine.root.xvii.utils.setTopInsetPadding
import com.twoeightnine.root.xvii.utils.stylize
import kotlinx.android.synthetic.main.activity_explanation.*

class LongPollExplanationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explanation)

        btnOpen.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startActivity(Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    putExtra(Settings.EXTRA_CHANNEL_ID, NotificationChannels.backgroundService.id)
                })
            }
        }
        btnSupport.setOnClickListener {
            ChatActivity.launch(this, -App.GROUP, getString(R.string.app_name))
        }

        btnOpen.stylize()
        btnSupport.stylize()
        tvTitle.setTopInsetPadding()
        svContent.setBottomInsetPadding()
    }

    override fun getStatusBarColor() = ContextCompat.getColor(this, R.color.status_bar)
}