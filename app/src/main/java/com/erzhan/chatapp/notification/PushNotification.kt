package com.erzhan.chatapp.notification

import com.erzhan.chatapp.notification.NotificationData

data class PushNotification(
    val data: NotificationData,
    val to: String
)