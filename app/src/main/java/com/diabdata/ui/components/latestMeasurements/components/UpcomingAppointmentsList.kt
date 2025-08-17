package com.diabdata.ui.components.latestMeasurements.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.R
import com.diabdata.models.Appointment
import com.diabdata.utils.SvgIcon
import com.diabdata.utils.getItemShape
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun UpcomingAppointmentsList(appointments: List<Appointment>) {
    val context = LocalContext.current   // <-- le bon context Android

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val primaryColor = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()

    val upcomingAppointments = appointments.filter { it.date.isAfter(today) }
        .sortedBy { it.date }

    if (upcomingAppointments.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Text(
            text = stringResource(R.string.upcoming_appointment_card_section_heading),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
            color = MaterialTheme.colorScheme.surfaceTint
        )

        Spacer(Modifier.height(8.dp))

        upcomingAppointments.forEachIndexed { index, appointment ->
            val daysUntil = ChronoUnit.DAYS.between(today, appointment.date)
            val monthsUntil = ChronoUnit.MONTHS.between(today, appointment.date)
            val yearsUntil = ChronoUnit.YEARS.between(today, appointment.date)

            val remainingText = when {
                daysUntil == 0L -> "Aujourd’hui"
                daysUntil in 1..6 -> "Dans $daysUntil jour${if (daysUntil > 1) "s" else ""}"
                yearsUntil == 0L -> "Dans $monthsUntil mois"
                else -> "Dans $yearsUntil an${if (yearsUntil > 1) "s" else ""}"
            }

            Surface(
                shape = getItemShape(index, upcomingAppointments.size),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconResId = when (appointment.type.name) {
                        "APPOINTMENT" -> R.drawable.stethoscope_icon_vector
                        "ANNUAL_CHECKUP" -> R.drawable.recurring_event_icon_vector
                        else -> R.drawable.event_icon_vector
                    }

                    SvgIcon(
                        resId = iconResId,
                        modifier = Modifier.size(26.dp),
                        color = primaryColor
                    )

                    Spacer(Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${appointment.doctor} - (${appointment.type.displayName(context)})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Le ${appointment.date.format(formatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SvgIcon(
                            resId = R.drawable.hourglass_icon_vector,
                            modifier = Modifier.size(15.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = remainingText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (index != upcomingAppointments.size - 1) {
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}