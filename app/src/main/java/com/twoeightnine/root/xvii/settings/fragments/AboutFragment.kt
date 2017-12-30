package com.twoeightnine.root.xvii.settings.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.TestLandscapeActivity
import com.twoeightnine.root.xvii.chats.fragments.ChatFragment
import com.twoeightnine.root.xvii.consts.Api
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.fragments.WebFragment
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.utils.*

/**
 * Created by root on 2/2/17.
 */

class AboutFragment : BaseFragment() {

    @BindView(R.id.tvFeedback)
    lateinit var tvFeedback: TextView
    @BindView(R.id.tvRate)
    lateinit var tvRate: TextView
    @BindView(R.id.tvPrivacy)
    lateinit var tvPrivacy: TextView
    @BindView(R.id.tvShare)
    lateinit var tvShare: TextView
    @BindView(R.id.tvAboutApp)
    lateinit var tvAboutApp: TextView
    @BindView(R.id.tvDebug)
    lateinit var tvDebug: TextView

    @BindView(R.id.tvCrash)
    lateinit var tvCrash: TextView
    @BindView(R.id.tvLandscape)
    lateinit var tvLandscape: TextView

    private lateinit var crashTest: String

    private var apiUtils = ApiUtils()

    override fun bindViews(view: View) {
        ButterKnife.bind(this, view)
        initViews()
        apiUtils.checkMembership({ if (!it) showJoinDialog() })
    }

    override fun getLayout() = R.layout.fragment_about

    private fun initViews() {
        tvFeedback.setOnClickListener { _ ->
            val message = Message(
                    0, 0, -Api.GROUP, 0, 0, getString(R.string.app_name), "", null
            )
            rootActivity.loadFragment(ChatFragment.newInstance(message))
        }
        tvRate.setOnClickListener { _ -> rate(context) }
        tvPrivacy.setOnClickListener { _ ->
            rootActivity.loadFragment(WebFragment.newInstance(
                    "file:///android_asset/privacy.html",
                    getString(R.string.privacy_policy)))
        }
        tvShare.setOnClickListener { share() }
        tvAboutApp.text = getString(R.string.aboutBig, BuildConfig.VERSION_NAME)
        tvAboutApp.setOnLongClickListener { _ ->
            showLogDialog()
            true
        }
        tvAboutApp.setOnClickListener { showLogDialog() }
        if (equalsDevUids(Session.uid)) {
            tvCrash.visibility = View.VISIBLE
            tvLandscape.visibility = View.VISIBLE
            tvCrash.setOnLongClickListener {
                val r = crashTest[7]
                true
            }
            tvLandscape.setOnLongClickListener {
                startActivity(Intent(rootActivity, TestLandscapeActivity::class.java))
                true
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.about))
    }

    private fun showLogDialog() {
        val logs = Lg.logs.joinToString(separator = "\n")
        val dialog = AlertDialog.Builder(activity)
                .setMessage(logs)
                .setPositiveButton("copy", { _, _ -> copyToClip(logs)})
                .setNegativeButton("close", null)
                .create()
        dialog.show()
        Style.forDialog(dialog)
    }

    private fun showJoinDialog() {
        val dialog = AlertDialog.Builder(activity)
                .setMessage(R.string.join_us)
                .setPositiveButton(android.R.string.ok, { _, _ -> apiUtils.joinGroup() })
                .setNegativeButton(android.R.string.cancel, null)
                .create()

        dialog.show()
        Style.forDialog(dialog)
    }

    private fun share() {
        apiUtils.shareToWall(
                Session.uid,
                getString(R.string.share_text),
                "https://play.google.com/store/apps/details?id=" + context.packageName,
                { showCommon(context, R.string.shared) }
        )
    }
}
