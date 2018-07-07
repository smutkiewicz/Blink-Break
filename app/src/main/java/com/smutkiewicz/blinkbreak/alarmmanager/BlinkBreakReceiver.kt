package com.smutkiewicz.blinkbreak.alarmmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.smutkiewicz.blinkbreak.util.BREAK_DURATION_KEY


class BlinkBreakReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, p1: Intent?) {
        val intent = Intent(context, BlinkBreakAlarmService::class.java)
        val duration = intent!!.getLongExtra(BREAK_DURATION_KEY, 1000)

        Log.i("TAG", "Duration in receiver = " + duration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(intent)
        } else {
            context?.startService(intent)
        }
    }

    companion object {
        val REQUEST_CODE = 1
        val ACTION = "com.smutkiewicz.blinkbreak"
    }
}