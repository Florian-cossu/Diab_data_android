package com.diabdata.wearOsComplications.complicationsWorkers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diabdata.data.DiabDataDatabase
import com.diabdata.shared.utils.dateUtils.getNumberOfDaysUntil
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class ExpiringDevicesComplicationUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val nodeClient = Wearable.getNodeClient(applicationContext)
            nodeClient.connectedNodes.await()

            val dao = DiabDataDatabase.getDatabase(applicationContext).medicalDevicesDao()

            val today = LocalDate.now()

            val upcomingDevices = dao.getAllCurrentConsumableMedicalDevices(today).firstOrNull()
            val nextDevice = upcomingDevices?.minByOrNull { it.lifeSpanEndDate }

            val nextDeviceIcon = nextDevice?.deviceType.toString()

            val nextDeviceLifespan = nextDevice?.lifeSpan

            val numberOfDays = nextDevice?.lifeSpanEndDate?.getNumberOfDaysUntil()

            val dataMapRequest = PutDataMapRequest.create("/complications/medical_device_expiry")
            dataMapRequest.dataMap.putString("nextDeviceExpiry", numberOfDays.toString())
            dataMapRequest.dataMap.putString("nextDeviceLifespan", nextDeviceLifespan.toString())
            dataMapRequest.dataMap.putString("deviceIconRes", nextDeviceIcon)
            dataMapRequest.dataMap.putLong("timestamp", System.currentTimeMillis())

            val putDataRequest = dataMapRequest.asPutDataRequest()
            putDataRequest.setUrgent()

            Wearable.getDataClient(applicationContext)
                .putDataItem(putDataRequest)
                .addOnSuccessListener {
                }
                .addOnFailureListener { e ->
                }
                .await()

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
