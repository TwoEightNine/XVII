/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.main

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseActivity
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerFactory
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsForwardFragment
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsFragment
import com.twoeightnine.root.xvii.features.FeaturesFragment
import com.twoeightnine.root.xvii.friends.fragments.FriendsFragment
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.search.SearchFragment
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.utils.deeplink.DeepLinkParser
import global.msnthrp.xvii.data.utils.FileUtils
import global.msnthrp.xvii.uikit.extensions.applyTopInsetMargin
import global.msnthrp.xvii.uikit.extensions.isVisible
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var apiUtils: ApiUtils

    private val bottomNavViewHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.bottom_navigation_height)
    }
    private val deepLinkHandler by lazy { DeepLinkHandler() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        App.appComponent?.inject(this)
        initFragments()
        bottomNavView.setOnNavigationItemSelectedListener(BottomViewListener())
        bottomNavView.selectedItemId = R.id.menu_dialogs

        startNotificationAlarm(this)
        apiUtils.trackVisitor()
        bottomNavView.paint(Munch.color.color)
        StatTool.get()?.incLaunch()

        ivSearch.setOnClickListener {
            startFragment<SearchFragment>()
        }
        ivSearch.paint(Munch.color.color)
        ivSearch.applyTopInsetMargin()

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavView) { view, insets ->
            view.updatePadding(bottom = insets.systemWindowInsetBottom)
            view.layoutParams.apply {
                height = bottomNavViewHeight + insets.systemWindowInsetBottom
                view.layoutParams = this
            }
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        deepLinkHandler.handle()
    }

    private fun initFragments() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.flFriends, FriendsFragment.newInstance())
                .replace(R.id.flDialogs, DialogsFragment.newInstance())
                .replace(R.id.flFeatures, FeaturesFragment.newInstance())
                .commit()
    }

    private fun showFragment(menuId: Int) {
        flFriends.setVisible(menuId == R.id.menu_friends)
        flDialogs.setVisible(menuId == R.id.menu_dialogs)
        flFeatures.setVisible(menuId == R.id.menu_features)
        ivSearch.setVisible(menuId != R.id.menu_features)
    }

    override fun getStatusBarColor() = ContextCompat.getColor(this, R.color.status_bar)

    override fun getNavigationBarColor() = Color.TRANSPARENT

    override fun getThemeId() = R.style.AppTheme_Main

    override fun onBackPressed() {
        if (flDialogs.isVisible()) {
            try {
                goHome(this)
            } catch (e: IllegalStateException) {
                L.def().throwable(e).log("unable to go home")
                super.onBackPressed()
            }
        } else {
            bottomNavView.selectedItemId = R.id.menu_dialogs
        }
    }

    companion object {

        fun launch(context: Context?) {
            launchActivity(context, MainActivity::class.java)
        }
    }

    private inner class BottomViewListener : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            showFragment(item.itemId)
            return true
        }

    }

    private inner class DeepLinkHandler {

        private val parser by lazy { DeepLinkParser() }

        fun handle() {
            when (val result = parser.parse(intent)) {
                is DeepLinkParser.Result.ChatOwner -> {
                    ChatOwnerFactory.launch(this@MainActivity, result.peerId)
                }
                is DeepLinkParser.Result.Chat -> {
                    openChatByPeer(result.peerId)
                }
                is DeepLinkParser.Result.Share -> {
                    copyFilesAsync(result.shareMediaUris) { sharedMediaPaths ->
                        val args = DialogsForwardFragment.createArgs(
                                shareText = result.shareText,
                                shareImage = sharedMediaPaths
                        )
                        startFragment<DialogsForwardFragment>(args)
                    }
                }
                DeepLinkParser.Result.Unknown -> Unit
            }
        }

        private fun openChatByPeer(peerId: Int) {
            val resolveCallable = {
                DefaultPeerResolver().resolvePeers(listOf(peerId))[peerId]
            }
            AsyncUtils.onIoThreadNullable(resolveCallable) { resolvedPeer ->
                resolvedPeer?.also {
                    ChatActivity.launch(this@MainActivity, it.peerId, it.peerName, it.peerPhoto)
                }
            }
        }

        private fun copyFilesAsync(uris: List<Uri>, onCopied: (List<String>) -> Unit) {
            if (uris.isEmpty()) {
                onCopied(emptyList())
                return
            }

            val copyCallable = {
                val filePaths = mutableListOf<String>()
                uris.forEach { uri ->
                    val tempFile = FileUtils.writeToTempFileFromContentUri(this@MainActivity, uri)
                    when {
                        tempFile != null -> tempFile.absolutePath
                        else -> uri.path
                    }?.let(filePaths::add)
                }
                L.tag("share").log(filePaths.joinToString { it.split('.').lastOrNull() ?: "null" })

                filePaths
            }
            val errorCallable = { throwable: Throwable ->
                L.tag("share").throwable(throwable).log("unable to copy shared media")
                onCopied(emptyList())
            }

            AsyncUtils.onIoThread(copyCallable, errorCallable, onCopied)
        }
    }
}