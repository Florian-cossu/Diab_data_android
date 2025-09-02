package com.diabdata.ui.components.addDataPopup.popupsByTypes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.models.Appointment
import com.diabdata.models.AppointmentType
import com.diabdata.ui.components.EnumDropdown
import com.diabdata.ui.components.addDataPopup.BasePopupLayout
import com.diabdata.ui.components.addDataPopup.getPopupTitleIcon
import com.diabdata.ui.components.date_components.DateSelector
import java.time.LocalDate

@Composable
fun AppointmentPopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel,
    prefilled: Appointment? = null
) {
    var doctor by remember { mutableStateOf(prefilled?.doctor ?: "") }
    var notes by remember { mutableStateOf(prefilled?.notes ?: "") }
    var selectedDate by remember { mutableStateOf(prefilled?.date ?: LocalDate.now()) }
    var selectedAppointmentType by remember {
        mutableStateOf(
            prefilled?.type ?: AppointmentType.APPOINTMENT
        )
    }
    val context = LocalContext.current
    val today = LocalDate.now()

    BasePopupLayout(
        title = context.getString(
            R.string.add_data_popup_title,
            AddableType.APPOINTMENT.getDisplayName(context)
        ),
        icon = getPopupTitleIcon(AddableType.APPOINTMENT),
        onDismiss = onDismiss,
        onConfirm = {
            dataViewModel.addAppointment(
                Appointment(
                    date = selectedDate,
                    doctor = doctor,
                    type = selectedAppointmentType,
                    createdAt = today,
                    isArchived = false,
                    notes = notes,
                    updatedAt = today
                )
            )
            onDismiss()
        },
        isConfirmEnabled = doctor.isNotBlank()
    ) {
        DateSelector(
            initialDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )

        EnumDropdown(
            label = context.getString(R.string.add_data_popup_appointment_dropdown_placeholder),
            options = AppointmentType.entries,
            selected = selectedAppointmentType,
            displayName = { it.displayName(context) },
            onSelectedChange = { selectedAppointmentType = it }
        )

        OutlinedTextField(
            value = doctor,
            onValueChange = { doctor = it },
            label = { Text(stringResource(R.string.add_data_popup_doctor_field_placeholder)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.upcoming_appointment_card_notes_header)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}