package com.smutkiewicz.blinkbreak

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.provider.Settings
import android.util.Log
import com.smutkiewicz.blinkbreak.util.BREAK_EVERY_KEY
import com.smutkiewicz.blinkbreak.util.MESSENGER_INTENT_KEY
import com.smutkiewicz.blinkbreak.util.MSG_COLOR_START
import com.smutkiewicz.blinkbreak.util.MSG_COLOR_STOP

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

        val duration = params.extras.getLong(BREAK_EVERY_KEY)
        userBrightness = getScreenBrightness()

        Handler().postDelayed({
            Log.i(TAG, "on finish job: ${params.jobId}")
            sendMessage(MSG_COLOR_STOP, params.jobId)
            setScreenBrightness(userBrightness)
            jobFinished(params, true)
        }, duration)

        Log.i(TAG, "on start job: ${params.jobId}")
        setScreenBrightness(0)

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

    private fun showNotification() {
        TODO()
        val text = getText(R.string.service_activated)
        val contentIntent = PendingIntent.getActivity(this, 0,
                Intent(this, MainActivity::class.java), 0)
        val notification = Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_eye_black)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.blink_break_service_label))
                .setContentText(text)
                .setContentIntent(contentIntent)
                .build()
        //nm!!.notify(BlinkBreakService.NOTIFICATION, notification)
    }

    companion object {
        private val TAG = "MyJobService"
    }
}