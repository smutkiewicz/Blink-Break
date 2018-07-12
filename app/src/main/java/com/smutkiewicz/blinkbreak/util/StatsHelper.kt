package com.smutkiewicz.blinkbreak.util
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class StatsHelper(internal var context: Context) {
    internal var pref: SharedPreferences
    internal var editor: SharedPreferences.Editor
    internal var spName: String

    var unskippedBreaks: Int
        get() = pref.getInt(STAT_UNSKIPPED_BREAKS, 0)
        set(value) {
            editor.putInt(STAT_UNSKIPPED_BREAKS, value)
            editor.commit()
        }

    var skippedBreaks: Int
        get() = pref.getInt(STAT_SKIPPED_BREAKS, 0)
        set(value) {
            editor.putInt(STAT_SKIPPED_BREAKS, value)
            editor.commit()
        }

    init {
        spName = context.packageName + "_preferences"
        pref = context.getSharedPreferences(spName, MODE_PRIVATE)
        editor = pref.edit()
    }

    fun increaseValue(prefName: String) {
        when(prefName) {
            STAT_UNSKIPPED_BREAKS -> {
                val newValue = unskippedBreaks + 1
                unskippedBreaks = newValue
            }
            STAT_SKIPPED_BREAKS -> {
                val newValue = skippedBreaks + 1
                skippedBreaks = newValue
            }
            else -> { //do nothing
            }
        }
    }

    companion object {
        val STAT_UNSKIPPED_BREAKS = "stat_unskipped_breaks"
        val STAT_SKIPPED_BREAKS = "stat_skipped_breaks"
        val STAT_LAST_BREAK = "stat_last_break"
        val STAT_MAXIMAL_BREAK_DURATION = "stat_max_break_duration"
        val STAT_UNSKIPPED_BREAKS_DURATION = "stat_unskipped_break_duration"
    }

}