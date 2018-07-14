package com.smutkiewicz.blinkbreak.alarmmanager

import android.app.IntentService
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.PowerManager
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.content.WakefulBroadcastReceiver
import android.util.Log
import com.smutkiewicz.blinkbreak.extensions.getProgress
import com.smutkiewicz.blinkbreak.util.*
import java.util.*

class BlinkBreakAlarmService : IntentService("BlinkBreakAlarmService") {

    private var rsiWindowView: RsiWindowView? = null
    private var statsHelper: StatsHelper? = null
    private var userBrightness: Int = 0
    private var sp: SharedPreferences? = null
    private var duration: Long = 0

    // the Service onStart callback
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sp = PreferenceManager.getDefaultSharedPreferences(this)
        statsHelper = StatsHelper(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(FOREGROUND_ID, NotificationsManager.getServiceActiveNotification(this))
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopForeground(true)
    }

    override fun onHandleIntent(intent: Intent?) {
        WakefulBroadcastReceiver.completeWakefulIntent(intent)

        Log.i("BlinkBreakAlarmService", "Service running")
        Log.i(TAG, "on start task")

        // save current brightness to switch it in the future
        saveUserScreenBrightness()

        // get user's job parameters
        val notifications = sp!!.getBoolean(PREF_NOTIFICATIONS, true)
        val drawRsiWindow = sp!!.getBoolean(PREF_RSI_BREAK_WINDOW, false)
        val lowerBrightnessActivated = sp!!.getBoolean(PREF_LOWER_BRIGHTNESS, false)

        // get duration preference, it's stored as SeekBar step in user's SP
        val durationProgress = sp!!.getInt(PREF_BREAK_DURATION_PROGRESS, 0)
        // so we have to map it to millis
        duration = getProgress(this, durationProgress)

        when { notifications -> showSingleTaskActiveNotification() }
        when { lowerBrightnessActivated -> setScreenBrightness(0) }
        when { drawRsiWindow -> drawRsiWindow() }

        Timer().schedule(object : TimerTask() {
            override fun run() {

                if (lowerBrightnessActivated) {
                    userBrightness = getUserScreenBrightness()
                    setScreenBrightness(userBrightness)
                }

                when { notifications -> cancelSingleTaskActiveNotification() }
                when {
                    drawRsiWindow -> removeRsiWindow()
                    else -> updateStats()
                }
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

    private fun drawRsiWindow() {
        val myKM = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = powerManager.isScreenOn

        when {
            !myKM.inKeyguardRestrictedInputMode() -> {// device is not locked
                when {
                    isScreenOn -> {
                        rsiWindowView = RsiWindowView(this, duration)
                    } else -> {
                        Log.d(TAG, "Device screen is off")
                    }
                }
            } else -> {
                Log.d(TAG, "Device screen is locked")
            }
        }
    }

    private fun removeRsiWindow() {
        rsiWindowView?.destroy()
        rsiWindowView = null
    }

    private fun showSingleTaskActiveNotification() {
        val myKM = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = powerManager.isScreenOn
        val lockedDeviceNotificationsEnabled =
                sp!!.getBoolean(PREF_NOTIFICATIONS_WHEN_DEVICE_LOCKED, true)

        if (lockedDeviceNotificationsEnabled) {
            NotificationsManager.showSingleTaskActiveNotification(this)
        } else {
            when {
                !myKM.inKeyguardRestrictedInputMode() -> {
                    when {
                    // Device is not locked
                        isScreenOn -> {
                            NotificationsManager.showSingleTaskActiveNotification(this)
                        }
                        else -> { // Device screen is off
                        }
                    }
                }
                else -> { // do nothing
                }
            }
        }
    }

    private fun cancelSingleTaskActiveNotification() {
        NotificationsManager.cancelSingleTaskActiveNotification(this)
    }

    private fun updateStats() {
        statsHelper?.increaseValue(StatsHelper.STAT_UNSKIPPED_BREAKS)
        statsHelper?.lastBreak = StatsHelper.getTimeStamp()
    }

    companion object {
        private const val FOREGROUND_ID = 999
        private const val TAG = "BlinkBreakAlarmService"
    }
}