package com.diabdata.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import com.diabdata.R


fun Context.showNotification(
    title: String,
    content: String,
    iconName: String? = null
) {
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

    val iconResId = iconName?.let {
        resources.getIdentifier(it, "drawable", packageName)
    } ?: R.drawable.logo_icon_vector

    val builder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(if (iconResId != 0) iconResId else R.drawable.logo_icon_vector)
        .setContentTitle(title)
        .setContentText(content)
        .setStyle(NotificationCompat.BigTextStyle().bigText(content))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    manager.notify(System.currentTimeMillis().toInt(), builder.build())
}

fun getItemShape(index: Int, size: Int): Shape {
    if (size == 1) {
        return RoundedCornerShape(16.dp)
    }
    return when (index) {
        0 -> RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 3.dp,
            bottomEnd = 3.dp
        )

        size - 1 -> RoundedCornerShape(
            topStart = 3.dp,
            topEnd = 3.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )

        else -> androidx.compose.foundation.shape.RoundedCornerShape(3.dp)
    }
}