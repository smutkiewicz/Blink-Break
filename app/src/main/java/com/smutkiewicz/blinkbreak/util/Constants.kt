@file:JvmName("Constants")
@file:Suppress("PropertyName")

package com.smutkiewicz.blinkbreak.util

import com.smutkiewicz.blinkbreak.BuildConfig

const val PREF_IS_FIRST_TIME_LAUNCH = "is_first_time_launch"
const val PREF_BREAK_EVERY_PROGRESS = "tiny_break_every"
const val PREF_BREAK_DURATION_PROGRESS = "tiny_break_duration"
const val PREF_NOTIFICATIONS = "notifications"
const val PREF_LOWER_BRIGHTNESS = "lower_brightness"
const val PREF_USER_BRIGHTNESS = "user_brightness"
const val PREF_RSI_BREAK_WINDOW = "rsi_break_window"
const val PREF_NOTIFICATIONS_WHEN_DEVICE_LOCKED = "notify_when_device_is_locked"

const val BREAK_DURATION_KEY = "${BuildConfig.APPLICATION_ID}.BREAK_DURATION_KEY"