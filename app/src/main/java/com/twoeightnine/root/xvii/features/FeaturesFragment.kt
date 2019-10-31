package com.twoeightnine.root.xvii.features

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
import com.twoeightnine.root.xvii.accounts.activities.AccountsActivity
import com.twoeightnine.root.xvii.accounts.models.Account
import com.twoeightnine.root.xvii.activities.PinActivity
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.chats.messages.starred.StarredMessagesActivity
import com.twoeightnine.root.xvii.features.appearance.AppearanceActivity
import com.twoeightnine.root.xvii.features.datausage.DataUsageActivity
import com.twoeightnine.root.xvii.features.general.GeneralActivity
import com.twoeightnine.root.xvii.features.notifications.NotificationsActivity
import com.twoeightnine.root.xvii.lg.LgAlertDialog
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.web.WebActivity
import kotlinx.android.synthetic.main.fragment_features.*
import javax.inject.Inject

class FeaturesFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: FeaturesViewModel.Factory
    private lateinit var viewModel: FeaturesViewModel

    private var joinGroupShown = false

    override fun getLayoutId() = R.layout.fragment_features

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[FeaturesViewModel::class.java]
        viewModel.getAccount().observe(this, Observer { updateAccount(it) })
        viewModel.loadAccount()

        rlAnalyse.setOnClickListener { showToast(context, R.string.in_future_versions) }
        rlStarred.setOnClickListener { StarredMessagesActivity.launch(context) }

        rlAccounts.setOnClickListener { AccountsActivity.launch(context) }
        rlGeneral.setOnClickListener {
            GeneralActivity.launch(context)
            suggestJoin()
        }
        rlNotifications.setOnClickListener {
            NotificationsActivity.launch(context)
            suggestJoin()
        }
        rlAppearance.setOnClickListener {
            AppearanceActivity.launch(context)
            suggestJoin()
        }
        rlPin.setOnClickListener {
            onPinClicked()
            suggestJoin()
        }

        rlFeedback.setOnClickListener { ChatActivity.launch(context, -App.GROUP, getString(R.string.app_name)) }
        rlRate.setOnClickListener { context?.also { rate(it) } }
        rlShare.setOnClickListener { share() }
        rlDonate.setOnClickListener { simpleUrlIntent(context, App.DONATE_LINK) }
        rlPrivacy.setOnClickListener {
            WebActivity.launch(
                    context,
                    "file:///android_asset/privacy.html",
                    getString(R.string.privacy_policy)
            )
        }

        tvAbout.text = getString(R.string.aboutbig, BuildConfig.VERSION_NAME, BuildConfig.BUILD_TIME)
        tvAbout.setOnClickListener { showLogDialog() }
        tvAbout.setOnLongClickListener {
            DataUsageActivity.launch(context)
            true
        }

        llRoot.stylizeAll()
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
            PinActivity.launch(context, PinActivity.ACTION_SET)
        } else {
            val dialog = AlertDialog.Builder(context)
                    .setMessage(R.string.have_pin)
                    .setPositiveButton(R.string.edit) { _, _ ->
                        PinActivity.launch(context, PinActivity.ACTION_EDIT)
                    }
                    .setNegativeButton(R.string.reset) { _, _ ->
                        PinActivity.launch(context, PinActivity.ACTION_RESET)
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
        if (!joinGroupShown && !equalsDevUids(Session.uid)) {
            viewModel.checkMembership { inGroup ->
                if (!inGroup) {
                    showConfirm(context, getString(R.string.join_us)) { yes ->
                        if (yes) {
                            viewModel.joinGroup()
                        }
                    }
                }
                joinGroupShown = true
            }
        }
    }

    companion object {

        fun newInstance() = FeaturesFragment()
    }
}