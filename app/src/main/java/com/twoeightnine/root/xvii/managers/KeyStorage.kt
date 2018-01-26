package com.twoeightnine.root.xvii.managers

import android.content.Context
import android.content.SharedPreferences
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.utils.time

/**
 * Created by fuckyou on 12.12.2017.
 */

object KeyStorage {

    private val NAME = "keyStorage"

    private val PRIME = "prime"             // p
    private val HALF_PRIME = "halfPrime"    // q
    private val TS = "timeStamp"

    private val DEFAULT_PRIME = "429960845873088536599738146849398890197656281978746052260302" +
            "647466290912363305996665498753182126120318295110244403964643426486915779918338905631403" +
            "871028184935255084712703423613529366297760543627384218281702559557613931230981025214701" +
            "436292843374882547050410898749274017561380961864641986822934974094546625703373934105581" +
            "804151000069136171694933799628596746360744089987859632744266134003621159571422862980144" +
            "043377717793249604329463406248840895370685965329721512935512704815510998868368597285047" +
            "022910731679048010756286396108128504010975404344672924191827643103234869173733652744217" +
            "51734483875743936086632531541480503"

    private val DEFAULT_HALF_PRIME = "2149804229365442682998690734246994450988281409893730261301513237" +
            "331454561816529983327493765910630601591475551222019823217132434578899591694528157019355" +
            "140924676275423563517118067646831488802718136921091408512797788069656154905126073507181" +
            "464216874412735252054493746370087806904809323209934114674870472733128516869670527909020" +
            "755000345680858474668998142983731803720449939298163721330670018105797857114314900720216" +
            "888588966248021647317031244204476853429826648607564677563524077554994341842986425235114" +
            "553658395240053781431980540642520054877021723364620959138215516174345868668263721087586" +
            "7241937871968043316265770740251"

    private val STORAGE_DURATION = 3600 * 24 * 30


    private val pref: SharedPreferences by lazy {
        App.context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    var prime
        get() = pref.getString(PRIME, DEFAULT_PRIME)
        set(value) = pref.edit().putString(PRIME, value).apply()

    var halfPrime
        get() = pref.getString(HALF_PRIME, DEFAULT_HALF_PRIME)
        set(value) = pref.edit().putString(HALF_PRIME, value).apply()

    var ts
        get() = pref.getInt(TS, 0)
        set(value) = pref.edit().putInt(TS, value).apply()

    fun saveCustomKey(peerId: Int, key: String) {
        pref.edit().putString(getPeerKey(peerId), key).apply()
    }

    fun removeCustomKey(peerId: Int) {
        pref.edit().remove(getPeerKey(peerId)).apply()
    }

    fun getCustomKey(peerId: Int) = pref.getString(getPeerKey(peerId), null)

    fun isObsolete() = time() - ts > STORAGE_DURATION

    fun isDefault() = prime == DEFAULT_PRIME

    private fun getPeerKey(peerId: Int) = "peer$peerId"

}