package com.diabdata.widget.workers

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.glanceWidget.proto.WidgetAppointment
import com.diabdata.glanceWidget.proto.WidgetDevice
import com.diabdata.widget.glanceWidget.GlanceWidget
import com.diabdata.widget.widgetDataStore
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class GlanceWidgetWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {

        val db = DiabDataDatabase.getDatabase(context)
        val today = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        val devicesFlow =
            db.medicalDevicesDao().getAllCurrentConsumableMedicalDevices(today.toLocalDate())
        val devices = devicesFlow.firstOrNull()?.map {
            Log.i(
                "GlanceWidgetWorker",
                "Processing devices: ${it.name}, end=${it.lifeSpanEndDate}, today=$today, daysLeft=${
                    getDaysLeft(
                        it.lifeSpanEndDate
                    )
                }"
            )

            WidgetDevice.newBuilder()
                .setName(it.name)
                .setType(it.deviceType.toString())
                .setDaysLeft(getDaysLeft(it.lifeSpanEndDate).toInt())
                .setLifespanProgression(getLifeSpanProgress(it.date, it.lifeSpanEndDate))
                .setLifeSpanEndDate(it.lifeSpanEndDate.format(formatter))
                .build()
        } ?: emptyList()

        Log.i("GlanceWidgetWorker", "Updating DataStore with devices: $devices")


        val apptsFlow = db.appointmentDao().getUpcomingAppointmentsFlow(today)
        val nextAppt = apptsFlow.firstOrNull()?.firstOrNull()

        context.widgetDataStore.updateData { current ->
            current.toBuilder()
                .clearDevices()
                .addAllDevices(devices)
                .apply {
                    if (nextAppt != null) {
                        nextAppointment = WidgetAppointment.newBuilder()
                            .setDate(nextAppt.date.toString())
                            .setDoctor(nextAppt.doctor)
                            .setType(nextAppt.type.toString())
                            .build()
                    }
                }
                .setLastUpdated(System.currentTimeMillis())
                .build()
        }

        GlanceWidget().updateAll(context)

        return Result.success()
    }
}

fun getLifeSpanProgress(startDate: LocalDate, endDate: LocalDate): Int {
    val today = LocalDate.now()

    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toDouble().coerceAtLeast(1.0)
    val elapsedDays = ChronoUnit.DAYS.between(startDate, today).toDouble().coerceAtLeast(0.0)

    val progress = (elapsedDays / totalDays * 100).coerceIn(0.0, 100.0)

    return progress.roundToInt()
}

fun getDaysLeft(endDate: LocalDate): Long {
    val today = LocalDate.now()

    val daysLeft = ChronoUnit.DAYS.between(today, endDate)

    return daysLeft
}