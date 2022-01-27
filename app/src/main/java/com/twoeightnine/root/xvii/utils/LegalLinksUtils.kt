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

package com.twoeightnine.root.xvii.utils

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.StringRes
import com.twoeightnine.root.xvii.R
import java.util.*

object LegalLinksUtils {

    private const val PRIVACY_WORLD = "https://github.com/TwoEightNine/XVII/blob/master/privacy.md"
    private const val PRIVACY_RU = "https://github.com/TwoEightNine/XVII/blob/master/privacy_ru.md"

    private const val VK_TOS = "https://m.vk.com/terms"

    fun getTermsOfServiceUrl() = VK_TOS

    fun getPrivacyPolicyUrl(): String {
        return when (Locale.getDefault()) {
            Locale("ru") -> PRIVACY_RU
            else -> PRIVACY_WORLD
        }
    }

    fun formatLegalText(context: Context, @StringRes fullTextRes: Int): CharSequence {
        val privacyPolicy = context.getString(R.string.privacy_policy)
        val termsOfService = context.getString(R.string.terms_of_service)
        val fullText = context.getString(fullTextRes, privacyPolicy, termsOfService)

        val privacyPolicyStart = fullText.indexOf(privacyPolicy)
        val privacyPolicyEnd = privacyPolicyStart + privacyPolicy.length

        val termsOfServiceStart = fullText.indexOf(termsOfService)
        val termsOfServiceEnd = termsOfServiceStart + termsOfService.length

        return SpannableString(fullText).apply {
            setSpan(
                    SimpleClickableSpan { onPrivacyPolicyClicked(context) },
                    privacyPolicyStart,
                    privacyPolicyEnd,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            setSpan(
                    SimpleClickableSpan { onTermsOfServiceClicked(context) },
                    termsOfServiceStart,
                    termsOfServiceEnd,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun onPrivacyPolicyClicked(context: Context) {
        BrowsingUtils.openUrl(context, getPrivacyPolicyUrl())
    }

    private fun onTermsOfServiceClicked(context: Context) {
        BrowsingUtils.openUrl(context, getTermsOfServiceUrl())
    }

    private class SimpleClickableSpan(private val onClick: () -> Unit) : ClickableSpan() {
        override fun onClick(widget: View) {
            onClick()
        }
    }

}