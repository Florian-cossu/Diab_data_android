package com.diabdata.glanceWidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.diabdata.glanceWidget.proto.WidgetState
import com.diabdata.models.AddableType
import java.util.concurrent.TimeUnit

class GlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GlanceWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        enqueueWidgetUpdateWorker(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        enqueueWidgetUpdateWorker(context)
    }

    private fun enqueueWidgetUpdateWorker(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val workRequest = PeriodicWorkRequestBuilder<GlanceWidgetWorker>(
            30, TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "glance_widget_update",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}

/**
 * Simple widget to display basic infos
 *
 * This widget displays the next upcoming appointment as well as the list of active devices.
 * To add data or udpate its data structure make sure to update the WidgetState proto file found
 * in `app/src/main/proto/widget_state.proto` as well as its related file for update when data is
 * updated in the local database `app/src/main/java/com/diabdata/DiabDataApp.kt`
 */
class GlanceWidget : GlanceAppWidget() {

    override val stateDefinition = WidgetGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent(context)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val state = currentState<WidgetState>()
        val devices = state.devicesList
        val appointment = state.nextAppointment

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
                .background(GlanceTheme.colors.surface)
                .cornerRadius(16.dp)
        ) {
            Text(
                text = "Équipements",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurface
                ),
            )
            if (devices.isEmpty()) {
                Text(
                    text = "Aucun équipement actif",
                    style = TextStyle(fontSize = 12.sp)
                )
            } else {
                devices.forEach {
                    Text(
                        text = "${it.name} (${it.type}) - ${it.lifespanProgression}%",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurface
                        ),
                    )
                }
            }

            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "Prochain RDV",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GlanceTheme.colors.onSurface
                    )
                )

                if (appointment.date.isNotEmpty()) {
                    val typeEnum = getAppointmentTypeOrNull(appointment.type)

                    val baseBgColor = AddableType.APPOINTMENT.baseColor.copy(alpha = 0.2f)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.Start,
                        modifier = GlanceModifier.padding(top = 4.dp)
                    ) {
                        typeEnum?.let {
                            Image(
                                provider = ImageProvider(it.iconRes),
                                contentDescription = it.displayName(LocalContext.current),
                                modifier = GlanceModifier
                                    .size(40.dp)
                                    .background(baseBgColor)
                                    .padding(8.dp)
                                    .cornerRadius(100.dp),
                                colorFilter = ColorFilter.tint(
                                    AddableType.APPOINTMENT.baseColor.toColorProvider(
                                        ColorVariant.CIRCLE_ICON_ICON
                                    )
                                )
                            )
                        }

                        Text(
                            text = "${appointment.date} avec ${appointment.doctor}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = GlanceTheme.colors.onSurface
                            ),
                        )
                    }
                } else {
                    Text(
                        text = "Aucun rendez-vous prévu",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurface
                        ),
                    )
                }
            }

        }
    }
}