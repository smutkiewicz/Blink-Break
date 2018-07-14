package com.smutkiewicz.blinkbreak.util
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import org.joda.time.*
import java.text.SimpleDateFormat
import java.util.*

class StatsHelper(internal var context: Context) {
    internal var pref: SharedPreferences
    internal var editor: SharedPreferences.Editor
    internal var spName: String

    init {
        spName = context.packageName + "_preferences"
        pref = context.getSharedPreferences(spName, MODE_PRIVATE)
        editor = pref.edit()
    }

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
            val format = SimpleDateFormat(DATE_FORMAT)
            val currentDate = getTimeStamp()

            val d1 = format.parse(lastBreak)
            val d2 = format.parse(currentDate)

            val dt1 = DateTime(d1)
            val dt2 = DateTime(d2)

            val seconds = Seconds.secondsBetween(dt1, dt2).seconds % 60
            val minutes = Minutes.minutesBetween(dt1, dt2).minutes % 60
            val hours = Hours.hoursBetween(dt1, dt2).hours % 24
            val days = Days.daysBetween(dt1, dt2).days

            return when {
                days > 0 -> days.toString() + " day(s) ago."
                hours > 0 -> hours.toString() + " hour(s) ago."
                minutes > 0 -> minutes.toString() + " min(s) ago."
                else -> seconds.toString() + " second(s) ago."
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
        val DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"

        fun getTimeStamp() =
                SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().time)!!

    }

}