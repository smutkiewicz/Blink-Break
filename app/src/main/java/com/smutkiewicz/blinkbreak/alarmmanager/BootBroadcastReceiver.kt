package com.smutkiewicz.blinkbreak.alarmmanager

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.content.WakefulBroadcastReceiver
import com.smutkiewicz.blinkbreak.util.NotificationsManager

/**
 * WakefulBroadcastReceiver ensures the device does not go back to sleep
 * during the startup of the service.
 */
class BootBroadcastReceiver : WakefulBroadcastReceiver() {
    override fun onReceive(context: Context, i: Intent) {
        val intent = Intent(context, BlinkBreakAlarmService::class.java)
        NotificationsManager.showServiceActiveNotification(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}