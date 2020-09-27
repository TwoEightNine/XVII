package com.twoeightnine.root.xvii.pin

import com.twoeightnine.root.xvii.crypto.sha256
import kotlin.math.abs

object PinUtils {

    private const val SALT = "oi|6yw4-c5g846-d5c53s9mx"

    private val monthsWith31Days = listOf(1, 3, 5, 7, 8, 10, 12)

    enum class PinWeakness {
        NONE,
        LENGTH,
        PATTERN,
        YEAR,
        DATE
    }

    fun getPinWeakness(rawPin: String): PinWeakness = when {
        rawPin.length < 4 -> PinWeakness.LENGTH
        hasPattern(rawPin) -> PinWeakness.PATTERN
        isValidDate(rawPin) -> PinWeakness.DATE
        else -> PinWeakness.NONE
    }

    fun isPinCorrect(
            pin: String,
            correctPinHash: String,
            mixtureType: SecurityFragment.MixtureType,
            minutes: String,
            battery: String
    ): Boolean {

        val mixtureMatches = when (mixtureType) {

            SecurityFragment.MixtureType.MINUTES_START -> {
                pin.startsWith(minutes)
            }

            SecurityFragment.MixtureType.MINUTES_END -> {
                pin.endsWith(minutes)
            }

            SecurityFragment.MixtureType.BATTERY_START -> {
                pin.startsWith(battery)
            }

            SecurityFragment.MixtureType.BATTERY_END -> {
                pin.endsWith(battery)
            }

            SecurityFragment.MixtureType.NONE -> true
        }
        if (!mixtureMatches) return false

        val cleanPin = when (mixtureType) {
            SecurityFragment.MixtureType.MINUTES_START ->
                pin.substring(minutes.length)

            SecurityFragment.MixtureType.MINUTES_END ->
                pin.take(pin.length - minutes.length)

            SecurityFragment.MixtureType.BATTERY_START ->
                pin.substring(battery.length)

            SecurityFragment.MixtureType.BATTERY_END ->
                pin.take(pin.length - battery.length)

            SecurityFragment.MixtureType.NONE -> pin
        }
        return correctPinHash == getPinHash(cleanPin)
    }

    fun getPinHash(pin: String): String = sha256("$pin$SALT")

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
            diffs.add(variants.minOrNull() ?: 0)
        }
        return diffs
    }

    private fun hasPattern(rawPin: String): Boolean {
        val pin = rawPin.map { it.toString().toInt() }
        val pinDiff = getPinDiff(pin)
        val pinDiff2 = getPinDiff(pinDiff)
        val zerosCount = pinDiff.count { it == 0 }
        val zerosCount2 = pinDiff2.count { it == 0 }
        val diffSame2 = pinDiff2.all { it == pinDiff2[0] }

        return zerosCount2 == pinDiff2.size
                || zerosCount != 0 && (zerosCount2 != 0 || diffSame2)
    }

    private fun isPopularYear(rawPin: String): Boolean =
            rawPin.length == 4 && rawPin.toInt() in 1900..2020

    private fun isValidDate(rawPin: String): Boolean = when(rawPin.length) {
        4 -> {
            isMonthAndDay(rawPin) || isPopularYear(rawPin)
        }
        6 -> {
            val firstFour = rawPin.substring(0, 4)
            val lastFour = rawPin.substring(2)
            val firstTwo = rawPin.substring(0, 2)
            val lastTwo = rawPin.substring(4)

            val hasYearFirst = isPopularYear("19$firstTwo") || isPopularYear("20$firstTwo")
            val hasYearLast = isPopularYear("19$lastTwo") || isPopularYear("20$lastTwo")

            isMonthAndDay(firstFour) && hasYearLast
                    || isMonthAndDay(lastFour) && hasYearFirst
        }
        8 -> {
            val firstHalf = rawPin.substring(0, 4)
            val secondHalf = rawPin.substring(4)

            isMonthAndDay(firstHalf) && isPopularYear(secondHalf)
                    || isMonthAndDay(secondHalf) && isPopularYear(firstHalf)
        }
        else -> false

    }

    private fun isMonthAndDay(rawPin: String): Boolean {
        if (rawPin.length != 4) return false

        val firstPair = rawPin.substring(0, 2).toInt()
        val secondPair = rawPin.substring(2).toInt()

        return isMonth(secondPair) && isDayOfMonth(firstPair, secondPair)
                || isMonth(firstPair) && isDayOfMonth(secondPair, firstPair)
    }

    private fun isDayOfMonth(num: Int, month: Int): Boolean {
        return (num in 1..29
                || num == 30 && month != 2
                ||num == 31 && month in monthsWith31Days)
    }

    private fun isMonth(num: Int) = num in 1..12
}