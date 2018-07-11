@file:JvmName("Constants")
@file:Suppress("PropertyName")

package com.smutkiewicz.blinkbreak.util

import com.smutkiewicz.blinkbreak.BuildConfig

const val PREF_IS_FIRST_TIME_LAUNCH = "is_first_time_launch"
const val PREF_BREAK_ENABLED = "tiny_break_enabled"
const val PREF_BREAK_EVERY_PROGRESS = "tiny_break_every"
const val PREF_BREAK_DURATION_PROGRESS = "tiny_break_duration"
const val PREF_NOTIFICATIONS = "notifications"
const val PREF_LOWER_BRIGHTNESS = "lower_brightness"
const val PREF_USER_BRIGHTNESS = "user_brightness"
const val PREF_RSI_BREAK_WINDOW = "rsi_break_window"

@JvmField val MESSENGER_INTENT_KEY = "${BuildConfig.APPLICATION_ID}.MESSENGER_INTENT_KEY"
@JvmField val BREAK_DURATION_KEY = "${BuildConfig.APPLICATION_ID}.BREAK_DURATION_KEY"
@JvmField val NOTIFICATIONS_KEY = "${BuildConfig.APPLICATION_ID}.NOTIFICATIONS_KEY"
@JvmField val LOWER_BRIGHTNESS_KEY = "${BuildConfig.APPLICATION_ID}.LOWER_BRIGHTNESS_KEY"