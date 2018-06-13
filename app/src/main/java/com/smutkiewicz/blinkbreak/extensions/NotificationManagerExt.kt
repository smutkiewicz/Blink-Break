package com.smutkiewicz.blinkbreak.extensions

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager


@TargetApi(26)
fun NotificationManager.setNotificationChannel(id: String, channelName: String, importance: Int) {
    val notificationChannel = NotificationChannel(id, channelName, importance)
    createNotificationChannel(notificationChannel)
}
