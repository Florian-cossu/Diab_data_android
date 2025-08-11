package com.diabdata.ui.components

import DateSelector
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry
import com.diabdata.ui.utils.showNotification
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

                // Fields
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
                                AddableType.APPOINTMENT -> "Date / Heure"
                                AddableType.DIAGNOSIS -> "Date de diagnostic"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (type == AddableType.APPOINTMENT) {
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val date = selectedDate ?: LocalDate.now()

                        when (type) {
                            AddableType.WEIGHT -> {
                                val weight = field1.replace(',', '.').toDoubleOrNull()
                                if(weight != null) {
                                    dataViewModel.addWeight(WeightEntry(date = date, weightKg = weight))
                                } else {
                                    Toast.makeText(context, "Poids invalide", Toast.LENGTH_SHORT).show()
                                }
                            }
                            AddableType.HBA1C -> {
                                val hba1c = field1.replace(',', '.').toFloatOrNull()
                                if(hba1c != null) {
                                    dataViewModel.addHba1c(HBA1CEntry(date = date, value = hba1c))
                                } else {
                                    Toast.makeText(context, "HBA1C invalide", Toast.LENGTH_SHORT).show()
                                }
                            }
                            AddableType.TREATMENT -> {
                                dataViewModel.addTreatment(
                                    Treatment(
                                        expirationDate = date,
                                        name = field1
                                    )
                                )
                            }
                            AddableType.DIAGNOSIS -> {
                                if (field1.isBlank()) {
                                    Toast.makeText(context, "Bug d'ajout du diagnostic", Toast.LENGTH_SHORT).show()
                                } else {
                                    dataViewModel.addDiagnosisDate(
                                        DiagnosisDate(
                                            date = date,
                                            diagnosis = field1,
                                        )
                                    )
                                }
                            }
                            AddableType.APPOINTMENT -> {
                                dataViewModel.addAppointment(
                                    Appointment(
                                        date = date,
                                        doctor = "",
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

// Helper to build the map
private fun buildDataMap(
    type: AddableType,
    field1: String,
    selectedDate: LocalDate?,
    notes: String
): Map<String, String> {
    val data = mutableMapOf<String, String>()
    data["field1"] = field1
    selectedDate?.let {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        data["date"] = it.format(formatter)
    }
    if (type == AddableType.APPOINTMENT && notes.isNotBlank()) {
        data["notes"] = notes
    }
    return data
}