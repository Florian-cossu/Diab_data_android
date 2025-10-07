package com.diabdata.glanceWidget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diabdata.data.DiabDataDatabase
import com.diabdata.glanceWidget.proto.WidgetAppointment
import com.diabdata.glanceWidget.proto.WidgetDevice
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class GlanceWidgetWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val db = DiabDataDatabase.getDatabase(context)
        val today = LocalDate.now()

        val devicesFlow = db.medicalDevicesDao().getAllCurrentConsumableMedicalDevices(today)
        val devices = devicesFlow.firstOrNull()?.map {
            WidgetDevice.newBuilder()
                .setName(it.name)
                .setType(it.deviceType.toString())
                .setLifespanProgression(getLifeSpanProgress(it.date, it.lifeSpanEndDate))
                .build()
        } ?: emptyList()

        val apptsFlow = db.appointmentDao().getUpcomingAppointmentsFlow(today)
        val nextAppt = apptsFlow.firstOrNull()?.firstOrNull()

        // ⚙️ Mettre à jour le DataStore Proto
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