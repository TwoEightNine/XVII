package com.twoeightnine.root.xvii.background.prime

import android.app.Service
import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.managers.KeyStorage
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.utils.applySchedulers
import com.twoeightnine.root.xvii.utils.getTime
import com.twoeightnine.root.xvii.utils.time
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

/**
 * Created by fuckyou on 12.12.2017.
 * generates safe prime for DH-2048
 */

class PrimeGeneratorService : Service() {

    private val core by lazy { PrimeGeneratorCore() }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        core.run()
        return START_STICKY
    }

    companion object {
        fun launch(context: Context) {
            context.startService(Intent(context, PrimeGeneratorService::class.java))
        }
    }
}