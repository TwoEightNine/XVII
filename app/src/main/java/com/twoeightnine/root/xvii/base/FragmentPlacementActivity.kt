package com.twoeightnine.root.xvii.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

class FragmentPlacementActivity : ContentActivity() {

    private val fragmentClassName by lazy {
        intent?.extras?.getString(ARG_FRAGMENT_CLASS)
    }
    private val fragmentArgs by lazy {
        intent?.extras?.getBundle(ARG_FRAGMENT_ARGS) ?: Bundle()
    }

    override fun createFragment(intent: Intent?): Fragment =
            Fragment.instantiate(this, fragmentClassName ?: "", fragmentArgs)

    companion object {

        private const val ARG_FRAGMENT_CLASS = "fragmentClass"
        private const val ARG_FRAGMENT_ARGS = "fragmentArgs"

        fun launch(context: Context?, fragmentClass: Class<*>, fragmentArgs: Bundle?) {
            context?.startActivity(Intent(context, FragmentPlacementActivity::class.java).apply {
                putExtra(ARG_FRAGMENT_CLASS, fragmentClass.name)
                putExtra(ARG_FRAGMENT_ARGS, fragmentArgs)
            })
        }

        inline fun <reified T : Fragment> Context?.startFragment(fragmentArgs: Bundle? = null) {
            launch(this, T::class.java, fragmentArgs)
        }

        inline fun <reified T : Fragment> Fragment.startFragment(fragmentArgs: Bundle? = null) {
            launch(context, T::class.java, fragmentArgs)
        }
    }
}