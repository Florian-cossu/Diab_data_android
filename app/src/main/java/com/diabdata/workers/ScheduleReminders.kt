package com.diabdata.workers

import android.content.Context
import com.diabdata.R
import com.diabdata.data.DataViewModel
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

suspend fun scheduleAllReminders(context: Context, dataViewModel: DataViewModel) {
    val reminderOffsets = listOf(30, 14, 1)

    val appointments = dataViewModel.upcomingAppointment.first()
    val expirations = dataViewModel.upcomingExpirationDates.first()

    appointments.forEach { appointment ->
        reminderOffsets.forEach { offset ->
            val notifyDate = appointment.date.minusDays(offset.toLong())

            val baseContent = context.getString(
                R.string.appointment_reminder_notification_content,
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
                title = context.getString(R.string.appointment_reminder_notification_title),
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
                title = context.getString(R.string.medication_expiration_reminder_notification_title),
                content = context.getString(
                    R.string.medication_expiration_reminder_notification_content,
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
                R.string.appointment_reminder_notification_content,
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
                title = context.getString(R.string.appointment_reminder_notification_title),
                content = content,
                date = notifyDate,
                tag = "appointments"
            )
        }
    }
}

suspend fun scheduleMedicationExpirationReminders(context: Context, dataViewModel: DataViewModel) {
    val reminderOffsets = listOf(30, 14, 1)
    val expirations = dataViewModel.upcomingExpirationDates.first()

    expirations.forEach { treatment ->
        reminderOffsets.forEach { offset ->
            val notifyDate = treatment.expirationDate.minusDays(offset.toLong())
            scheduleNotification(
                context,
                title = context.getString(R.string.medication_expiration_reminder_notification_title),
                content = context.getString(
                    R.string.medication_expiration_reminder_notification_content,
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

// Reminder when user insert new data
fun scheduleNewReminder(
    context: Context,
    date: LocalDate,
    titleResId: Int,
    content: String,
    tag: String,
    reminderOffsets: List<Long> = listOf(30, 14, 1)
) {
    reminderOffsets.forEach { offset ->
        val notifyDate = date.minusDays(offset)
        scheduleNotification(
            context,
            title = context.getString(titleResId),
            content = content,
            date = notifyDate,
            tag = tag
        )
    }
}
