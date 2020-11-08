package com.twoeightnine.root.xvii.features

import android.graphics.Paint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.accounts.fragments.AccountsFragment
import com.twoeightnine.root.xvii.accounts.models.Account
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerActivity
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.chats.messages.starred.StarredMessagesFragment
import com.twoeightnine.root.xvii.features.appearance.AppearanceActivity
import com.twoeightnine.root.xvii.features.general.GeneralFragment
import com.twoeightnine.root.xvii.features.notifications.NotificationsFragment
import com.twoeightnine.root.xvii.lg.LgAlertDialog
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.pin.SecurityFragment
import com.twoeightnine.root.xvii.scheduled.ui.ScheduledMessagesFragment
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.UiKitFragment
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.web.WebFragment
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import global.msnthrp.xvii.uikit.extensions.lowerIf
import kotlinx.android.synthetic.main.fragment_features.*
import java.util.*
import javax.inject.Inject

class FeaturesFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: FeaturesViewModel.Factory
    private lateinit var viewModel: FeaturesViewModel

    override fun getLayoutId() = R.layout.fragment_features

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[FeaturesViewModel::class.java]

        tvSwitchAccount.paintFlags = tvSwitchAccount.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        tvSwitchAccount.paint(Munch.color.color)

        xiAnalyze.setOnClickListener { showToast(context, R.string.in_future_versions) }
        xiStarred.setOnClickListener { startFragment<StarredMessagesFragment>() }
        xiScheduledMessages.setOnClickListener { startFragment<ScheduledMessagesFragment>() }

        rlAccounts.setOnClickListener { ChatOwnerActivity.launch(context, Session.uid) }
        tvSwitchAccount.setOnClickListener { startFragment<AccountsFragment>() }
        xiGeneral.setOnClickListener {
            startFragment<GeneralFragment>()
            suggestJoin()
        }
        xiNotifications.setOnClickListener {
            startFragment<NotificationsFragment>()
            suggestJoin()
        }
        xiAppearance.setOnClickListener {
            AppearanceActivity.launch(context)
            suggestJoin()
        }
        xiSecurity.setOnClickListener { startFragment<SecurityFragment>() }

        xiSupport.setOnClickListener { ChatActivity.launch(context, -App.GROUP, getString(R.string.app_name)) }
        xiRate.setOnClickListener { context?.also { rate(it) } }
        xiShare.setOnClickListener { share() }
        xiPrivacy.setOnClickListener { resolvePrivacyPolicy() }
        xiSourceCode.setOnClickListener { simpleUrlIntent(context, GITHUB_URL) }

        xlAbout.text = getString(R.string.aboutbig, BuildConfig.VERSION_NAME, BuildConfig.BUILD_TIME)
        xlAbout.setOnClickListener { showLogDialog() }
        xlAbout.setOnLongClickListener { startFragment<UiKitFragment>(); true }

//        rlRoot.stylizeAll()
        svContent.applyBottomInsetPadding()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getAccount().observe(viewLifecycleOwner, ::updateAccount)
        viewModel.lastSeen.observe(viewLifecycleOwner) { (isOnline, timeStamp) ->
            tvLastSeen.text = getLastSeenText(resources, isOnline, timeStamp, 0)
        }
        viewModel.loadAccount()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateLastSeen()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun updateAccount(account: Account) {
        civPhoto.load(account.photo)
        tvName.text = account.name
        tvName.lowerIf(Prefs.lowerTexts)
    }

    private fun showLogDialog() {
        LgAlertDialog(context ?: return).show()
    }

    private fun share() {
        viewModel.shareXvii({
            showToast(context, R.string.shared)
        }, { showError(context, it) })
    }

    private fun suggestJoin() {
        if (time() - Prefs.joinShownLast <= SHOW_JOIN_DELAY) return // one week

        Prefs.joinShownLast = time()
        if (!equalsDevUids(Session.uid)) {
            viewModel.checkMembership { inGroup ->
                if (!inGroup) {
                    val dialog = AlertDialog.Builder(context ?: return@checkMembership)
                            .setMessage(R.string.join_us)
                            .setPositiveButton(R.string.join) { _, _ -> viewModel.joinGroup() }
                            .setNegativeButton(R.string.cancel, null)
                            .create()
                    dialog.show()
                    dialog.stylize()
                }
            }
        }
    }

    private fun resolvePrivacyPolicy() {
        val url = if (Locale.getDefault() == Locale("ru", "RU")) {
            PRIVACY_RU
        } else {
            PRIVACY_WORLD
        }
        startFragment<WebFragment>(
                WebFragment.createArgs(url, getString(R.string.privacy_policy))
        )
    }

    companion object {

        const val PRIVACY_WORLD = "https://github.com/TwoEightNine/XVII/blob/master/privacy.md"
        const val PRIVACY_RU = "https://github.com/TwoEightNine/XVII/blob/master/privacy_ru.md"

        const val GITHUB_URL = "https://github.com/twoeightnine/xvii"

        const val SHOW_JOIN_DELAY = 3600 * 24 * 7 // one week

        fun newInstance() = FeaturesFragment()
    }
}