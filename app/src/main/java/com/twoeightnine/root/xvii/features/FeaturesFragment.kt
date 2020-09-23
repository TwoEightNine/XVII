package com.twoeightnine.root.xvii.features

import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
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
import com.twoeightnine.root.xvii.features.assist.AssistActivity
import com.twoeightnine.root.xvii.features.general.GeneralFragment
import com.twoeightnine.root.xvii.features.notifications.NotificationsFragment
import com.twoeightnine.root.xvii.lg.LgAlertDialog
import com.twoeightnine.root.xvii.main.InsetViewModel
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.pin.Camera2BasicFragment
import com.twoeightnine.root.xvii.pin.PinActivity
import com.twoeightnine.root.xvii.scheduled.ui.ScheduledMessagesFragment
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.web.WebFragment
import kotlinx.android.synthetic.main.fragment_features.*
import java.util.*
import javax.inject.Inject

class FeaturesFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: FeaturesViewModel.Factory
    private lateinit var viewModel: FeaturesViewModel

    private val insetViewModel by lazy {
        ViewModelProviders.of(activity ?: return@lazy null)[InsetViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.fragment_features

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[FeaturesViewModel::class.java]
        viewModel.getAccount().observe(this, Observer { updateAccount(it) })
        viewModel.loadAccount()

        tvSwitchAccount.paintFlags = tvSwitchAccount.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        tvSwitchAccount.stylize()

        rlAnalyse.setOnClickListener { showToast(context, R.string.in_future_versions) }
        rlStarred.setOnClickListener { startFragment<StarredMessagesFragment>() }
        rlScheduledMessages.setOnClickListener { startFragment<ScheduledMessagesFragment>() }

        rlAccounts.setOnClickListener { ChatOwnerActivity.launch(context, Session.uid) }
        tvSwitchAccount.setOnClickListener { startFragment<AccountsFragment>() }
        rlGeneral.setOnClickListener {
            startFragment<GeneralFragment>()
            suggestJoin()
        }
        rlNotifications.setOnClickListener {
            startFragment<NotificationsFragment>()
            suggestJoin()
        }
        rlAppearance.setOnClickListener {
            AppearanceActivity.launch(context)
            suggestJoin()
        }
        rlPin.setOnClickListener {
            onPinClicked()
        }

        rlFeedback.setOnClickListener { ChatActivity.launch(context, -App.GROUP, getString(R.string.app_name)) }
        rlRate.setOnClickListener { context?.also { rate(it) } }
        rlContribute.setOnClickListener { AssistActivity.launch(context) }
        rlShare.setOnClickListener { share() }
        rlPrivacy.setOnClickListener { resolvePrivacyPolicy() }
        rlSourceCode.setOnClickListener { simpleUrlIntent(context, GITHUB_URL) }

        tvAbout.text = getString(R.string.aboutbig, BuildConfig.VERSION_NAME, BuildConfig.BUILD_TIME)
        tvAbout.setOnClickListener { showLogDialog() }
        tvAbout.setOnLongClickListener { startFragment<Camera2BasicFragment>(); true }

        rlRoot.stylizeAll()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        insetViewModel?.topInset?.observe(viewLifecycleOwner, Observer { top ->
            rlAccounts.setTopMargin(top)
        })
        insetViewModel?.bottomInset?.observe(viewLifecycleOwner, Observer { bottom ->
            val bottomNavHeight = context?.resources?.getDimensionPixelSize(R.dimen.bottom_navigation_height) ?: 0
            svContent.setBottomPadding(bottom + bottomNavHeight)
        })
    }

    override fun onResume() {
        super.onResume()
        rlContribute.setVisible(time() - Prefs.lastAssistance > ASSISTANCE_DELAY)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    private fun updateAccount(account: Account) {
        ivPhoto.load(account.photo)
        tvName.text = account.name
        if (Prefs.lowerTexts) tvName.lower()
    }

    private fun onPinClicked() {
        val context = context ?: return

        val pin = Prefs.pin
        if (TextUtils.isEmpty(pin)) {
            PinActivity.launch(context, PinActivity.Action.SET)
            suggestJoin()
        } else {
            val dialog = AlertDialog.Builder(context)
                    .setMessage(R.string.have_pin)
                    .setPositiveButton(R.string.edit) { _, _ ->
                        PinActivity.launch(context, PinActivity.Action.EDIT)
                    }
                    .setNegativeButton(R.string.reset) { _, _ ->
                        PinActivity.launch(context, PinActivity.Action.RESET)
                    }
                    .create()

            dialog.show()
            dialog.stylize()
        }
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

        const val ASSISTANCE_DELAY = 60 * 2 // two minutes

        fun newInstance() = FeaturesFragment()
    }
}