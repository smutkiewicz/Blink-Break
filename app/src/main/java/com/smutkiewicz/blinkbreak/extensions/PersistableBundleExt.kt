package com.smutkiewicz.blinkbreak.extensions

import android.os.PersistableBundle

/**
 * Boolean value extension for devices with API lower than 22.
 */
fun PersistableBundle.putBooleanValue(key: String, value: Boolean) {
    when {
        value -> putInt(key, 1)
        else -> putInt(key, 0)
    }
}

/**
 * Boolean value extension for devices with API lower than 22.
 */
fun PersistableBundle.getBooleanValue(key: String): Boolean {
    val value = getInt(key, 0)
    return when(value) {
        1 -> true
        else -> false
    }
}