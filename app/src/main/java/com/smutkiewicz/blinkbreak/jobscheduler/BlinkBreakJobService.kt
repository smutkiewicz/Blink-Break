package com.smutkiewicz.blinkbreak.jobscheduler

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.smutkiewicz.blinkbreak.MainActivity
import com.smutkiewicz.blinkbreak.R
import com.smutkiewicz.blinkbreak.extensions.getBooleanValue
import com.smutkiewicz.blinkbreak.extensions.setNotificationChannel
import com.smutkiewicz.blinkbreak.util.*

class BlinkBreakJobService : JobService() {

    private var activityMessenger: Messenger? = null
    private var lowerBrightnessActivated = true
    private var userBrightness: Int = 0

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        activityMessenger = intent.getParcelableExtra(MESSENGER_INTENT_KEY)
        return Service.START_NOT_STICKY
    }

    override fun onStartJob(params: JobParameters): Boolean {
        Log.i(TAG, "on start job: ${params.jobId}")

        // save current brightness to switch it in the future
        saveUserScreenBrightness()

        // get user's job parameters
        val duration = params.extras.getLong(BREAK_DURATION_KEY)
        val notifications = params.extras.getBooleanValue(NOTIFICATIONS_KEY)
        lowerBrightnessActivated = params.extras.getBooleanValue(LOWER_BRIGHTNESS_KEY)

        when {
            notifications -> showNotification()
        }

        when {
            lowerBrightnessActivated -> setScreenBrightness(0)
        }

        Handler().postDelayed({
            if (lowerBrightnessActivated) {
                userBrightness = getUserScreenBrightness()
                setScreenBrightness(userBrightness)
            }

            cancelNotification()

            Log.i(TAG, "on finish job: ${params.jobId}")
            jobFinished(params, true)
        }, duration)

        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.i(TAG, "on stop job: ${params.jobId}")

        if (lowerBrightnessActivated) {
            userBrightness = getUserScreenBrightness()
            setScreenBrightness(userBrightness)
        }

        return true
    }

    private fun saveUserScreenBrightness() {
        val brightness = Settings.System.getInt(applicationContext.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, 0)
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        sp.edit().putInt(PREF_USER_BRIGHTNESS, brightness).apply()
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

    private fun sendMessage(messageID: Int, params: Any?) {
        if (activityMessenger == null) {
            Log.d(TAG, "Service is bound, not started.")
            return
        }
        val message = Message.obtain()
        message.run {
            what = messageID
            obj = params
        }
        try {
            activityMessenger?.send(message)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error passing service object back to activity.")
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

        val builder = NotificationCompat.Builder(this, SERVICE_SINGLE_JOB_CHANNEL_ID)
                .setWhen(System.currentTimeMillis())
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_have_a_break_24dp)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setColor(Color.MAGENTA)
                .setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.setNotificationChannel(SERVICE_SINGLE_JOB_CHANNEL_ID,
                    getString(R.string.service_task_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW)
            builder.setChannelId(SERVICE_SINGLE_JOB_CHANNEL_ID)
        }

        val notification = builder.build()
        nm.notify(R.string.have_a_break_message, notification)
    }

    private fun cancelNotification() {
        val nm = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(R.string.have_a_break_message)
    }

    companion object {
        private val TAG = "MyJobService"
        private val SERVICE_SINGLE_JOB_CHANNEL_ID = "blink_break_single_job_channel_id"
    }
}