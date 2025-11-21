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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diabdata.data.DataViewModel
import com.diabdata.models.Appointment
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dataTypes.AppointmentType
import com.diabdata.shared.utils.dateUtils.formatLocalDate
import com.diabdata.shared.utils.dateUtils.toRelativeString
import com.diabdata.ui.components.ColoredIconCircle
import com.diabdata.ui.components.layout.SvgIcon
import com.diabdata.utils.getItemShape
import java.time.LocalDate
import com.diabdata.shared.R as shared

@Composable
fun UpcomingAppointmentsListContent(
    upcomingAppointments: List<Appointment>
) {
    if (upcomingAppointments.isEmpty()) return

    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Text(
            text = stringResource(shared.string.home_section_upcoming_appointments),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.surfaceTint
        )

        Spacer(Modifier.height(8.dp))

        upcomingAppointments.forEachIndexed { index, appointment ->
            val remainingText = appointment.date.toRelativeString(context)

            Surface(
                shape = getItemShape(index, upcomingAppointments.size),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColoredIconCircle(
                        iconRes = appointment.type.iconRes,
                        baseColor = AddableType.APPOINTMENT.baseColor,
                        size = 40.dp,
                        iconSize = 25.dp
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
                                    onClick = {
                                        showNotesDialog = true
                                    },
                                    modifier = Modifier.size(22.dp),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    SvgIcon(
                                        resId = shared.drawable.note_icon_vector,
                                        modifier = Modifier.size(18.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (showNotesDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showNotesDialog = false },
                                        icon = {
                                            SvgIcon(
                                                resId = shared.drawable.note_icon_vector,
                                                modifier = Modifier.size(48.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        title = {
                                            Text(
                                                text = stringResource(shared.string.appointment_card_notes_header),
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
                                            TextButton(onClick = {
                                                showNotesDialog = false
                                            }) { Text(text = stringResource(shared.string.action_confirm)) }
                                        })
                                }
                            }

                            Text(
                                text = stringResource(
                                    shared.string.scheduled_on_date_text,
                                    formatLocalDate(appointment.date)
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SvgIcon(
                            resId = shared.drawable.hourglass_icon_vector,
                            modifier = Modifier.size(15.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text =
                                remainingText,
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

@Composable
fun UpcomingAppointmentsList(viewModel: DataViewModel) {
    val upcomingAppointments by viewModel.upcomingAppointment.collectAsState()
    UpcomingAppointmentsListContent(upcomingAppointments)
}

@Preview(showBackground = true)
@Composable
fun PreviewUpcomingAppointmentsList() {
    val sampleAppointments = listOf(
        Appointment(
            doctor = "Dr. Dupont",
            type = AppointmentType.APPOINTMENT,
            date = LocalDate.now().plusDays(5),
            notes = "Rappel vaccin",
            id = 1,
            createdAt = LocalDate.of(2015, 3, 1),
            isArchived = false,
            updatedAt = LocalDate.of(2015, 4, 2),
        ),
        Appointment(
            doctor = "Dr. Martin",
            type = AppointmentType.ANNUAL_CHECKUP,
            date = LocalDate.now().plusMonths(2),
            notes = null,
            id = 2,
            createdAt = LocalDate.of(2015, 3, 1),
            isArchived = false,
            updatedAt = LocalDate.of(2015, 4, 2),
        )
    )

    MaterialTheme {
        UpcomingAppointmentsListContent(upcomingAppointments = sampleAppointments)
    }
}