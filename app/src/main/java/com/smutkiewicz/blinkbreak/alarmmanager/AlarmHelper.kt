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
import com.smutkiewicz.blinkbreak.model.Task
import com.smutkiewicz.blinkbreak.util.BREAK_DURATION_KEY

class AlarmHelper(private val context: Context) {

    private var sp: SharedPreferences? = null

    fun scheduleAlarm(task: Task?) {
        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(context, BlinkBreakReceiver::class.java)

        // Extras, periodic fire time of the break and its duration
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp!!.edit().putLong(BREAK_DURATION_KEY, task!!.breakDuration).apply()

        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = getBroadcast(context, BlinkBreakReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val firstMillis = SystemClock.elapsedRealtime() // alarm is set right away
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                firstMillis + task.breakEvery,
                task.breakEvery, pIntent)

        Log.d(TAG, "Alarm scheduled.")
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

    private companion object {
        const val TAG = "AlarmHelper"
    }
}