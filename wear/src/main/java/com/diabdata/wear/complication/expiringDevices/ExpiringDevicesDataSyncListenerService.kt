package com.diabdata.wear.complication.expiringDevices

import android.content.ComponentName
import androidx.core.content.edit
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class ExpiringDevicesDataSyncListenerService : WearableListenerService() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/medical_device_update") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val date = dataMap.getString("nextDeviceDate")
                val iconRes = dataMap.getString("deviceIconRes")
                dataMap.getLong("timestamp")

                if (date != null && iconRes != null) {
                    storeDataLocally(date, iconRes)
                    requestComplicationUpdate()
                }
            }
        }
    }

    private fun storeDataLocally(date: String, iconRes: String) {
        val prefs = getSharedPreferences("complication_data", MODE_PRIVATE)
        prefs.edit {
            putString("nextDeviceDate", date)
            putString("deviceIconRes", iconRes)
            putLong("timestamp", System.currentTimeMillis())
        }
    }

    private fun requestComplicationUpdate() {
        val componentName =
            ComponentName(this, ExpiringMedicalDevicesComplicationService::class.java)
        val requester = ComplicationDataSourceUpdateRequester.create(this, componentName)
        requester.requestUpdateAll()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}