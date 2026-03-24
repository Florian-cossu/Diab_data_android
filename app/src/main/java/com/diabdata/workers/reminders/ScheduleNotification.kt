package com.diabdata.workers.reminders

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

fun scheduleNotification(
    context: Context,
    title: String,
    content: String,
    date: LocalDateTime,
    tag: String,
    hour: Int = 9
) {
    val notifyDateTime = date
    val delay = Duration.between(LocalDateTime.now(), notifyDateTime).toMillis()

    if (delay <= 0) return

    val debugTag =
        "${tag}_${notifyDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()}"

    val data = workDataOf(
        "title" to title,
        "content" to content,
        "tag" to tag
    )

    val workRequest = OneTimeWorkRequestBuilder<SingleNotificationWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(data)
        .addTag(tag)
        .addTag(debugTag)
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
}
