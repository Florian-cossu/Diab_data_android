package com.diabdata.wear.tile.glanceTile

import android.util.Log
import androidx.core.net.toUri
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.FontStyle
import androidx.wear.protolayout.LayoutElementBuilders.Layout
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.LayoutElementBuilders.Text
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.System.currentTimeMillis
import java.util.concurrent.Executors

data class TileData(
    val device: DeviceInfo?,
    val treatment: TreatmentInfo?,
    val appointment: AppointmentInfo?,
    val timestamp: Long = currentTimeMillis()
)

data class DeviceInfo(
    val type: String, val daysUntilExpiry: Int, val lifespan: String
)

data class TreatmentInfo(
    val type: String, val daysUntilExpiry: Int, val daysSinceCreation: Int
)

data class AppointmentInfo(
    val type: String, val doctor: String, val daysUntil: Int
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
            ResourceBuilders.Resources.Builder().setVersion("1").build()
        }
    }

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest
    ): ListenableFuture<TileBuilders.Tile> {
        return serviceScope.future {
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
                    "wear://*/diabdata/tile/glance_tile".toUri()
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

                dataItems.release()

                result
            }
        } catch (e: Exception) {
            Log.e("GlanceTileService", "Erreur fetch data", e)
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

fun createTile(data: TileData?): TileBuilders.Tile {
    Log.d("GlanceTileService", "Creating tile with data: $data")

    return TileBuilders.Tile.Builder().setResourcesVersion("1").setTileTimeline(
            TimelineBuilders.Timeline.Builder().addTimelineEntry(
                    TimelineBuilders.TimelineEntry.Builder().setLayout(
                            Layout.Builder().setRoot(createSimpleLayout(data)).build()
                        ).build()
                ).build()
        ).build()
}

fun createSimpleLayout(data: TileData?): LayoutElement {
    return Column.Builder().setWidth(expand()).setHeight(expand()).setModifiers(
            ModifiersBuilders.Modifiers.Builder().setPadding(
                    ModifiersBuilders.Padding.Builder().setAll(dp(12f)).build()
                ).build()
        ).apply {
            addContent(
                Spacer.Builder().setHeight(dp(8f)).build()
            )

            if (data != null) {
                var hasData = false

                // Device
                data.device?.let { device ->
                    hasData = true
                    addContent(
                        Text.Builder().setText("• Device: ${device.type}").setFontStyle(
                                FontStyle.Builder().setSize(sp(12f)).build()
                            ).build()
                    )
                    addContent(
                        Text.Builder().setText("  ${device.daysUntilExpiry} jours").setFontStyle(
                                FontStyle.Builder().setSize(sp(11f))
                                    .setColor(ColorBuilders.argb(0xFF999999.toInt())).build()
                            ).build()
                    )
                }

                // Treatment
                data.treatment?.let { treatment ->
                    hasData = true
                    addContent(
                        Text.Builder().setText("• Traitement: ${treatment.type}").setFontStyle(
                                FontStyle.Builder().setSize(sp(12f)).build()
                            ).build()
                    )
                    addContent(
                        Text.Builder().setText("  ${treatment.daysUntilExpiry} jours").setFontStyle(
                                FontStyle.Builder().setSize(sp(11f))
                                    .setColor(ColorBuilders.argb(0xFF999999.toInt())).build()
                            ).build()
                    )
                }

                // Appointment
                data.appointment?.let { appointment ->
                    hasData = true
                    addContent(
                        Text.Builder().setText("• RDV: ${appointment.type}").setFontStyle(
                                FontStyle.Builder().setSize(sp(12f)).build()
                            ).build()
                    )
                    addContent(
                        Text.Builder().setText("  ${appointment.daysUntil} jours").setFontStyle(
                                FontStyle.Builder().setSize(sp(11f))
                                    .setColor(ColorBuilders.argb(0xFF999999.toInt())).build()
                            ).build()
                    )
                }

                // Si aucune donnée
                if (!hasData) {
                    addContent(
                        Text.Builder().setText("Aucune donnée disponible").setFontStyle(
                                FontStyle.Builder().setSize(sp(12f))
                                    .setColor(ColorBuilders.argb(0xFF999999.toInt())).build()
                            ).build()
                    )
                }
            } else {
                // Pas de données du tout
                addContent(
                    Text.Builder().setText("En attente de sync...").setFontStyle(
                            FontStyle.Builder().setSize(sp(12f))
                                .setColor(ColorBuilders.argb(0xFF999999.toInt())).build()
                        ).build()
                )

                addContent(
                    Text.Builder().setText("Ouvrez l'app mobile").setFontStyle(
                            FontStyle.Builder().setSize(sp(10f))
                                .setColor(ColorBuilders.argb(0xFF666666.toInt())).build()
                        ).build()
                )
            }
        }.build()
}

class GlanceTileLayoutPreview {
    @Preview(device = WearDevices.SMALL_ROUND, name = "Round Device")
    @Preview(device = WearDevices.SQUARE, name = "Square Device")
    fun tilePreviewWithData(): TilePreviewData {
        val fakeData = TileData(
            device = DeviceInfo(
                type = "CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_SENSOR", daysUntilExpiry = 3, lifespan = "7 jours"
            ), treatment = TreatmentInfo(
                type = "FAST_ACTING_INSULIN_CARTRIDGE", daysUntilExpiry = 12, daysSinceCreation = 5
            ), appointment = AppointmentInfo(
                type = "ANNUAL_CHECKUP", doctor = "Dr. Martin", daysUntil = 25
            ), timestamp = currentTimeMillis()
        )

        return TilePreviewData {
            createTile(fakeData)
        }
    }

    @Preview(device = WearDevices.SMALL_ROUND, name = "Round Device - No Data")
    @Preview(device = WearDevices.SQUARE, name = "Square Device - No Data")
    fun tilePreviewWithNoData(): TilePreviewData {
        return TilePreviewData {
            createTile(null)
        }
    }
}