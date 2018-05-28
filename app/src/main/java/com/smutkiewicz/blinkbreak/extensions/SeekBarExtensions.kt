package com.smutkiewicz.blinkbreak.extensions

import android.content.Context
import android.widget.SeekBar
import com.smutkiewicz.blinkbreak.R

fun SeekBar.getProgress(context: Context): Int {
    val intArray = context.resources.getIntArray(R.array.break_val_array)
    return intArray[progress]
}

fun SeekBar.getProgressLabel(context: Context): String {
    val stringArray = context.resources.getStringArray(R.array.break_array)
    return stringArray[progress]
}