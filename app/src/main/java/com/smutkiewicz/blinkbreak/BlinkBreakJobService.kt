package com.smutkiewicz.blinkbreak

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.smutkiewicz.blinkbreak.extensions.getBooleanValue
import com.smutkiewicz.blinkbreak.util.*


class BlinkBreakJobService : JobService() {

    private var nm: NotificationManager? = null
    private var activityMessenger: Messenger? = null
    private var userBrightness: Int = 0

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        nm = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        activityMessenger = intent.getParcelableExtra(MESSENGER_INTENT_KEY)
        return Service.START_NOT_STICKY
    }

    override fun onStartJob(params: JobParameters): Boolean {
        sendMessage(MSG_COLOR_START, params.jobId)

        val duration = params.extras.getLong(BREAK_DURATION_KEY)
        val typeOfBreak = params.extras.getInt(BREAK_TYPE_KEY)
        val notifications = params.extras.getBooleanValue(NOTIFICATIONS_KEY)
        val highImportance = params.extras.getBooleanValue(HIGH_IMPORTANCE_KEY)
        val lowerBrightness = params.extras.getBooleanValue(LOWER_BRIGHTNESS_KEY)
        userBrightness = getScreenBrightness()

        showNotification(typeOfBreak)
        setScreenBrightness(0)

        Handler().postDelayed({
            Log.i(TAG, "on finish job: ${params.jobId}")
            sendMessage(MSG_COLOR_STOP, params.jobId)
            setScreenBrightness(userBrightness)
            jobFinished(params, true)
        }, duration)

        Log.i(TAG, "on start job: ${params.jobId}")

        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        sendMessage(MSG_COLOR_STOP, params.jobId)
        Log.i(TAG, "on stop job: ${params.jobId}")

        return false
    }

    private fun getScreenBrightness()
            = Settings.System.getInt(applicationContext.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS, 0)

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
            Log.d(TAG, "Service is bound, not started. There's no callback to send a message to.")
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

    private fun showNotification(typeOfBreak: Int) {
        val contentText = getString(R.string.have_a_break_ticker)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0)

        val builder = NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                .setWhen(System.currentTimeMillis())
                .setContentText(contentText)
                .setContentIntent(contentIntent)

        when (typeOfBreak) {
            BREAK_TYPE_TINY -> {
                val breakText = getString(R.string.tiny_break)
                val title = getString(R.string.have_a_break_message, breakText)
                builder.setSmallIcon(R.drawable.ic_have_a_short_break_24dp)
                        .setContentTitle(title)
            }
            BREAK_TYPE_BIG -> {
                val breakText = getString(R.string.big_break)
                val title = getString(R.string.have_a_break_message, breakText)
                builder.setSmallIcon(R.drawable.ic_have_a_big_break_24dp)
                        .setContentTitle(title)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setNotificationChannel()
            builder.setChannelId(SERVICE_CHANNEL_ID)
        }

        val notification = builder.build()
        nm!!.notify(R.string.have_a_break_message, notification)
    }

    @TargetApi(26)
    private fun setNotificationChannel() {
        val notificationChannel = NotificationChannel(SERVICE_CHANNEL_ID,
                getString(R.string.service_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.lightColor = Color.RED
        nm!!.createNotificationChannel(notificationChannel)
    }

    companion object {
        private val TAG = "MyJobService"
        private val SERVICE_CHANNEL_ID = "blink_break_channel_id"
    }
}