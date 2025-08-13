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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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

    // Déclare ces états UNE SEULE FOIS (pas dans le if)
    val appointmentTypes =
        remember { AppointmentType.entries } // ou AppointmentType.entries si dispo
    var selectedType by remember { mutableStateOf(AppointmentType.APPOINTMENT) }
    var expanded by remember { mutableStateOf(false) }

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
                Text("Ajouter une entrée pour $type")

                DateSelector(
                    initialDate = LocalDate.now(),
                    onDateSelected = { selectedDate = it }
                )

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

                if (type == AddableType.APPOINTMENT) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedType.displayName,   // <-- nécessite enum avec displayName
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type de RDV") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier
                                .menuAnchor()                    // <-- important !
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            appointmentTypes.forEach { typeOption ->
                                DropdownMenuItem(
                                    text = { Text(typeOption.displayName) },
                                    onClick = {
                                        selectedType = typeOption
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optionnel)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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
                                    Treatment(expirationDate = date, name = field1)
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
                                        type = selectedType,   // <-- l’état sélectionné ici fonctionne
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