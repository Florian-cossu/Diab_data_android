package com.diabdata.ui.components.latestMeasurements.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.utils.SvgIcon
import com.diabdata.utils.getItemShape
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun UpcomingAppointmentsList(
    viewModel: DataViewModel
) {
    val upcomingAppointments by viewModel.upcomingAppointment.collectAsState()

    if (upcomingAppointments.isEmpty()) return

    val context = LocalContext.current

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val primaryColor = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()

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
            val daysUntil = ChronoUnit.DAYS.between(today, appointment.date).toInt()
            val yearsUntil = ChronoUnit.YEARS.between(today, appointment.date).toInt()
            val totalMonths = ChronoUnit.MONTHS.between(today, appointment.date).toInt()
            val remainingMonths = totalMonths - yearsUntil * 12

            val remainingText = when {
                daysUntil == 0 -> context.getString(R.string.today)
                daysUntil in 1..29 -> pluralStringResource(
                    R.plurals.in_days,
                    daysUntil,
                    daysUntil
                )

                yearsUntil == 0 && remainingMonths > 0 -> pluralStringResource(
                    R.plurals.in_months,
                    remainingMonths,
                    remainingMonths
                )

                yearsUntil > 0 && remainingMonths == 0 -> pluralStringResource(
                    R.plurals.in_years,
                    yearsUntil,
                    yearsUntil
                )

                yearsUntil > 0 && remainingMonths > 0 -> pluralStringResource(
                    R.plurals.in_years_and_months,
                    1,
                    yearsUntil,
                    remainingMonths
                )

                else -> context.getString(R.string.today)
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (!appointment.notes.isNullOrBlank()) {
                                var showNotesDialog by remember { mutableStateOf(false) }

                                IconButton(
                                    onClick = { showNotesDialog = true },
                                    modifier = Modifier.size(22.dp),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    SvgIcon(
                                        resId = R.drawable.note_icon_vector,
                                        modifier = Modifier.size(18.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (showNotesDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showNotesDialog = false },
                                        icon = {
                                            SvgIcon(
                                                resId = R.drawable.note_icon_vector,
                                                modifier = Modifier.size(48.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        title = {
                                            Text(
                                                text = stringResource(R.string.upcoming_appointment_card_notes_header),
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        },
                                        text = {
                                            Text(
                                                text = appointment.notes,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        confirmButton = {
                                            TextButton(onClick = { showNotesDialog = false }) {
                                                Text(text = stringResource(R.string.confirm_button_text))
                                            }
                                        }
                                    )
                                }
                            }

                            Text(
                                text = stringResource(
                                    R.string.scheduled_on_date_text,
                                    appointment.date.format(formatter)
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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