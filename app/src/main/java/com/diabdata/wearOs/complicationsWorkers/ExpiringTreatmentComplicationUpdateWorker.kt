package com.diabdata.wearOs.complicationsWorkers

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

class ExpiringTreatmentComplicationUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val nodeClient = Wearable.getNodeClient(applicationContext)
            val nodes = nodeClient.connectedNodes.await()

            if (nodes.isEmpty()) {
                return Result.failure()
            }

            val dao = DiabDataDatabase.getDatabase(applicationContext).treatmentDao()

            val today = LocalDate.now()

            val upcomingTreatments = dao.getUpcomingExpirationDatesFlow(today).firstOrNull()
            val nextTreatment = upcomingTreatments?.minByOrNull { it.expirationDate }

            val dataMapRequest = PutDataMapRequest.create("/complications/treatment_expiry")

            if (nextTreatment != null) {
                val elapsed = nextTreatment.createdAt.getNumberOfDaysUntil()
                val numberOfDays = nextTreatment.expirationDate.getNumberOfDaysUntil()
                val nextTreatmentIcon = nextTreatment.type.toString()

                dataMapRequest.dataMap.putInt("nextTreatmentExpiryDays", numberOfDays)
                dataMapRequest.dataMap.putInt("elapsedTreatmentConservation", elapsed)
                dataMapRequest.dataMap.putString("treatmentIconRes", nextTreatmentIcon)
                dataMapRequest.dataMap.putBoolean("hasTreatment", true)
            } else {
                dataMapRequest.dataMap.putBoolean("hasTreatment", false)
            }

            dataMapRequest.dataMap.putLong("timestamp", System.currentTimeMillis())

            val putDataRequest = dataMapRequest.asPutDataRequest()
            putDataRequest.setUrgent()

            Wearable.getDataClient(applicationContext)
                .putDataItem(putDataRequest)
                .await()

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}