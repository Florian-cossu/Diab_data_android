package com.diabdata.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diabdata.R
import com.diabdata.utils.NotificationImportance
import com.diabdata.utils.showNotification

class SingleNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: return Result.failure()
        val content = inputData.getString("content") ?: return Result.failure()
        val tag = inputData.getString("tag") ?: "default"

        // utilise applicationContext au lieu de LocalContext
        val channelName = when (tag) {
            "treatments" -> applicationContext.getString(R.string.expiration_dates_reminder_notification_channel)
            "appointments" -> applicationContext.getString(R.string.appointment_reminder_notification_channel)
            else -> "DiabData Notifications"
        }

        applicationContext.showNotification(
            title = title,
            content = content,
            channelName = channelName,
            importance = NotificationImportance.HIGH
        )

        return Result.success()
    }
}
