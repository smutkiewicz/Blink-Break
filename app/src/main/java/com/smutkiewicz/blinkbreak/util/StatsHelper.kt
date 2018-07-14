package com.smutkiewicz.blinkbreak.util
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*

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

    var lastBreak: String
        get() = pref.getString(STAT_LAST_BREAK, "Never")
        set(value) {
            editor.putString(STAT_LAST_BREAK, value)
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

    fun calculateTimeDifference(): String {
        if (lastBreak != STAT_NEVER) {
            val currentTime = getTimeStamp()
            val format = SimpleDateFormat("HH:mm:ss")
            val date1 = format.parse(lastBreak)
            val date2 = format.parse(currentTime)
            val difference = abs(date2.time - date1.time)

            var seconds = (difference / 1000)
            val minutes = (seconds / 60)
            val hours = (minutes / 60)

            seconds %= 60

            return when {
                hours > 0 -> String.format("%d", seconds) + " hour(s) ago."
                minutes > 0 -> String.format("%d", minutes) + " min(s) ago."
                else -> String.format("%d", seconds) + " second(s) ago."
            }
        } else {
            return STAT_NEVER
        }
    }

    companion object {
        val STAT_UNSKIPPED_BREAKS = "stat_unskipped_breaks"
        val STAT_SKIPPED_BREAKS = "stat_skipped_breaks"
        val STAT_LAST_BREAK = "stat_last_break"
        val STAT_MAXIMAL_BREAK_DURATION = "stat_max_break_duration"
        val STAT_UNSKIPPED_BREAKS_DURATION = "stat_unskipped_break_duration"
        val STAT_NEVER = "Never"

        fun getTimeStamp() =
                SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().time)

    }

}