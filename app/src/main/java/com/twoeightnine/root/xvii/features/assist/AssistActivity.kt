package com.twoeightnine.root.xvii.features.assist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.BaseActivity
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.stylize
import com.twoeightnine.root.xvii.utils.time
import kotlinx.android.synthetic.main.activity_assist.*

class AssistActivity : BaseActivity() {

    private var rewardedAd: RewardedAd? = null
    private var userAgreed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assist)
        progressBar.stylize()
        rewardedAd = RewardedAd(this, getAdId())
        rewardedAd?.loadAd(AdRequest.Builder().build(), AdLoadedCallback())

        showDisclaimer()
    }

    private fun showDisclaimer() {
        val dialog = AlertDialog.Builder(this)
                .setMessage(R.string.assist_prompt)
                .setPositiveButton(R.string.assist_yes) { _, _ -> showAd() }
                .setNegativeButton(R.string.assist_no) { _, _ -> finish() }
                .create()
        dialog.show()
        dialog.stylize()
    }

    private fun showAd() {
        if (rewardedAd?.isLoaded == true) {
            rewardedAd?.show(this@AssistActivity, AdCallback())
        } else {
            userAgreed = true
        }
    }

    private fun thankUser() {
        Prefs.lastAssistance = time()
        Toast.makeText(this@AssistActivity, R.string.assist_thank_you, Toast.LENGTH_LONG).show()
    }

    private fun getAdId() = if (BuildConfig.DEBUG) TEST_ID else ACTUAL_ID

    companion object {

        const val TEST_ID = "ca-app-pub-3940256099942544/5224354917"
        const val ACTUAL_ID = BuildConfig.AD_ID

        fun launch(context: Context?) {
            context?.startActivity(Intent(context, AssistActivity::class.java))
        }
    }

    private inner class AdLoadedCallback : RewardedAdLoadCallback() {
        override fun onRewardedAdLoaded() {
            super.onRewardedAdLoaded()
            if (userAgreed) {
                showAd()
            }
        }
    }

    private inner class AdCallback : RewardedAdCallback() {
        override fun onRewardedAdOpened() {
            // Ad opened.
        }

        override fun onRewardedAdClosed() {
            thankUser()
            finish()
        }

        override fun onUserEarnedReward(@NonNull reward: RewardItem) {
            thankUser()
        }

        override fun onRewardedAdFailedToShow(errorCode: Int) {
            // Ad failed to display.
        }
    }
}