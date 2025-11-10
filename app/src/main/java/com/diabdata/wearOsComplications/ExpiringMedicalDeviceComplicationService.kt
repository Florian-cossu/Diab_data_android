package com.diabdata.wearOsComplications

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.diabdata.data.DiabDataDatabase
import com.diabdata.models.MedicalDeviceInfoType
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MedicalDeviceComplicationService : ComplicationDataSourceService() {

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        runBlocking {
            val dao = DiabDataDatabase.getDatabase(applicationContext).medicalDevicesDao()

            val today = LocalDate.now()
            val nextMonth = today.plusMonths(1)

            val upcomingDevices = dao.getUpcomingExpirationDatesFlow(today, nextMonth).firstOrNull()
            val nextDevice = upcomingDevices?.minByOrNull { it.lifeSpanEndDate }

            val nextDeviceIcon = nextDevice?.let {
                val typeEnum = try {
                    MedicalDeviceInfoType.valueOf(it.deviceType.toString())
                } catch (e: IllegalArgumentException) {
                    MedicalDeviceInfoType.UNKNOWN
                }
                typeEnum.iconRes
            } ?: MedicalDeviceInfoType.UNKNOWN.iconRes

            val dateText = nextDevice?.lifeSpanEndDate?.format(
                DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
            ) ?: "--/--"

            val data = ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(dateText).build(),
                contentDescription = PlainComplicationText.Builder("Prochaine expiration").build()
            )
                .setMonochromaticImage(
                    MonochromaticImage.Builder(
                        Icon.createWithResource(applicationContext, nextDeviceIcon)
                    ).build()
                )
                .build()

            listener.onComplicationData(data)
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData {
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("28 Oct").build(),
            contentDescription = PlainComplicationText.Builder("Exemple de date d’expiration")
                .build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    Icon.createWithResource(
                        applicationContext,
                        MedicalDeviceInfoType.CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_SENSOR.iconRes
                    )
                ).build()
            )
            .build()
    }
}