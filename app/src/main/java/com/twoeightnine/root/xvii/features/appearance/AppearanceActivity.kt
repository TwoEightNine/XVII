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

package com.twoeightnine.root.xvii.features.appearance

import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.base.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class AppearanceActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = AppearanceFragment.newInstance()

    override fun onBackPressed() {
        val fragment = getFragment() as? AppearanceFragment
        if (fragment != null && fragment.hasChanges()) {
            fragment.askForRestarting()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        fun launch(context: Context?) {
            launchActivity(context, AppearanceActivity::class.java)
        }
    }
}