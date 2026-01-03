package com.diabdata.workers.reminders

import android.content.Context
import com.diabdata.data.DataViewModel
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import com.diabdata.shared.R as shared

suspend fun scheduleAllReminders(context: Context, dataViewModel: DataViewModel) {
    val reminderOffsets = listOf(30, 14, 1)

    val appointments = dataViewModel.upcomingAppointment.first()
    val expirations = dataViewModel.upcomingExpiringTreatmentDates.first()

    appointments.forEach { appointment ->
        reminderOffsets.forEach { offset ->
            val notifyDate = appointment.date.minusDays(offset.toLong())

            val baseContent = context.getString(
                shared.string.notification_appointment_content,
                appointment.doctor,
                appointment.date.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.getDefault())
                )
            )
            val content =
                if (appointment.notes?.isNotBlank() == true) "$baseContent\n- ${appointment.notes}" else baseContent

            scheduleNotification(
                context,
                title = context.getString(shared.string.notification_appointment_title),
                content = content,
                date = notifyDate,
                tag = "appointments"
            )
        }
    }

    expirations.forEach { treatment ->
        reminderOffsets.forEach { offset ->
            val notifyDate = treatment.expirationDate.minusDays(offset.toLong())
            scheduleNotification(
                context,
                title = context.getString(shared.string.notification_expiration_title),
                content = context.getString(
                    shared.string.notification_expiration_content,
                    treatment.name,
                    treatment.expirationDate.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(
                            Locale.getDefault()
                        )
                    )
                ),
                date = notifyDate,
                tag = "treatments"
            )
        }
    }
}

suspend fun scheduleAppointmentReminders(context: Context, dataViewModel: DataViewModel) {
    val reminderOffsets = listOf(30, 14, 1)
    val appointments = dataViewModel.upcomingAppointment.first()

    appointments.forEach { appointment ->
        reminderOffsets.forEach { offset ->
            val notifyDate = appointment.date.minusDays(offset.toLong())
            val baseContent = context.getString(
                shared.string.notification_appointment_content,
                appointment.doctor,
                appointment.date.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.getDefault())
                )
            )
            val content =
                if (appointment.notes?.isNotBlank() == true) "$baseContent\n- ${appointment.notes}" else baseContent

            scheduleNotification(
                context,
                title = context.getString(shared.string.notification_appointment_title),
                content = content,
                date = notifyDate,
                tag = "appointments"
            )
        }
    }
}

suspend fun scheduleMedicationExpirationReminders(context: Context, dataViewModel: DataViewModel) {
    val reminderOffsets = listOf(30, 14, 1)
    val expirations = dataViewModel.upcomingExpiringTreatmentDates.first()

    expirations.forEach { treatment ->
        reminderOffsets.forEach { offset ->
            val notifyDate = treatment.expirationDate.minusDays(offset.toLong())
            scheduleNotification(
                context,
                title = context.getString(shared.string.notification_expiration_title),
                content = context.getString(
                    shared.string.notification_expiration_content,
                    treatment.name,
                    treatment.expirationDate.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            .withLocale(Locale.getDefault())
                    )
                ),
                date = notifyDate,
                tag = "treatments"
            )
        }
    }
}

