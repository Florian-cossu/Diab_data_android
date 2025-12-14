package com.diabdata.wear.tile.glanceTile

import android.net.Uri
import android.util.Log
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders.*
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors

// Mêmes data classes...
data class TileData(
    val device: DeviceInfo?,
    val treatment: TreatmentInfo?,
    val appointment: AppointmentInfo?,
    val timestamp: Long = System.currentTimeMillis()
)

data class DeviceInfo(
    val type: String,
    val daysUntilExpiry: Int,
    val lifespan: String
)

data class TreatmentInfo(
    val type: String,
    val daysUntilExpiry: Int,
    val daysSinceCreation: Int
)

data class AppointmentInfo(
    val type: String,
    val doctor: String,
    val daysUntil: Int
)

class GlanceTileService : TileService() {

    private val gson = Gson()
    private val serviceScope = CoroutineScope(
        Executors.newSingleThreadExecutor().asCoroutineDispatcher() + SupervisorJob()
    )

    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ListenableFuture<ResourceBuilders.Resources> {
        return serviceScope.future {
            ResourceBuilders.Resources.Builder()
                .setVersion("1")
                .build()
        }
    }

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest
    ): ListenableFuture<TileBuilders.Tile> {
        return serviceScope.future {
            // Timeout plus long
            val tileData = withTimeoutOrNull(3000L) {
                fetchTileData()
            }
            createTile(tileData)
        }
    }

    private suspend fun fetchTileData(): TileData? {
        return try {
            withContext(Dispatchers.IO) {
                val dataClient = Wearable.getDataClient(applicationContext)

                // Path correct qui correspond à celui du worker
                val dataItems = dataClient.getDataItems(
                    Uri.parse("wear://*/diabdata/tile/glance_tile")
                ).await()

                Log.d("GlanceTileService", "Found ${dataItems.count} data items")

                val result = dataItems.firstOrNull()?.let { item ->
                    val dataMap = DataMapItem.fromDataItem(item).dataMap
                    val jsonData = dataMap.getString("json_data")

                    Log.d("GlanceTileService", "JSON data: $jsonData")

                    jsonData?.let {
                        try {
                            gson.fromJson(it, TileData::class.java)
                        } catch (e: Exception) {
                            Log.e("GlanceTileService", "Erreur parsing JSON", e)
                            null
                        }
                    }
                }

                // Important: fermer les data items
                dataItems.release()

                result
            }
        } catch (e: Exception) {
            Log.e("GlanceTileService", "Erreur fetch data", e)
            null
        }
    }

    private fun createTile(data: TileData?): TileBuilders.Tile {
        Log.d("GlanceTileService", "Creating tile with data: $data")

        return TileBuilders.Tile.Builder()
            .setResourcesVersion("1")
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                Layout.Builder()
                                    .setRoot(createSimpleLayout(data))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun createSimpleLayout(data: TileData?): LayoutElement {
        return Column.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setAll(dp(12f))
                            .build()
                    )
                    .build()
            )
            .apply {
                // Titre
                addContent(
                    Text.Builder()
                        .setText("Diab Data")
                        .setFontStyle(
                            FontStyle.Builder()
                                .setSize(sp(16f))
                                .setWeight(FONT_WEIGHT_BOLD)
                                .build()
                        )
                        .build()
                )

                addContent(
                    Spacer.Builder()
                        .setHeight(dp(8f))
                        .build()
                )

                if (data != null) {
                    var hasData = false

                    // Device
                    data.device?.let { device ->
                        hasData = true
                        addContent(
                            Text.Builder()
                                .setText("• Device: ${device.type}")
                                .setFontStyle(
                                    FontStyle.Builder()
                                        .setSize(sp(12f))
                                        .build()
                                )
                                .build()
                        )
                        addContent(
                            Text.Builder()
                                .setText("  ${device.daysUntilExpiry} jours")
                                .setFontStyle(
                                    FontStyle.Builder()
                                        .setSize(sp(11f))
                                        .setColor(ColorBuilders.argb(0xFF999999.toInt()))
                                        .build()
                                )
                                .build()
                        )
                    }

                    // Treatment
                    data.treatment?.let { treatment ->
                        hasData = true
                        addContent(
                            Text.Builder()
                                .setText("• Traitement: ${treatment.type}")
                                .setFontStyle(
                                    FontStyle.Builder()
                                        .setSize(sp(12f))
                                        .build()
                                )
                                .build()
                        )
                        addContent(
                            Text.Builder()
                                .setText("  ${treatment.daysUntilExpiry} jours")
                                .setFontStyle(
                                    FontStyle.Builder()
                                        .setSize(sp(11f))
                                        .setColor(ColorBuilders.argb(0xFF999999.toInt()))
                                        .build()
                                )
                                .build()
                        )
                    }

                    // Appointment
                    data.appointment?.let { appointment ->
                        hasData = true
                        addContent(
                            Text.Builder()
                                .setText("• RDV: ${appointment.type}")
                                .setFontStyle(
                                    FontStyle.Builder()
                                        .setSize(sp(12f))
                                        .build()
                                )
                                .build()
                        )
                        addContent(
                            Text.Builder()
                                .setText("  ${appointment.daysUntil} jours")
                                .setFontStyle(
                                    FontStyle.Builder()
                                        .setSize(sp(11f))
                                        .setColor(ColorBuilders.argb(0xFF999999.toInt()))
                                        .build()
                                )
                                .build()
                        )
                    }

                    // Si aucune donnée
                    if (!hasData) {
                        addContent(
                            Text.Builder()
                                .setText("Aucune donnée disponible")
                                .setFontStyle(
                                    FontStyle.Builder()
                                        .setSize(sp(12f))
                                        .setColor(ColorBuilders.argb(0xFF999999.toInt()))
                                        .build()
                                )
                                .build()
                        )
                    }
                } else {
                    // Pas de données du tout
                    addContent(
                        Text.Builder()
                            .setText("En attente de sync...")
                            .setFontStyle(
                                FontStyle.Builder()
                                    .setSize(sp(12f))
                                    .setColor(ColorBuilders.argb(0xFF999999.toInt()))
                                    .build()
                            )
                            .build()
                    )

                    addContent(
                        Text.Builder()
                            .setText("Ouvrez l'app mobile")
                            .setFontStyle(
                                FontStyle.Builder()
                                    .setSize(sp(10f))
                                    .setColor(ColorBuilders.argb(0xFF666666.toInt()))
                                    .build()
                            )
                            .build()
                    )
                }
            }
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}