package com.diabdata

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.diabdata.core.database.DataRepository
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.workers.main.WorkersInitializer
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class DiabDataApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(this).pruneWork()

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
            userDetailsDao = db.userDetailsDao(),
            database = db
        )

        val vm = DataViewModel(repo, this)

        WorkersInitializer.enqueuePeriodicWorkers(this)
        WorkersInitializer.enqueueOneTimeWorkers(this)

        ProcessLifecycleOwner.get().lifecycleScope.launch {
            combine(
                vm.currentConsumableDevices,
                vm.upcomingAppointment,
                vm.upcomingExpiringTreatmentDates
            ) { devices, appointments, treatments ->
                Triple(devices, appointments, treatments)
            }
                .distinctUntilChanged()
                .collect {
                    Log.d("DiabDataApp", "Données modifiées, mise à jour des workers")
                    WorkersInitializer.enqueueOneTimeWorkers(this@DiabDataApp)
                }
        }
    }
}