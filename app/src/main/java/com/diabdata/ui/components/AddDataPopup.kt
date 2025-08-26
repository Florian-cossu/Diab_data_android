package com.diabdata.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.models.Appointment
import com.diabdata.models.AppointmentType
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.Treatment
import com.diabdata.models.TreatmentType
import com.diabdata.models.WeightEntry
import com.diabdata.utils.SvgIcon
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDataPopup(
    type: AddableType,
    dataViewModel: DataViewModel,
    onSubmit: (Map<String, String>) -> Unit,
    onDismiss: () -> Unit,
    prefilledTreatment: Treatment? = null
) {
    val context = LocalContext.current

    var field1 by remember { mutableStateOf(prefilledTreatment?.name ?: "") }

    var selectedDate by remember {
        mutableStateOf(
            prefilledTreatment?.expirationDate ?: LocalDate.now()
        )
    }
    var notes by remember { mutableStateOf("") }

    remember { AppointmentType.entries }
    remember { TreatmentType.entries }

    var selectedAppointmentType by remember { mutableStateOf(AppointmentType.APPOINTMENT) }
    var selectedTreatmentType by remember {
        mutableStateOf(
            prefilledTreatment?.type ?: TreatmentType.FAST_ACTING_INSULIN_VIAL
        )
    }

    val today = LocalDate.now()

    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val resId = getPopupTitleIcon(type)

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SvgIcon(resId = resId, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        context.getString(
                            R.string.add_data_popup_title, type.getDisplayName(context)
                        ).uppercase(), color = MaterialTheme.colorScheme.primary
                    )
                }

                DateSelector(
                    initialDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    isExpiryDate = (type == AddableType.TREATMENT)
                )

                when (type) {
                    AddableType.APPOINTMENT -> {
                        val context = LocalContext.current
                        EnumDropdown(
                            label = context.getString(R.string.add_data_popup_appointment_dropdown_placeholder),
                            options = AppointmentType.entries,
                            selected = selectedAppointmentType,
                            displayName = { it.displayName(context) },
                            onSelectedChange = { selectedAppointmentType = it })
                    }

                    AddableType.TREATMENT -> {
                        EnumDropdown(
                            label = context.getString(R.string.add_data_popup_medication_dropdown_placeholder),
                            options = TreatmentType.entries,
                            selected = selectedTreatmentType,
                            displayName = { it.displayName(context) },
                            onSelectedChange = { selectedTreatmentType = it })
                    }

                    else -> {}
                }

                OutlinedTextField(
                    value = field1,
                    onValueChange = {
                        field1 = it
                        when (type) {
                            AddableType.WEIGHT -> {
                                val weight = it.replace(',', '.').toFloatOrNull()
                                if (weight == null || weight < 0 || weight > 600) {
                                    isError = true
                                    errorMessage =
                                        context.getString(R.string.add_data_popup_invalid_weigth_kg_input_hint)
                                } else {
                                    isError = false
                                    errorMessage = null
                                }
                            }

                            AddableType.HBA1C -> {
                                val hba1c = it.replace(',', '.').toFloatOrNull()
                                if (hba1c == null || hba1c < 0 || hba1c > 15) {
                                    isError = true
                                    errorMessage =
                                        context.getString(R.string.add_data_popup_invalid_HBA1C_input_hint)
                                } else {
                                    isError = false
                                    errorMessage = null
                                }
                            }

                            else -> {
                                isError = false
                                errorMessage = null
                            }
                        }
                    },
                    label = {
                        Text(
                            when (type) {
                                AddableType.WEIGHT -> context.getString(R.string.add_data_popup_weight_field_placeholder)
                                AddableType.HBA1C -> context.getString(R.string.add_data_popup_HBA1C_field_placeholder)
                                AddableType.TREATMENT -> context.getString(R.string.add_data_popup_medication_field_placeholder)
                                AddableType.APPOINTMENT -> context.getString(R.string.add_data_popup_doctor_field_placeholder)
                                AddableType.DIAGNOSIS -> context.getString(R.string.add_data_popup_diagnosis_field_placeholder)
                            }
                        )
                    },
                    isError = isError,
                    supportingText = {
                        if (isError && errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = if (type == AddableType.WEIGHT || type == AddableType.HBA1C) {
                        KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    } else {
                        KeyboardOptions.Default
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                )

                Row(
                    horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text(context.getString(R.string.cancel_button_text)) }
                    Spacer(Modifier.width(8.dp))
                    val isValid = when (type) {
                        AddableType.WEIGHT -> {
                            val weight = field1.replace(',', '.').toDoubleOrNull()
                            weight != null && weight in 0.0..600.0
                        }

                        AddableType.HBA1C -> {
                            val hba1c = field1.replace(',', '.').toFloatOrNull()
                            hba1c != null && hba1c in 0f..15f
                        }

                        AddableType.TREATMENT -> field1.isNotBlank()
                        AddableType.DIAGNOSIS -> field1.isNotBlank()
                        AddableType.APPOINTMENT -> field1.isNotBlank()
                    }

                    Button(
                        onClick = {
                            val date = selectedDate ?: LocalDate.now()

                            when (type) {
                                AddableType.WEIGHT -> {
                                    val weight = field1.replace(',', '.').toFloatOrNull()
                                    if (weight != null) {
                                        dataViewModel.addWeight(
                                            WeightEntry(
                                                date = date,
                                                value = weight,
                                                isArchived = false,
                                                createdAt = today
                                            )
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.add_data_popup_invalid_weight_field_message),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                AddableType.HBA1C -> {
                                    val hba1c = field1.replace(',', '.').toFloatOrNull()
                                    if (hba1c != null) {
                                        dataViewModel.addHba1c(
                                            HBA1CEntry(
                                                date = date,
                                                value = hba1c,
                                                isArchived = false,
                                                createdAt = today
                                            )
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.add_data_popup_invalid_HBA1C_field_message),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                AddableType.TREATMENT -> {
                                    dataViewModel.addTreatment(
                                        Treatment(
                                            expirationDate = date,
                                            name = field1,
                                            type = selectedTreatmentType,
                                            isArchived = false,
                                            createdAt = today
                                        )
                                    )
                                }

                                AddableType.DIAGNOSIS -> {
                                    if (field1.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.add_data_popup_diagnosis_date_insertion_error),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        dataViewModel.addDiagnosisDate(
                                            DiagnosisDate(
                                                date = date,
                                                diagnosis = field1,
                                                isArchived = false,
                                                createdAt = today
                                            ),
                                        )
                                    }
                                }

                                AddableType.APPOINTMENT -> {
                                    dataViewModel.addAppointment(
                                        Appointment(
                                            date = date,
                                            doctor = field1,
                                            type = selectedAppointmentType,
                                            notes = notes,
                                            isArchived = false,
                                            createdAt = today
                                        )
                                    )
                                }
                            }

                            onDismiss()
                        }, enabled = isValid, colors = ButtonDefaults.buttonColors()
                    ) {
                        Text(context.getString(R.string.confirm_button_text))
                    }
                }
            }
        }
    }
}

fun getPopupTitleIcon(type: AddableType): Int = when (type) {
    AddableType.APPOINTMENT -> R.drawable.event_add_icon_vector
    AddableType.TREATMENT -> R.drawable.medication_add_icon_vector
    AddableType.WEIGHT -> R.drawable.weight_add_icon_vector
    AddableType.HBA1C -> R.drawable.hba1c_add_icon_vector
    AddableType.DIAGNOSIS -> R.drawable.diagnosis_icon_vector
}
