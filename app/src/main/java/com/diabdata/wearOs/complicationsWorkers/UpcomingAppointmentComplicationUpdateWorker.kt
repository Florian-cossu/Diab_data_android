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

class UpcomingAppointmentComplicationUpdateWorker(
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

            val dao = DiabDataDatabase.getDatabase(applicationContext).appointmentDao()
            val today = LocalDate.now()
            val upcomingAppointments = dao.getUpcomingAppointmentsFlow(today).firstOrNull()

            val nextAppointment = upcomingAppointments?.minByOrNull { it.date }

            val dataMapRequest = PutDataMapRequest.create("/complications/upcoming_appointment")

            if (nextAppointment != null) {
                val numberOfDays = nextAppointment.date.getNumberOfDaysUntil()
                val appointmentType = nextAppointment.type.toString()
                val doctor = nextAppointment.doctor

                dataMapRequest.dataMap.putInt("nextAppointment", numberOfDays)
                dataMapRequest.dataMap.putString("doctor", doctor)
                dataMapRequest.dataMap.putString("appointmentType", appointmentType)
                dataMapRequest.dataMap.putBoolean("hasAppointment", true)
            } else {
                dataMapRequest.dataMap.putBoolean("hasAppointment", false)
            }

            dataMapRequest.dataMap.putLong("timestamp", System.currentTimeMillis())
            dataMapRequest.dataMap.putInt(
                "updateCount",
                (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
            )

            val putDataRequest = dataMapRequest.asPutDataRequest()
            putDataRequest.setUrgent()

            Wearable.getDataClient(applicationContext)
                .putDataItem(putDataRequest)
                .await()

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}