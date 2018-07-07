package com.smutkiewicz.blinkbreak.alarmmanager

import android.content.Context
import android.content.Intent
import android.support.v4.content.WakefulBroadcastReceiver

// WakefulBroadcastReceiver ensures the device does not go back to sleep
// during the startup of the service
class BootBroadcastReceiver : WakefulBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Launch the specified service when this message is received
        val startServiceIntent = Intent(context, BlinkBreakAlarmService::class.java)
        startWakefulService(context, startServiceIntent)
    }
}