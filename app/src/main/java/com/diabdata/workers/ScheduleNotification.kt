package com.diabdata.workers

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

fun scheduleNotification(
    context: Context,
    title: String,
    content: String,
    date: LocalDate,
    tag: String,
    hour: Int = 9
) {
    val notifyDateTime = date.atTime(hour, 0)
    val delay = Duration.between(LocalDateTime.now(), notifyDateTime).toMillis()

    if (delay <= 0) return

    val debugTag =
        "${tag}_${notifyDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()}"

    val data = workDataOf(
        "title" to title,
        "content" to content
    )

    val workRequest = OneTimeWorkRequestBuilder<SingleNotificationWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(data)
        .addTag(tag)
        .addTag(debugTag)
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
}
