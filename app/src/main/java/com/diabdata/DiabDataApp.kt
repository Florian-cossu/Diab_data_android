package com.diabdata

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.diabdata.data.DataRepository
import com.diabdata.data.DataViewModel
import com.diabdata.data.DiabDataDatabase
import com.diabdata.glanceWidget.GlanceWidgetWorker
import com.diabdata.wearOsComplications.ComplicationUpdateWorker
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DiabDataApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val db = DiabDataDatabase.getDatabase(this)
        val repo = DataRepository(
            weightDao = db.weightDao(),
            hba1cDao = db.hba1cDao(),
            appointmentDao = db.appointmentDao(),
            treatmentDao = db.treatmentDao(),
            importantDateDao = db.importantDateDao(),
            medicationDao = db.medicationDao(),
            medicalDevicesDao = db.medicalDevicesDao(),
            medicalDeviceInfo = db.medicalDevicesInfoDao(),
            database = db
        )

        val vm = DataViewModel(repo, this)
        val workManager = WorkManager.getInstance(this)

        val periodicComplicationWork = PeriodicWorkRequestBuilder<ComplicationUpdateWorker>(
            30, TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "complication_periodic_update",
            ExistingPeriodicWorkPolicy.KEEP, // ne pas recréer si déjà présent
            periodicComplicationWork
        )

        ProcessLifecycleOwner.get().lifecycleScope.launch {
            combine(
                vm.currentConsumableDevices,
                vm.upcomingAppointment
            ) { devices, appointments ->
                devices to appointments.firstOrNull()
            }
                .distinctUntilChanged()
                .collect {
                    // Glance widget
                    val glanceWork = OneTimeWorkRequestBuilder<GlanceWidgetWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build()
                    workManager.enqueueUniqueWork(
                        "glance_widget_update_once",
                        ExistingWorkPolicy.REPLACE,
                        glanceWork
                    )

                    // Wear OS complication
                    val complicationWork = OneTimeWorkRequestBuilder<ComplicationUpdateWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build()
                    workManager.enqueueUniqueWork(
                        "complication_update_once",
                        ExistingWorkPolicy.REPLACE,
                        complicationWork
                    )
                }
        }
    }
}
