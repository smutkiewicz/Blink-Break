package com.smutkiewicz.blinkbreak.util
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import org.joda.time.Seconds
import java.text.SimpleDateFormat
import java.util.*

class StatsHelper(internal var context: Context) {

    private var pref: SharedPreferences
    private var editor: SharedPreferences.Editor
    private var spName: String

    init {
        spName = context.packageName + "_preferences"
        pref = context.getSharedPreferences(spName, MODE_PRIVATE)
        editor = pref.edit()
    }

    var unskippedBreaks: Int
        get() = pref.getInt(STAT_UNSKIPPED_BREAKS, 0)
        set(value) {
            editor.putInt(STAT_UNSKIPPED_BREAKS, value).commit()
        }

    var unskippedInARow: Int
        get() = pref.getInt(STAT_UNSKIPPED_IN_A_ROW, 0)
        set(value) {
            editor.putInt(STAT_UNSKIPPED_IN_A_ROW, value).commit()
        }

    var skippedBreaks: Int
        get() = pref.getInt(STAT_SKIPPED_BREAKS, 0)
        set(value) {
            editor.putInt(STAT_SKIPPED_BREAKS, value).commit()
        }

    var lastBreak: String
        get() = pref.getString(STAT_LAST_BREAK, "Never")
        set(value) {
            editor.putString(STAT_LAST_BREAK, value).commit()
        }

    fun increaseValue(prefName: String) {
        when(prefName) {
            STAT_UNSKIPPED_BREAKS -> unskippedBreaks += 1
            STAT_SKIPPED_BREAKS -> skippedBreaks += 1
            STAT_UNSKIPPED_IN_A_ROW -> unskippedInARow += 1
            else -> {
                //do nothing
            }
        }
    }

    fun resetValues() {
        editor.apply {
            putString(STAT_LAST_BREAK, "Never").apply()
            putInt(STAT_UNSKIPPED_BREAKS, 0).apply()
            putInt(STAT_SKIPPED_BREAKS, 0).apply()
            putInt(STAT_UNSKIPPED_IN_A_ROW, 0).apply()
        }
    }

    fun getTimeDifferenceString(): String {
        return if (lastBreak != STAT_NEVER) {
            val currentDate = getTimeStamp()
            calculateTimeDifferenceString(lastBreak, currentDate)
        } else {
            STAT_NEVER
        }
    }

    fun calculateTimeDifferenceString(date1: String, date2: String): String {
        val format = SimpleDateFormat(DATE_FORMAT)

        val d1 = format.parse(date1)
        val d2 = format.parse(date2)

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
    }

    companion object {
        const val STAT_UNSKIPPED_BREAKS = "stat_unskipped_breaks"
        const val STAT_SKIPPED_BREAKS = "stat_skipped_breaks"
        const val STAT_LAST_BREAK = "stat_last_break"
        const val STAT_UNSKIPPED_IN_A_ROW = "stat_unskipped_in_a_row"
        const val STAT_NEVER = "Never"
        const val DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"

        fun getTimeStamp() = SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().time)!!
    }

}