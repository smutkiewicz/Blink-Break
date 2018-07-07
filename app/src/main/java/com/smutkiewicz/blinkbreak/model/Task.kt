package com.smutkiewicz.blinkbreak.model

data class Task(val breakEvery: Int, val breakDuration: Int,
                val areNotificationsEnabled: Boolean, val isLowerBrightness: Boolean)