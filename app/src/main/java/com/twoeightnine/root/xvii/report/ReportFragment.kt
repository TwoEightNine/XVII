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

package com.twoeightnine.root.xvii.report

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.RadioButton
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.updateMargins
import androidx.fragment.app.viewModels
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WallPost
import com.twoeightnine.root.xvii.utils.showAlert
import com.twoeightnine.root.xvii.utils.showToast
import global.msnthrp.xvii.core.report.model.ReportReason
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetMargin
import global.msnthrp.xvii.uikit.extensions.setVisible
import global.msnthrp.xvii.uikit.extensions.setVisibleWithInvis
import global.msnthrp.xvii.uikit.extensions.show
import kotlinx.android.synthetic.main.fragment_report.*
import kotlin.math.PI
import kotlin.math.sin

class ReportFragment : BaseFragment() {

    private val viewModel by viewModels<ReportViewModel>()

    private val radioButtonMarginHorizontal by lazy {
        requireContext().resources.getDimensionPixelSize(R.dimen.margin16)
    }
    private val radioButtonMarginVertical by lazy {
        requireContext().resources.getDimensionPixelSize(R.dimen.margin8)
    }

    private var animator: ViewPropertyAnimator? = null
    private var reportBlock: ((reason: ReportReason, comment: String) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.fragment_report


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user: User? = arguments?.getParcelable(ARG_USER)
        val wallPost: WallPost? = arguments?.getParcelable(ARG_WALL_POST)

        when {
            user != null -> bindUser(user)
            wallPost != null -> bindWallPost(wallPost)
        }

        viewModel.loading.observe(::onLoadingChanged)
        viewModel.sent.observe { isSent -> if (isSent) onSent() }
        viewModel.error.observe { showAlert(context, it) }

        btnReport.setOnClickListener {
            withSelectedReason { reason, comment ->
                reportBlock?.invoke(reason, comment)
            }
        }

        btnReport.applyBottomInsetMargin()
    }

    private fun bindUser(user: User) {
        setReasons(ReportReason.forUser)
        reportBlock = { reason, comment ->
            viewModel.reportUser(user, reason, comment)
        }
        tvCommentHint.show()
        etComment.show()
    }

    private fun bindWallPost(wallPost: WallPost) {
        setReasons(ReportReason.forContent)
        reportBlock = { reason, _ ->
            viewModel.reportWallPost(wallPost, reason)
        }
    }

    private fun onLoadingChanged(isLoading: Boolean) {
        btnReport.setVisibleWithInvis(!isLoading)
        loader.setVisible(isLoading)
    }

    private fun onSent() {
        showToast(context, R.string.report_reported)
        onBackPressed()
    }

    private fun withSelectedReason(block: (reason: ReportReason, comment: String) -> Unit) {
        val selectedRadioButton = rgReason.children
                .filterIsInstance<RadioButton>()
                .find { it.isChecked }
        val reason = selectedRadioButton?.tag as? ReportReason

        if (reason != null) {
            block(reason, etComment.text.toString())
        } else {
            animateEmptyRadioGroup()
        }
    }

    private fun setReasons(reasons: Collection<ReportReason>) {
        rgReason.removeAllViews()
        reasons.forEach { reason ->
            RadioButton(requireContext()).apply {

                text = getString(reason.getStringRes())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                tag = reason

                rgReason.addView(this)

                (layoutParams as? ViewGroup.MarginLayoutParams)
                        ?.updateMargins(
                                left = radioButtonMarginHorizontal,
                                top = radioButtonMarginVertical,
                                right = radioButtonMarginHorizontal,
                                bottom = radioButtonMarginVertical
                        )
            }
        }
    }

    private fun animateEmptyRadioGroup() {
        animator?.cancel()
        rgReason.translationX = 0f

        val n = 2
        val animator = rgReason.animate()
                .translationX(16f)
                .setInterpolator { time -> sin(n * 2 * PI * time).toFloat() }
                .setDuration(300L)
        animator.start()

        this.animator = animator
    }

    @StringRes
    private fun ReportReason.getStringRes(): Int {
        return when (this) {
            ReportReason.PORN -> R.string.report_reason_porn
            ReportReason.SPAM -> R.string.report_reason_spam
            ReportReason.ABUSE -> R.string.report_reason_insult
            ReportReason.ADS -> R.string.report_reason_advertisement
            ReportReason.CP -> R.string.report_reason_cp
            ReportReason.VIOLENCE -> R.string.report_reason_violence
            ReportReason.DRUGS -> R.string.report_reason_drugs
            ReportReason.ADULT -> R.string.report_reason_adult
        }
    }

    companion object {

        private const val ARG_USER = "user"
        private const val ARG_WALL_POST = "wallPost"

        fun createArgs(
                user: User? = null,
                wallPost: WallPost? = null
        ): Bundle {
            return bundleOf(
                    ARG_USER to user,
                    ARG_WALL_POST to wallPost,
            )
        }
    }
}