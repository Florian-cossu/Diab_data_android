package com.diabdata.ui.components.addDataPopup.popupsByTypes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.data.converters.toEntity
import com.diabdata.models.AddableType
import com.diabdata.models.Appointment
import com.diabdata.models.AppointmentType
import com.diabdata.ui.components.EnumDropdown
import com.diabdata.ui.components.addDataPopup.BasePopupLayout
import com.diabdata.ui.components.date_components.DateSelector
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun AppointmentPopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel,
    toUpdate: DataViewModel.MixedDbEntry.AppointmentEntry? = null
) {
    var doctor by remember { mutableStateOf(toUpdate?.doctor ?: "") }
    var notes by remember { mutableStateOf(toUpdate?.notes ?: "") }
    var selectedDate by remember { mutableStateOf(toUpdate?.date ?: LocalDate.now()) }
    var selectedAppointmentType by remember {
        mutableStateOf(
            toUpdate?.type ?: AppointmentType.APPOINTMENT
        )
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()

    BasePopupLayout(
        title = context.getString(
            if (toUpdate == null) R.string.add_data_popup_title else R.string.update_data_popup_title,
            AddableType.APPOINTMENT.getDisplayName(context)
        ),
        icon = AddableType.APPOINTMENT.iconRes,
        onDismiss = onDismiss,
        onConfirm = {
            val appointmentEntry = DataViewModel.MixedDbEntry.AppointmentEntry(
                id = toUpdate?.id ?: 0,
                date = selectedDate,
                doctor = doctor,
                type = selectedAppointmentType,
                createdAt = toUpdate?.createdAt ?: today,
                isArchived = toUpdate?.isArchived ?: false,
                notes = notes,
                updatedAt = today,
                icon = AddableType.APPOINTMENT.iconRes
            )

            if (toUpdate == null) {
                dataViewModel.addAppointment(appointmentEntry.toEntity() as Appointment)
            } else {
                scope.launch {
                    dataViewModel.updateEntry(appointmentEntry)
                }
            }
            onDismiss()
        },
        isConfirmEnabled = doctor.isNotBlank()
    ) {
        DateSelector(date = selectedDate, onDateSelected = { selectedDate = it })

        EnumDropdown(
            label = context.getString(R.string.add_data_popup_appointment_dropdown_placeholder),
            options = AppointmentType.entries,
            selected = selectedAppointmentType,
            displayName = { it.displayName(context) },
            onSelectedChange = { selectedAppointmentType = it },
            iconRes = { it.iconRes }
        )

        OutlinedTextField(
            value = doctor,
            onValueChange = { doctor = it },
            label = { Text(stringResource(R.string.add_data_popup_doctor_field_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.upcoming_appointment_card_notes_header)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )
    }
}