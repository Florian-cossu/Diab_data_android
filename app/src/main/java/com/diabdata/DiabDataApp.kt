package com.diabdata

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import com.diabdata.data.DataRepository
import com.diabdata.data.DataViewModel
import com.diabdata.data.DiabDataDatabase
import com.diabdata.glanceWidget.GlanceWidgetWorker
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

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

        ProcessLifecycleOwner.get().lifecycleScope.launch {
            combine(
                vm.currentConsumableDevices,
                vm.upcomingAppointment
            ) { devices, appointments ->
                devices to appointments.firstOrNull()
            }
                .distinctUntilChanged()
                .collect {
                    val workRequest = OneTimeWorkRequestBuilder<GlanceWidgetWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build()

                    val workManager = androidx.work.WorkManager.getInstance(this@DiabDataApp)
                    workManager.enqueueUniqueWork(
                        "glance_widget_update_once",
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
            }
        }
    }
}
