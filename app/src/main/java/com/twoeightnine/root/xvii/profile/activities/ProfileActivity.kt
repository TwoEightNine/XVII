package com.twoeightnine.root.xvii.profile.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.BaseActivity
import com.twoeightnine.root.xvii.profile.fragments.ProfileFragment

class ProfileActivity : BaseActivity() {

    companion object {
        fun launch(context: Context?, userId: Int) {
            context ?: return

            val intent = Intent(context, ProfileActivity::class.java).apply {
                putExtra(ProfileFragment.ARG_USER_ID, userId)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)
        savedInstanceState ?: supportFragmentManager
                .beginTransaction()
                .replace(R.id.flContainer, ProfileFragment.newInstance(getUserId()))
                .commitAllowingStateLoss()
    }

    private fun getUserId(): Int {
        if (intent.action == Intent.ACTION_VIEW) {
            intent?.data?.lastPathSegment?.also { path ->
                return try {
                    Integer.parseInt(path.replace("id", ""))
                } catch (e: NumberFormatException) {
                    0
                }
            }
            return 0
        } else {
            return intent?.extras?.getInt(ProfileFragment.ARG_USER_ID) ?: 0
        }
    }
}