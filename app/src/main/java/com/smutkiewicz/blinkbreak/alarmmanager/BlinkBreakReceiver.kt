package com.smutkiewicz.blinkbreak.alarmmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build


class BlinkBreakReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, p1: Intent?) {
        val intent = Intent(context, BlinkBreakAlarmService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(intent)
        } else {
            context?.startService(intent)
        }
    }

    companion object {
        private val TAG = "AlarmReceiver"
        val REQUEST_CODE = 1
    }
}