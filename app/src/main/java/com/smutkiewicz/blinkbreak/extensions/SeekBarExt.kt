package com.smutkiewicz.blinkbreak.extensions

import android.content.Context
import android.widget.SeekBar
import com.smutkiewicz.blinkbreak.R

/**
 * Set of extensions for mapping array resources to seekbars' progress values.
 */
fun SeekBar.getProgress(context: Context) =
        getProgressValue(context, R.array.break_duration_val_array)

fun SeekBar.getProgressLabel(context: Context) =
        getLabel(context, R.array.break_duration_array)

fun SeekBar.getProgressFrequency(context: Context)=
        getProgressValue(context, R.array.break_frequency_val_array)

fun SeekBar.getProgressFrequencyLabel(context: Context) =
        getLabel(context, R.array.break_frequency_array)

fun getProgress(context: Context, progress: Int): Long
{
    val intArray = context.resources.getIntArray(R.array.break_duration_val_array)
    return intArray[progress].toLong()
}

private fun SeekBar.getLabel(context: Context, resId: Int) : String
{
    val stringArray = context.resources.getStringArray(resId)
    return stringArray[progress]
}

private fun SeekBar.getProgressValue(context: Context, resId: Int): Long
{
    val intArray = context.resources.getIntArray(resId)
    return intArray[progress].toLong()
}