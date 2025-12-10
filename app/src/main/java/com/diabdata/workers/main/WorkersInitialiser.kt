package com.diabdata.workers.main

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.diabdata.glanceWidget.GlanceWidgetWorker
import com.diabdata.wearOsComplications.complicationsWorkers.ExpiringDevicesComplicationUpdateWorker
import com.diabdata.wearOsComplications.complicationsWorkers.ExpiringTreatmentComplicationUpdateWorker
import com.diabdata.wearOsComplications.complicationsWorkers.UpcomingAppointmentComplicationUpdateWorker
import java.util.concurrent.TimeUnit

object WorkersInitializer {

    fun enqueuePeriodicWorkers(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val periodicDeviceExpiryComplicationWork =
            PeriodicWorkRequestBuilder<ExpiringDevicesComplicationUpdateWorker>(
                30, TimeUnit.MINUTES
            ).build()

        val periodicUpcomingAppointmentComplicationWork =
            PeriodicWorkRequestBuilder<UpcomingAppointmentComplicationUpdateWorker>(
                12, TimeUnit.HOURS
            ).build()

        val periodicTreatmentExpiryWorkPolicy =
            PeriodicWorkRequestBuilder<ExpiringTreatmentComplicationUpdateWorker>(
                12, TimeUnit.HOURS
            ).build()

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = "complication_medical_device_expiry_periodic_update",
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
            request = periodicDeviceExpiryComplicationWork
        )

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = "complication_upcoming_appointment_periodic_update",
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
            request = periodicUpcomingAppointmentComplicationWork
        )

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = "complication_treatment_expiry_periodic_update",
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
            request = periodicTreatmentExpiryWorkPolicy
        )
    }

    fun enqueueOneTimeWorkers(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Glance widget
        val glanceWork =
            OneTimeWorkRequestBuilder<GlanceWidgetWorker>().setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        workManager.enqueueUniqueWork(
            "glance_widget_update_once", ExistingWorkPolicy.REPLACE, glanceWork
        )

        // Wear OS complications
        val expiringDevicesComplicationWork =
            OneTimeWorkRequestBuilder<ExpiringDevicesComplicationUpdateWorker>().setExpedited(
                OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
            ).build()

        workManager.enqueueUniqueWork(
            "complication_medical_device_expiry_update_once",
            ExistingWorkPolicy.REPLACE,
            expiringDevicesComplicationWork
        )

        val upcomingAppointmentComplicationWork =
            OneTimeWorkRequestBuilder<UpcomingAppointmentComplicationUpdateWorker>().setExpedited(
                OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
            ).build()

        workManager.enqueueUniqueWork(
            uniqueWorkName = "complication_upcoming_appointment_update_once",
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = upcomingAppointmentComplicationWork
        )

        val expiringTreatmentComplicationWork =
            OneTimeWorkRequestBuilder<ExpiringTreatmentComplicationUpdateWorker>().setExpedited(
                OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
            ).build()

        workManager.enqueueUniqueWork(
            uniqueWorkName = "complication_treatment_expiry_update_once",
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = expiringTreatmentComplicationWork
        )
    }
}