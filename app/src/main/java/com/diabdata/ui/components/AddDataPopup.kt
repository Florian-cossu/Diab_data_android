package com.diabdata.ui.components

import DateSelector
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
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.models.Appointment
import com.diabdata.models.AppointmentType
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.Treatment
import com.diabdata.models.TreatmentType
import com.diabdata.models.WeightEntry
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDataPopup(
    type: AddableType,
    dataViewModel: DataViewModel,
    onSubmit: (Map<String, String>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var field1 by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var notes by remember { mutableStateOf("") }

    remember { AppointmentType.entries }
    remember { TreatmentType.entries }
    var selectedAppointmentType by remember { mutableStateOf(AppointmentType.APPOINTMENT) }
    var selectedTreatmentType by remember { mutableStateOf(TreatmentType.FAST_ACTING_RAPID_VIAL) }

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
                Text("Ajouter ${type.getDisplayName(context)}")

                DateSelector(
                    initialDate = LocalDate.now(),
                    onDateSelected = { selectedDate = it }
                )

                when (type) {
                    AddableType.APPOINTMENT -> {
                        val context = LocalContext.current
                        EnumDropdown(
                            label = "Type de RDV",
                            options = AppointmentType.entries,
                            selected = selectedAppointmentType,
                            displayName = { it.displayName(context) },
                            onSelectedChange = { selectedAppointmentType = it }
                        )
                    }

                    AddableType.TREATMENT -> {
                        EnumDropdown(
                            label = "Type de traitement",
                            options = TreatmentType.entries,
                            selected = selectedTreatmentType,
                            displayName = { it.displayName(context) },
                            onSelectedChange = { selectedTreatmentType = it }
                        )
                    }

                    else -> {}
                }

                OutlinedTextField(
                    value = field1,
                    onValueChange = { field1 = it },
                    label = {
                        Text(
                            when (type) {
                                AddableType.WEIGHT -> "Poids (kg)"
                                AddableType.HBA1C -> "HBA1C (%)"
                                AddableType.TREATMENT -> "Nom du traitement"
                                AddableType.APPOINTMENT -> "Nom du praticien"
                                AddableType.DIAGNOSIS -> "Affection diagnostiquée"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val date = selectedDate ?: LocalDate.now()

                        when (type) {
                            AddableType.WEIGHT -> {
                                val weight = field1.replace(',', '.').toDoubleOrNull()
                                if (weight != null) {
                                    dataViewModel.addWeight(WeightEntry(date = date, weightKg = weight))
                                } else {
                                    Toast.makeText(context, "Poids invalide", Toast.LENGTH_SHORT).show()
                                }
                            }
                            AddableType.HBA1C -> {
                                val hba1c = field1.replace(',', '.').toFloatOrNull()
                                if (hba1c != null) {
                                    dataViewModel.addHba1c(HBA1CEntry(date = date, value = hba1c))
                                } else {
                                    Toast.makeText(context, "HBA1C invalide", Toast.LENGTH_SHORT).show()
                                }
                            }
                            AddableType.TREATMENT -> {
                                dataViewModel.addTreatment(
                                    Treatment(
                                        expirationDate = date,
                                        name = field1,
                                        type = selectedTreatmentType
                                    )
                                )
                            }
                            AddableType.DIAGNOSIS -> {
                                if (field1.isBlank()) {
                                    Toast.makeText(context, "Bug d'ajout du diagnostic", Toast.LENGTH_SHORT).show()
                                } else {
                                    dataViewModel.addDiagnosisDate(
                                        DiagnosisDate(date = date, diagnosis = field1)
                                    )
                                }
                            }
                            AddableType.APPOINTMENT -> {
                                dataViewModel.addAppointment(
                                    Appointment(
                                        date = date,
                                        doctor = field1,
                                        type = selectedAppointmentType,
                                        notes = notes
                                    )
                                )
                            }
                        }

                        onDismiss()
                    }) {
                        Text("Valider")
                    }
                }
            }
        }
    }
}