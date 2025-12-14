package com.diabdata.wear.tile.glanceTile

import android.util.Log
import androidx.wear.tiles.TileService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class GlanceTileDataListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("GlanceTileDataListener", "onDataChanged appelé")

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                Log.d("GlanceTileDataListener", "Data changed on path: $path")

                if (path == "/diabdata/tile/glance_tile") {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val jsonData = dataMap.getString("json_data")
                    Log.d("GlanceTileDataListener", "Received JSON: $jsonData")

                    TileService.getUpdater(applicationContext)
                        .requestUpdate(GlanceTileService::class.java)

                    Log.d("GlanceTileDataListener", "Tile update requested")
                }
            }
        }
    }
}