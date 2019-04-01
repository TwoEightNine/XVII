package com.twoeightnine.root.xvii.crypto.prime

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService

/**
 * Created by fuckyou on 12.12.2017.
 * generates safe prime for DH-2048
 */

class PrimeGeneratorJobIntentService : JobIntentService() {

    private val core by lazy { PrimeGeneratorCore() }

    override fun onHandleWork(p0: Intent) {
        core.run()
    }

    companion object {

        private const val JOB_ID = 524

        fun launch(context: Context) {
            enqueueWork(context, PrimeGeneratorJobIntentService::class.java,
                    JOB_ID, Intent(context, PrimeGeneratorJobIntentService::class.java))
        }
    }
}