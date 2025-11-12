package com.diabdata.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.diabdata.shared.R as shared

fun Context.showNotification(
    title: String,
    channelId: String = "diabdata_channel",
    channelName: String? = null,
    content: String,
    channelDescription: String? = null,
    iconName: String? = null,
    importance: NotificationImportance = NotificationImportance.DEFAULT
) {
    val safeChannelName = channelName ?: "DiabData Notifications"
    val safeChannelDescription = channelDescription ?: ""

    val (notifImportance, notifPriority) = when (importance) {
        NotificationImportance.HIGH -> NotificationManager.IMPORTANCE_HIGH to NotificationCompat.PRIORITY_HIGH
        NotificationImportance.LOW -> NotificationManager.IMPORTANCE_LOW to NotificationCompat.PRIORITY_LOW
        NotificationImportance.MIN -> NotificationManager.IMPORTANCE_MIN to NotificationCompat.PRIORITY_MIN
        else -> NotificationManager.IMPORTANCE_DEFAULT to NotificationCompat.PRIORITY_DEFAULT
    }

    val channel = NotificationChannel(channelId, safeChannelName, notifImportance).apply {
        description = safeChannelDescription
    }

    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)

    // Icône de notif
    val iconResId = iconName?.let {
        resources.getIdentifier(it, "drawable", packageName)
    } ?: shared.drawable.logo_icon_vector

    // Construire la notif
    val builder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(if (iconResId != 0) iconResId else shared.drawable.logo_icon_vector)
        .setContentTitle(title)
        .setContentText(content)
        .setStyle(NotificationCompat.BigTextStyle().bigText(content))
        .setPriority(notifPriority)

    // ID unique basé sur le temps
    manager.notify(System.currentTimeMillis().toInt(), builder.build())
}

enum class NotificationImportance {
    MIN, LOW, DEFAULT, HIGH
}