package com.smutkiewicz.blinkbreak.extensions

import android.content.Context
import android.widget.SeekBar
import com.smutkiewicz.blinkbreak.R

fun SeekBar.getProgress(context: Context): Int {
    val intArray = context.resources.getIntArray(R.array.break_duration_val_array)
    return intArray[progress]
}

fun SeekBar.getProgress(context: Context, arrayShift: Int): Int {
    val intArray = context.resources.getIntArray(R.array.break_duration_val_array)
    val arraySize = intArray.size

    return when {
        arraySize <= progress + arrayShift + 1 -> intArray[progress + arrayShift]
        else -> intArray[progress]
    }
}

fun SeekBar.getProgressLabel(context: Context): String {
    val stringArray = context.resources.getStringArray(R.array.break_duration_array)
    return stringArray[progress]
}

fun SeekBar.getProgressLabel(context: Context, arrayShift: Int): String {
    val stringArray = context.resources.getStringArray(R.array.break_duration_array)
    val arraySize = stringArray.size

    return when {
        arraySize > progress + arrayShift -> stringArray[progress + arrayShift]
        else -> stringArray[progress]
    }
}