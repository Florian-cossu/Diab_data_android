package com.diabdata.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.diabdata.R


fun Context.showNotification(
    title: String,
    notificationChannel: String?,
    content: String,
    notificationDescription: String?,
    iconName: String? = null
) {
    val channelId = "diabdata_channel"
    var notificationChannel = notificationChannel
    var notificationDescription = notificationDescription

    if (notificationChannel.isNullOrEmpty()) {
        notificationChannel = "DiabData Notifications"
    }

    if (notificationDescription.isNullOrEmpty()) {
        notificationDescription = ""
    }

    val channel = NotificationChannel(
        channelId, notificationChannel, NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = notificationDescription
    }

    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)

    val iconResId = iconName?.let {
        resources.getIdentifier(it, "drawable", packageName)
    } ?: R.drawable.logo_icon_vector

    val builder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(if (iconResId != 0) iconResId else R.drawable.logo_icon_vector)
        .setContentTitle(title).setContentText(content)
        .setStyle(NotificationCompat.BigTextStyle().bigText(content))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    manager.notify(System.currentTimeMillis().toInt(), builder.build())
}