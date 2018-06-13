package com.smutkiewicz.blinkbreak.model

data class Job(val breakEvery: Int, val breakDuration: Int,
               val areNotificationsEnabled: Boolean, val isLowerBrightness: Boolean)