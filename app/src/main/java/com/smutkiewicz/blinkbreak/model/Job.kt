package com.smutkiewicz.blinkbreak.model

data class Job(val breakType: Int, val breakEvery: Int, val breakDuration: Int,
               val areNotificationsEnabled: Boolean, val highImportance: Boolean,
               val isLowerBrightness: Boolean)