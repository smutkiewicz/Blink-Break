package com.smutkiewicz.blinkbreak.alarmmanager

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.support.v4.content.WakefulBroadcastReceiver
import android.util.Log
import com.smutkiewicz.blinkbreak.MainActivity
import com.smutkiewicz.blinkbreak.R
import com.smutkiewicz.blinkbreak.extensions.setNotificationChannel
import com.smutkiewicz.blinkbreak.util.BREAK_DURATION_KEY
import com.smutkiewicz.blinkbreak.util.LOWER_BRIGHTNESS_KEY
import com.smutkiewicz.blinkbreak.util.NOTIFICATIONS_KEY
import com.smutkiewicz.blinkbreak.util.PREF_USER_BRIGHTNESS
import java.util.*

class BlinkBreakAlarmService : IntentService("BlinkBreakAlarmService") {

    private var lowerBrightnessActivated = true
    private var userBrightness: Int = 0

    // the Service onStart callback
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, getServiceActiveNotification())
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        WakefulBroadcastReceiver.completeWakefulIntent(intent)

        Log.i("BlinkBreakAlarmService", "Service running")
        Log.i(TAG, "on start task")

        // save current brightness to switch it in the future
        saveUserScreenBrightness()

        // get user's job parameters
        val duration = intent!!.getLongExtra(BREAK_DURATION_KEY, 5000)
        val notifications = intent.getBooleanExtra(NOTIFICATIONS_KEY, true)
        lowerBrightnessActivated = intent.getBooleanExtra(LOWER_BRIGHTNESS_KEY, true)

        when {
            notifications -> showNotification()
        }

        when {
            lowerBrightnessActivated -> setScreenBrightness(0)
        }

        Timer().schedule(object : TimerTask() {
            override fun run() {

                if (lowerBrightnessActivated) {
                    userBrightness = getUserScreenBrightness()
                    setScreenBrightness(userBrightness)
                }

                cancelNotification()
            }
        }, duration)
    }

    private fun saveUserScreenBrightness() {
        val brightness = Settings.System.getInt(applicationContext.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, 0)

        if (brightness != 0) {
            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            sp.edit().putInt(PREF_USER_BRIGHTNESS, brightness).apply()
        }
    }

    private fun getUserScreenBrightness(): Int {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        return sp.getInt(PREF_USER_BRIGHTNESS, 10)
    }

    private fun setScreenBrightness(brightnessValue: Int) {
        if (brightnessValue in 0..255) {
            Settings.System.putInt(
                    applicationContext.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
            )
        }
    }

    private fun showNotification() {
        val nm = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        val breakText = getString(R.string.break_options)
        val title = getString(R.string.have_a_break_message, breakText)
        val contentText = getString(R.string.have_a_break_ticker)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0)

        val builder = NotificationCompat.Builder(this, SERVICE_SINGLE_TASK_CHANNEL_ID)
                .setWhen(System.currentTimeMillis())
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_have_a_break_24dp)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setColor(Color.MAGENTA)
                .setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.setNotificationChannel(SERVICE_SINGLE_TASK_CHANNEL_ID,
                    getString(R.string.service_task_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW)
            builder.setChannelId(SERVICE_SINGLE_TASK_CHANNEL_ID)
        }

        val notification = builder.build()
        nm.notify(R.string.have_a_break_message, notification)
    }

    private fun getServiceActiveNotification(): Notification? {
        val nm = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        val title = getString(R.string.service_is_active)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0)

        val builder = NotificationCompat.Builder(this, MainActivity.SERVICE_CHANNEL_ID)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_eye_black)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setColor(Color.MAGENTA)
                .setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.setNotificationChannel(MainActivity.SERVICE_CHANNEL_ID,
                    getString(R.string.service_notification_channel_name),
                    NotificationManager.IMPORTANCE_MIN)
            builder.setChannelId(MainActivity.SERVICE_CHANNEL_ID)
        }

        return builder.build()
        //nm.notify(R.string.service_is_active, notification)
    }

    private fun cancelServiceActiveNotification() {
        val nm = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(R.string.service_is_active)
    }

    private fun cancelNotification() {
        val nm = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(R.string.have_a_break_message)
    }

    companion object {
        private val TAG = "BlinkBreakAlarmService"
        private val SERVICE_SINGLE_TASK_CHANNEL_ID = "blink_break_single_task_channel_id"
    }
}