package com.smutkiewicz.blinkbreak.model

data class Task(val breakEvery: Long, val breakDuration: Long,
                val areNotificationsEnabled: Boolean, val isLowerBrightness: Boolean)