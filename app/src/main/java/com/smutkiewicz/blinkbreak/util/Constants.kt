@file:JvmName("Constants")
@file:Suppress("PropertyName")

package com.smutkiewicz.blinkbreak.util

import com.smutkiewicz.blinkbreak.BuildConfig

@JvmField val MSG_UNCOLOR_START = 0
@JvmField val MSG_UNCOLOR_STOP = 1
@JvmField val MSG_COLOR_START = 2
@JvmField val MSG_COLOR_STOP = 3

@JvmField val MESSENGER_INTENT_KEY = "${BuildConfig.APPLICATION_ID}.MESSENGER_INTENT_KEY"
@JvmField val INTERVAL_KEY = "${BuildConfig.APPLICATION_ID}.INTERVAL_KEY"
@JvmField val BREAK_LENGTH_KEY = "${BuildConfig.APPLICATION_ID}.BREAK_LENGTH_KEY"
@JvmField val BRIGHTNESS_KEY = "${BuildConfig.APPLICATION_ID}.BRIGHTNESS_KEY"