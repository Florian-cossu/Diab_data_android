package com.diabdata.ui.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun Context.showNotification(data: Map<String, String>) {
    val channelId = "diabdata_channel"

    val channel = NotificationChannel(
        channelId,
        "DiabData Notifications",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Notifications pour tests"
    }
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)

    val content = data.entries.joinToString("\n") { "${it.key}: ${it.value}" }

    val builder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Données saisies")
        .setContentText(content)
        .setStyle(NotificationCompat.BigTextStyle().bigText(content))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    NotificationManagerCompat.from(this).notify(1, builder.build())
}
