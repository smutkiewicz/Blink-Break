package com.smutkiewicz.blinkbreak

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.widget.Toast


class BlinkBreakService : Service() {

    companion object {
        val PREF_SERVICE_ACTIVATED = "service_activated"
        private val NOTIFICATION = R.string.local_service_started
    }

    private val localBinder = LocalBinder()
    private var nm: NotificationManager? = null
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    inner class LocalBinder : Binder() {
        internal val service: BlinkBreakService
            get() = this@BlinkBreakService
    }

    private var myRunnable: Runnable = Runnable {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(1000)
                Log.d("BLINK", "Slept for 1000 millis")
            }
        } catch (e: InterruptedException) {
            Log.d("BLINK", "Thread interrupted")
        } finally {

        }
    }

    override fun onCreate() {
        nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        showNotification()

        handlerThread = HandlerThread("LocalServiceThread")
        handlerThread!!.start()

        handler = Handler(handlerThread!!.looper)
        postRunnable(myRunnable)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("BlinkBreakService", "Received start id $startId: $intent")
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.i("BlinkBreakService", "Received destroy")
        nm!!.cancel(NOTIFICATION)
        handlerThread!!.interrupt()
        handlerThread!!.quit()
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent): IBinder {
        return localBinder
    }

    private fun showNotification() {
        val text = getText(R.string.local_service_started)
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

        nm!!.notify(NOTIFICATION, notification)
    }

    fun postRunnable(runnable: Runnable) {
        handler!!.post(runnable)
    }
}
