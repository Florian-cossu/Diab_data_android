package com.diabdata.wearOsComplications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diabdata.data.DiabDataDatabase
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ComplicationUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val nodeClient = Wearable.getNodeClient(applicationContext)
            val nodes = nodeClient.connectedNodes.await()
            nodes.forEach {
                Log.d("ComplicationWorker", "Connected node: ${it.displayName} (${it.id})")
            }

            val dao = DiabDataDatabase.getDatabase(applicationContext).medicalDevicesDao()

            val today = LocalDate.now()

            val upcomingDevices = dao.getAllCurrentConsumableMedicalDevices(today).firstOrNull()
            val nextDevice = upcomingDevices?.minByOrNull { it.lifeSpanEndDate }

            val nextDeviceIcon = nextDevice?.deviceType.toString()

            val dateText = nextDevice?.lifeSpanEndDate?.format(
                DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
            ) ?: "--/--"

            val dataMapRequest = PutDataMapRequest.create("/medical_device_update")
            dataMapRequest.dataMap.putString("nextDeviceDate", dateText)
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
