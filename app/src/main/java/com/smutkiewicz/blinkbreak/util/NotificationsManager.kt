package com.smutkiewicz.blinkbreak.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.smutkiewicz.blinkbreak.MainActivity
import com.smutkiewicz.blinkbreak.R
import com.smutkiewicz.blinkbreak.extensions.setNotificationChannel


object NotificationsManager {

    private const val TAG = "NotificationsManager"
    private const val SERVICE_CHANNEL_ID = "blink_break_channel_id"
    private const val SERVICE_SINGLE_TASK_CHANNEL_ID = "blink_break_single_task_channel_id"

    fun showServiceActiveNotification(context: Context) {
        val nm = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        val title = context.getString(R.string.service_is_active)

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val contentIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder = NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .apply {
                setWhen(System.currentTimeMillis())
                setContentIntent(contentIntent)
                setContentTitle(title)
                setOngoing(true)
                setSmallIcon(R.drawable.ic_eye_black)
                priority = NotificationCompat.PRIORITY_MIN
                color = Color.MAGENTA
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.setNotificationChannel(
                SERVICE_CHANNEL_ID,
                context.getString(R.string.service_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            builder.setChannelId(SERVICE_CHANNEL_ID)
        }

        val notification = builder.build()
        nm.notify(R.string.service_is_active, notification)
    }

    fun getServiceActiveNotification(context: Context): Notification? {
        val nm = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        val title = context.getString(R.string.service_is_active)

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val contentIntent = PendingIntent.getActivity(context, 0,
                intent, 0)

        val builder = NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .apply {
                setWhen(System.currentTimeMillis())
                setContentIntent(contentIntent)
                setContentTitle(title)
                setOngoing(true)
                setSmallIcon(R.drawable.ic_eye_black)
                priority = NotificationCompat.PRIORITY_MIN
                color = Color.MAGENTA
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.setNotificationChannel(
                SERVICE_CHANNEL_ID,
                context.getString(R.string.service_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            builder.setChannelId(SERVICE_CHANNEL_ID)
        }

        return builder.build()
    }

    fun showSingleTaskActiveNotification(context: Context) {
        val nm = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        val title = context.getString(R.string.have_a_break_message)
        val contentText = context.getString(R.string.have_a_break_ticker)

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val contentIntent = PendingIntent.getActivity(context, 0,
                intent, 0)

        val builder = NotificationCompat.Builder(context, SERVICE_SINGLE_TASK_CHANNEL_ID)
            .apply {
                setWhen(System.currentTimeMillis())
                setContentText(contentText)
                setContentIntent(contentIntent)
                setContentTitle(title)
                setSmallIcon(R.drawable.ic_have_a_break_24dp)
                priority = NotificationCompat.PRIORITY_LOW
                color = Color.MAGENTA
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.setNotificationChannel(
                SERVICE_SINGLE_TASK_CHANNEL_ID,
                context.getString(R.string.service_task_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            builder.setChannelId(SERVICE_SINGLE_TASK_CHANNEL_ID)
        }

        val notification = builder.build()
        nm.notify(R.string.have_a_break_message, notification)
    }

    fun cancelServiceActiveNotification(context: Context) {
        cancelNotification(context, R.string.service_is_active)
    }

    fun cancelSingleTaskActiveNotification(context: Context) {
        cancelNotification(context, R.string.have_a_break_message)
    }

    private fun cancelNotification(context: Context, resId: Int) {
        val nm = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(resId)
    }
}