package com.twoeightnine.root.xvii.pin.fake.diagnostics

import android.app.Activity
import android.content.Context
import android.os.BatteryManager
import android.util.DisplayMetrics
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import com.twoeightnine.root.xvii.utils.getTotalRAM
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class DiagnosticsViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val displayLiveData = MutableLiveData<String>()
    private val networkLiveData = MutableLiveData<String>()
    private val batteryLiveData = MutableLiveData<String>()
    private val cpuLiveData = MutableLiveData<String>()
    private val ramLiveData = MutableLiveData<String>()

    val display: LiveData<String>
        get() = displayLiveData

    val network: LiveData<String>
        get() = networkLiveData

    val battery: LiveData<String>
        get() = batteryLiveData

    val cpu: LiveData<String>
        get() = cpuLiveData

    val ram: LiveData<String>
        get() = ramLiveData


    fun runAll(activity: Activity) {
        runDisplay(activity)
        runNetwork()
        runBattery(activity)
        runCpu()
        runRam()
    }

    fun runDisplay(activity: Activity) {
        execute(displayLiveData) {
            val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metrics)
            val result = StringBuilder()
            result.line("${metrics.widthPixels}x${metrics.heightPixels}")
                    .line("density = ${metrics.density}")
                    .line("xdpi = ${metrics.xdpi}")
                    .line("ydpi = ${metrics.ydpi}")
            result.toString()
        }
    }

    fun runNetwork() {
        execute(networkLiveData) {
            ""
        }
    }

    fun runBattery(context: Context) {
        execute(batteryLiveData) {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val result = StringBuilder()
            result.line("percentage = ${bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)}")
                    .line("avg current = ${bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)} ma")
                    .line("energy = ${bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)} nw")
            result.toString()
        }
    }

    fun runCpu() {
        execute(cpuLiveData) {
            val result = StringBuilder()
            try {
                val data = listOf("/system/bin/cat", "/proc/cpuinfo")
                val processBuilder = ProcessBuilder(data)
                val process = processBuilder.start()
                val bytes = process.inputStream.use { it.readBytes() }
                result.append(String(bytes))
            } catch (ex: IOException) {
                result.line("unable to fetch cpu info")
                        .line("${ex.message}")
            }
            result.toString()
        }
    }

    fun runRam() {
        execute(ramLiveData) {
            val result = StringBuilder()
            val runtime = Runtime.getRuntime()
            val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
            val maxHeapSizeInMB = runtime.maxMemory() / 1048576L
            val availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB
            result.line("total ram = ${getTotalRAM()}")
                    .line("used = $usedMemInMB mb")
                    .line("max heap size = $maxHeapSizeInMB mb")
                    .line("available heap size = $availHeapSizeInMB mb")
            result.toString()
        }
    }

    fun getStability(): Int {
        return 60 + Session.uid % 30
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    private fun execute(liveData: MutableLiveData<String>, block: () -> String) {
        liveData.value = "Loading.."
        Single.fromCallable(block)
                .flatMap { result -> Single.timer(getDelay(), TimeUnit.MILLISECONDS).map { result } }
                .sub(liveData)
    }

    private fun <T> Single<T>.sub(liveData: MutableLiveData<T>) {
        compose(applySingleSchedulers())
                .subscribe { result ->
                    liveData.value = result
                }
                .let { compositeDisposable.add(it) }
    }

    private fun getDelay() = Random.nextLong(1200L, 3600L)

    private fun StringBuilder.line(s: String) = append("$s\n")

}