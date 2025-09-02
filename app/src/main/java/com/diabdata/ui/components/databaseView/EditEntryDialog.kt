package com.diabdata.ui.components.databaseView

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.TreatmentType
import com.diabdata.ui.components.EnumDropdown
import com.diabdata.ui.components.addDataPopup.getPopupTitleIcon
import com.diabdata.ui.components.date_components.DateSelector
import com.diabdata.utils.SvgIcon
import java.time.LocalDate

@Composable
fun EditEntryDialog(
    entry: DataViewModel.MixedDbEntry,
    onDismiss: () -> Unit,
    onSave: (DataViewModel.MixedDbEntry) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
                onClick = { onDismiss() },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SvgIcon(
                        resId = getPopupTitleIcon(entry.addableType),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(
                            R.string.update_data_popup_title,
                            entry.addableType.displayNameRes
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                when (entry) {
                    is DataViewModel.MixedDbEntry.AppointmentEntry -> {
                        var doctor by remember { mutableStateOf(entry.doctor) }
                        var notes by remember { mutableStateOf(entry.notes ?: "") }
                        var date by remember { mutableStateOf(entry.date) }

                        DateSelector(
                            initialDate = entry.date,
                            onDateSelected = { date = it }
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
                            label = { Text("Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { onDismiss() },
                            ) {
                                Text(stringResource(R.string.cancel_button_text))
                            }

                            Button(
                                onClick = {
                                    onSave(
                                        entry.copy(
                                            date = date,
                                            doctor = doctor,
                                            notes = notes,
                                            updatedAt = LocalDate.now(),
                                        )
                                    )
                                }
                            ) {
                                Text(stringResource(R.string.update_button_text))
                            }
                        }
                    }

                    is DataViewModel.MixedDbEntry.ImportantDateEntry -> {
                        var diagnosis by remember { mutableStateOf(entry.importantDate) }
                        var date by remember { mutableStateOf(entry.date) }

                        DateSelector(
                            initialDate = entry.date,
                            onDateSelected = { date = it }
                        )

                        OutlinedTextField(
                            value = diagnosis,
                            onValueChange = { diagnosis = it },
                            label = { Text(stringResource(R.string.add_data_popup_important_date_field_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { onDismiss() },
                            ) {
                                Text(stringResource(R.string.cancel_button_text))
                            }

                            Button(
                                onClick = {
                                    onSave(
                                        entry.copy(
                                            date = date,
                                            importantDate = diagnosis,
                                            updatedAt = LocalDate.now()
                                        )
                                    )
                                },
                                enabled = diagnosis.isNotBlank()
                            ) {
                                Text(stringResource(R.string.update_button_text))
                            }
                        }
                    }

                    is DataViewModel.MixedDbEntry.Hba1cEntry -> {
                        var valueText by remember { mutableStateOf(entry.value.toString()) }
                        var value by remember { mutableFloatStateOf(entry.value) }

                        var date by remember { mutableStateOf(entry.date) }

                        var isError by remember { mutableStateOf(false) }
                        var errorMessage by remember { mutableStateOf<String?>(null) }

                        DateSelector(
                            initialDate = entry.date,
                            onDateSelected = { date = it }
                        )

                        OutlinedTextField(
                            value = valueText,
                            onValueChange = {
                                valueText = it
                                val parsed = it.replace(',', '.').toFloatOrNull()
                                if (parsed == null || parsed < 0 || parsed > 15) {
                                    isError = true
                                    errorMessage =
                                        context.getString(R.string.add_data_popup_invalid_HBA1C_input_hint)
                                } else {
                                    isError = false
                                    errorMessage = null
                                    value = parsed
                                }
                            },
                            label = { Text(stringResource(R.string.add_data_popup_HBA1C_field_placeholder)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            isError = isError,
                            supportingText = {
                                if (isError && errorMessage != null) {
                                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )


                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { onDismiss() },
                            ) {
                                Text(stringResource(R.string.cancel_button_text))
                            }

                            Button(
                                onClick = {
                                    onSave(
                                        entry.copy(
                                            date = date,
                                            value = value,
                                            updatedAt = LocalDate.now()
                                        )
                                    )
                                },
                                enabled = !isError
                            ) {
                                Text(stringResource(R.string.update_button_text))
                            }
                        }
                    }

                    is DataViewModel.MixedDbEntry.TreatmentEntry -> {
                        var name by remember { mutableStateOf(entry.name) }
                        var treatmentType by remember { mutableStateOf(entry.treatmentType) }
                        var date by remember { mutableStateOf(entry.date) }

                        DateSelector(
                            initialDate = entry.date,
                            onDateSelected = { date = it },
                            isExpiryDate = true
                        )

                        EnumDropdown(
                            label = context.getString(R.string.add_data_popup_medication_dropdown_placeholder),
                            options = TreatmentType.entries,
                            selected = treatmentType,
                            displayName = { it.displayName(context) },
                            onSelectedChange = { treatmentType = it }
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.add_data_popup_medication_field_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { onDismiss() },
                            ) {
                                Text(stringResource(R.string.cancel_button_text))
                            }

                            Button(
                                onClick = {
                                    onSave(
                                        entry.copy(
                                            date = date,
                                            name = name,
                                            treatmentType = treatmentType,
                                            updatedAt = LocalDate.now()
                                        )
                                    )
                                }
                            ) {
                                Text(stringResource(R.string.update_button_text))
                            }
                        }
                    }

                    is DataViewModel.MixedDbEntry.WeightEntry -> {
                        var valueText by remember { mutableStateOf(entry.value.toString()) }
                        var value by remember { mutableFloatStateOf(entry.value) }

                        var date by remember { mutableStateOf(entry.date) }

                        var isError by remember { mutableStateOf(false) }
                        var errorMessage by remember { mutableStateOf<String?>(null) }

                        DateSelector(
                            initialDate = entry.date,
                            onDateSelected = { date = it }
                        )

                        OutlinedTextField(
                            value = valueText,
                            onValueChange = {
                                valueText = it
                                val parsed = it.replace(',', '.').toFloatOrNull()
                                if (parsed == null || parsed < 0 || parsed > 600) {
                                    isError = true
                                    errorMessage =
                                        context.getString(R.string.add_data_popup_invalid_weight_kg_input_hint)
                                } else {
                                    isError = false
                                    errorMessage = null
                                    value = parsed
                                }
                            },
                            label = { Text(stringResource(R.string.add_data_popup_weight_field_placeholder)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            isError = isError,
                            supportingText = {
                                if (isError && errorMessage != null) {
                                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { onDismiss() },
                            ) {
                                Text(stringResource(R.string.cancel_button_text))
                            }

                            Button(
                                onClick = {
                                    onSave(
                                        entry.copy(
                                            date = date,
                                            value = value,
                                            updatedAt = LocalDate.now()
                                        )
                                    )
                                },
                                enabled = !isError
                            ) {
                                Text(stringResource(R.string.update_button_text))
                            }
                        }
                    }
                }
            }
        }
    }
}