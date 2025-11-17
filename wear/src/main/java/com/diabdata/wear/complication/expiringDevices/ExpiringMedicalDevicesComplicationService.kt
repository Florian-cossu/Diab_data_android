package com.diabdata.wear.complication.expiringDevices

import android.content.ComponentName
import android.graphics.drawable.Icon
import android.net.Uri
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.diabdata.wear.models.MedicalDeviceInfoType
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable

class ExpiringMedicalDevicesComplicationService :
    SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {

        val prefs = getSharedPreferences("complication_data", MODE_PRIVATE)
        val latestDate = prefs.getString("nextDeviceDate", "--/--") ?: "--/--"
        val deviceTypeStr = prefs.getString("deviceIconRes", "UNKNOWN") ?: "UNKNOWN"

        updateFromDataClient()

        val latestIconRes = try {
            MedicalDeviceInfoType.valueOf(deviceTypeStr).iconRes
        } catch (e: Exception) {
            MedicalDeviceInfoType.UNKNOWN.iconRes
        }

        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("").build(),
            contentDescription = PlainComplicationText.Builder("Prochaine expiration: $latestDate")
                .build()
        )
            .setTitle(
                PlainComplicationText.Builder(latestDate)
                    .build()
            )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    Icon.createWithResource(applicationContext, latestIconRes)
                ).build()
            )
            .build()
    }

    private fun updateFromDataClient() {
        val dataClient = Wearable.getDataClient(applicationContext)
        dataClient.getDataItems(Uri.parse("wear://*/medical_device_update"))
            .addOnSuccessListener { dataItemBuffer ->

                var latestTimestamp = 0L
                var latestDate: String? = null
                var deviceTypeStr: String? = null

                dataItemBuffer.forEach { item ->
                    val dataMap = DataMapItem.fromDataItem(item).dataMap
                    val timestamp = dataMap.getLong("timestamp", 0L)

                    if (timestamp > latestTimestamp) {
                        latestDate = dataMap.getString("nextDeviceDate")
                        deviceTypeStr = dataMap.getString("deviceIconRes")
                        latestTimestamp = timestamp
                    }
                }

                dataItemBuffer.release()

                if (latestDate != null && deviceTypeStr != null) {
                    storeDataLocally(latestDate, deviceTypeStr)

                    val componentName = ComponentName(
                        applicationContext,
                        ExpiringMedicalDevicesComplicationService::class.java
                    )
                    val requester = ComplicationDataSourceUpdateRequester.create(
                        applicationContext,
                        componentName
                    )
                    requester.requestUpdateAll()
                }
            }
            .addOnFailureListener { e ->
            }
    }

    private fun storeDataLocally(date: String, iconRes: String) {
        val prefs = getSharedPreferences("complication_data", MODE_PRIVATE)
        prefs.edit().apply {
            putString("nextDeviceDate", date)
            putString("deviceIconRes", iconRes)
            putLong("timestamp", System.currentTimeMillis())
            apply()
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData {
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("28 Oct").build(),
            contentDescription = PlainComplicationText.Builder("WIRELESS_PUMP").build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    Icon.createWithResource(
                        applicationContext,
                        MedicalDeviceInfoType.UNKNOWN.iconRes
                    )
                ).build()
            )
            .build()
    }
}