package com.smutkiewicz.blinkbreak.alarmmanager

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.getBroadcast
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import com.smutkiewicz.blinkbreak.R
import com.smutkiewicz.blinkbreak.model.Task
import com.smutkiewicz.blinkbreak.util.BREAK_DURATION_KEY
import com.smutkiewicz.blinkbreak.util.PREF_BREAK_DURATION_PROGRESS
import com.smutkiewicz.blinkbreak.util.PREF_BREAK_EVERY_PROGRESS
import com.smutkiewicz.blinkbreak.util.PREF_POSTPONE_DURATION

class AlarmHelper(private val context: Context) {

    private var sp: SharedPreferences? = null

    fun scheduleAlarm() {
        // Constructs an intent that will execute the AlarmReceiver
        val intent = Intent(context, BlinkBreakReceiver::class.java)
        val task = getAlarmTaskSettings()

        sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp!!.edit().putLong(BREAK_DURATION_KEY, task!!.breakDuration).apply()

        // Creates a PendingIntent to be triggered when the alarm goes off
        val pIntent = getBroadcast(context, BlinkBreakReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val firstMillis = SystemClock.elapsedRealtime() // alarm is set right away
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                firstMillis + task.breakEvery,
                task.breakEvery, pIntent)

        Log.d(TAG, "Alarm scheduled.")
    }

    fun schedulePostponedAlarm() {
        // we have to cancel current alarm before making the postponed one
        cancelAlarm()

        val intent = Intent(context, BlinkBreakReceiver::class.java)
        val task = getAlarmTaskSettings()

        sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp!!.edit().putLong(BREAK_DURATION_KEY, task!!.breakDuration).apply()

        val pIntent = getBroadcast(context, BlinkBreakReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val firstMillis = SystemClock.elapsedRealtime()
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val postponeDuration = Integer.valueOf(sp!!.getString(PREF_POSTPONE_DURATION, DEFAULT_POSTPONE_VAL))

        // alarm will be trigerred after postpone duration
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                firstMillis + postponeDuration,
                task.breakEvery, pIntent)

        Log.d(TAG, "Alarm postponed.")
    }

    fun cancelAlarm() {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BlinkBreakReceiver::class.java)
        val pIntent = getBroadcast(context, BlinkBreakReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarm.cancel(pIntent)
        pIntent.cancel()

        Log.d(TAG, "Alarm cancelled.")
    }

    fun checkIfThereArePendingTasks(): Boolean =
            PendingIntent.getBroadcast(context, BlinkBreakReceiver.REQUEST_CODE,
                Intent(context, BlinkBreakReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE) != null

    private fun getAlarmTaskSettings(): Task? {
        sp = PreferenceManager.getDefaultSharedPreferences(context)

        val breakEveryProgress = sp!!.getInt(PREF_BREAK_EVERY_PROGRESS, 0)
        val breakEvery = getProgressValue(R.array.break_frequency_val_array, breakEveryProgress)

        val breakDurationProgress = sp!!.getInt(PREF_BREAK_DURATION_PROGRESS, 0)
        val breakDuration = getProgressValue(R.array.break_duration_val_array, breakDurationProgress)

        return Task(breakEvery, breakDuration)
    }

    // Values of time are stored in sp in steps of SeekBar,
    // so we need to map them to their real values.
    private fun getProgressValue(resId: Int, progress: Int): Long {
        val intArray = context.resources.getIntArray(resId)
        return intArray[progress].toLong()
    }

    companion object {
        const val DEFAULT_POSTPONE_VAL = "1200000"
        const val TAG = "AlarmHelper"
    }
}