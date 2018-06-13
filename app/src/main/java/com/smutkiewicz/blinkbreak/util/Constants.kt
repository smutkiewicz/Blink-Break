@file:JvmName("Constants")
@file:Suppress("PropertyName")

package com.smutkiewicz.blinkbreak.util

import com.smutkiewicz.blinkbreak.BuildConfig

@JvmField val PREF_BREAK_ENABLED = "tiny_break_enabled"
@JvmField val PREF_BREAK_EVERY = "tiny_break_every"
@JvmField val PREF_BREAK_DURATION = "tiny_break_duration"
@JvmField val PREF_NOTIFICATIONS = "notifications"
@JvmField val PREF_LOWER_BRIGHTNESS = "lower_brightness"
@JvmField val PREF_USER_BRIGHTNESS = "user_brightness"

@JvmField val MESSENGER_INTENT_KEY = "${BuildConfig.APPLICATION_ID}.MESSENGER_INTENT_KEY"
@JvmField val BREAK_DURATION_KEY = "${BuildConfig.APPLICATION_ID}.BREAK_DURATION_KEY"
@JvmField val NOTIFICATIONS_KEY = "${BuildConfig.APPLICATION_ID}.NOTIFICATIONS_KEY"
@JvmField val LOWER_BRIGHTNESS_KEY = "${BuildConfig.APPLICATION_ID}.LOWER_BRIGHTNESS_KEY"

@JvmField val MSG_START = 2
@JvmField val MSG_STOP = 3