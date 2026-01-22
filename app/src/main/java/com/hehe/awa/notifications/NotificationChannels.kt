package com.hehe.awa.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object NotificationChannels {
    const val CHANNEL_GENERAL_ID = "general_notifications"
    const val CHANNEL_GENERAL_NAME = "General Notifications"
    const val CHANNEL_GENERAL_DESCRIPTION = "General notifications and updates"

    const val CHANNEL_FRIENDS_ID = "friends_notifications"
    const val CHANNEL_FRIENDS_NAME = "Friends Notifications"
    const val CHANNEL_FRIENDS_DESCRIPTION = "Notifications about friend requests and friend activities"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // General notifications channel
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL_ID,
                CHANNEL_GENERAL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_GENERAL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            // Friends notifications channel
            val friendsChannel = NotificationChannel(
                CHANNEL_FRIENDS_ID,
                CHANNEL_FRIENDS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_FRIENDS_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            notificationManager.createNotificationChannel(generalChannel)
            notificationManager.createNotificationChannel(friendsChannel)
        }
    }
}

