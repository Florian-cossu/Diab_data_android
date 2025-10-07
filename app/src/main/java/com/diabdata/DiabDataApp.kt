package com.diabdata

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.diabdata.data.DataRepository
import com.diabdata.data.DataViewModel
import com.diabdata.data.DiabDataDatabase
import com.diabdata.glanceWidget.GlanceWidget
import com.diabdata.glanceWidget.getLifeSpanProgress
import com.diabdata.glanceWidget.proto.WidgetAppointment
import com.diabdata.glanceWidget.proto.WidgetDevice
import com.diabdata.glanceWidget.widgetDataStore
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
            }.distinctUntilChanged().collect { (devices, appointment) ->
                widgetDataStore.updateData { current ->
                    current.toBuilder()
                        .clearDevices()
                        .addAllDevices(
                            devices.map {
                                WidgetDevice.newBuilder()
                                    .setName(it.name)
                                    .setType(it.deviceType.toString())
                                    .setLifespanProgression(
                                        getLifeSpanProgress(it.date, it.lifeSpanEndDate)
                                    )
                                    .build()
                            }
                        )
                        .apply {
                            if (appointment != null) {
                                nextAppointment = WidgetAppointment.newBuilder()
                                    .setDate(appointment.date.toString())
                                    .setDoctor(appointment.doctor)
                                    .setType(appointment.type.toString())
                                    .build()
                            }
                        }
                        .build()
                }

                GlanceWidget().updateAll(this@DiabDataApp)
            }
        }
    }
}
