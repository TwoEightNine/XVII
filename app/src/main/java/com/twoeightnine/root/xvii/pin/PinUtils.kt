package com.twoeightnine.root.xvii.pin

import com.twoeightnine.root.xvii.crypto.sha256
import kotlin.math.abs

object PinUtils {

    private const val SALT = "oi|6yw4-c5g846-d5c53s9mx"

    private fun getPinDiff(pin: List<Int>): List<Int> {
        val diffs = arrayListOf<Int>()
        for (i in 1 until pin.size) {
            val variants = arrayListOf<Int>()
            variants.add(abs(pin[i] - pin[i - 1]))
            if (pin[i] == 0) {
                variants.add(abs(10 - pin[i - 1]))
            }
            if (pin[i - 1] == 0) {
                variants.add(abs(10 - pin[i]))
            }
            diffs.add(variants.min() ?: 0)
        }
        return diffs
    }

    private fun hasPattern(rawPin: String): Boolean {
        val pin = rawPin.map { it.toString().toInt() }
        val pinDiff = getPinDiff(pin)
        val pinDiff2 = getPinDiff(pinDiff)
        val zerosCount = pinDiff.count { it == 0 }

        return (pinDiff2.sum().toFloat() / pinDiff2.size) >= 1f && zerosCount == 0
    }

    fun isPinSecure(rawPin: String): Boolean {
        return hasPattern(rawPin)
    }

    fun isPinCorrect(
            pin: String,
            correctPinHash: String,
            mixtureType: PinSettingsFragment.MixtureType,
            minutes: String,
            battery: String
    ): Boolean {

        val mixtureMatches = when (mixtureType) {

            PinSettingsFragment.MixtureType.MINUTES_START -> {
                pin.startsWith(minutes)
            }

            PinSettingsFragment.MixtureType.MINUTES_END -> {
                pin.endsWith(minutes)
            }

            PinSettingsFragment.MixtureType.BATTERY_START -> {
                pin.startsWith(battery)
            }

            PinSettingsFragment.MixtureType.BATTERY_END -> {
                pin.endsWith(battery)
            }

            PinSettingsFragment.MixtureType.NONE -> true
        }
        if (!mixtureMatches) return false

        val cleanPin = when (mixtureType) {
            PinSettingsFragment.MixtureType.MINUTES_START ->
                pin.substring(minutes.length)

            PinSettingsFragment.MixtureType.MINUTES_END ->
                pin.take(pin.length - minutes.length)

            PinSettingsFragment.MixtureType.BATTERY_START ->
                pin.substring(battery.length)

            PinSettingsFragment.MixtureType.BATTERY_END ->
                pin.take(pin.length - battery.length)

            PinSettingsFragment.MixtureType.NONE -> pin
        }
        return correctPinHash == getPinHash(cleanPin)
    }

    fun getPinHash(pin: String): String = sha256("$pin$SALT")
}