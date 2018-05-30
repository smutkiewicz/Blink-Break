@file:JvmName("Constants")
@file:Suppress("PropertyName")

package com.smutkiewicz.blinkbreak.util

import com.smutkiewicz.blinkbreak.BuildConfig

@JvmField val PREF_TINY_BREAK_ENABLED = "tiny_break_enabled"
@JvmField val PREF_TINY_BREAK_EVERY = "tiny_break_every"
@JvmField val PREF_TINY_BREAK_DURATION = "tiny_break_duration"
@JvmField val PREF_BIG_BREAK_ENABLED = "big_break_enabled"
@JvmField val PREF_BIG_BREAK_EVERY = "big_break_every"
@JvmField val PREF_BIG_BREAK_DURATION = "big_break_duration"
@JvmField val PREF_NOTIFICATIONS = "notifications"
@JvmField val PREF_HIGH_IMPORTANCE = "high_importance"
@JvmField val PREF_LOWER_BRIGHTNESS = "lower_brightness"
@JvmField val PREF_USER_BRIGHTNESS = "user_brightness"

@JvmField val MSG_UNCOLOR_START = 0
@JvmField val MSG_UNCOLOR_STOP = 1
@JvmField val MSG_COLOR_START = 2
@JvmField val MSG_COLOR_STOP = 3

@JvmField val BREAK_TYPE_TINY = 0
@JvmField val BREAK_TYPE_BIG = 1

@JvmField val MESSENGER_INTENT_KEY = "${BuildConfig.APPLICATION_ID}.MESSENGER_INTENT_KEY"
@JvmField val BREAK_TYPE_KEY = "${BuildConfig.APPLICATION_ID}.BREAK_TYPE_KEY"
@JvmField val BREAK_EVERY_KEY = "${BuildConfig.APPLICATION_ID}.BREAK_EVERY_KEY"
@JvmField val BREAK_DURATION_KEY = "${BuildConfig.APPLICATION_ID}.BREAK_DURATION_KEY"
@JvmField val BRIGHTNESS_KEY = "${BuildConfig.APPLICATION_ID}.BRIGHTNESS_KEY"
@JvmField val NOTIFICATIONS_KEY = "${BuildConfig.APPLICATION_ID}.NOTIFICATIONS_KEY"
@JvmField val HIGH_IMPORTANCE_KEY = "${BuildConfig.APPLICATION_ID}.HIGH_IMPORTANCE_KEY"
@JvmField val LOWER_BRIGHTNESS_KEY = "${BuildConfig.APPLICATION_ID}.LOWER_BRIGHTNESS_KEY"