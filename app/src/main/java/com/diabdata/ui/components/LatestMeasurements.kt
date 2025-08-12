package com.diabdata.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.R
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.WeightEntry
import com.diabdata.utils.SvgIcon
import java.time.format.DateTimeFormatter

@SuppressLint("DefaultLocale")
@Composable
fun LatestMeasurements(
    weightEntries: List<WeightEntry> = emptyList(),
    hba1cEntries: List<HBA1CEntry> = emptyList(),
    diagnosisEntries: List<DiagnosisDate> = emptyList()
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ImportantDatesList(diagnosisEntries)
        LatestMeasures(weightEntries, hba1cEntries)
    }
}

@Composable
fun ImportantDatesList(diagnosisEntries: List<DiagnosisDate>) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val primaryColor = MaterialTheme.colorScheme.primary

    if (diagnosisEntries.isEmpty()) {
        return
    }

    Column {
        Text(
            text = "Dates importantes",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
            color = MaterialTheme.colorScheme.surfaceTint
        )
        Spacer(Modifier.height(8.dp))

        diagnosisEntries.forEach { diagnosis ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SvgIcon(
                        resId = (R.drawable.diagnosis_icon_vector),
                        modifier = Modifier.size(40.dp),
                        color = primaryColor
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = diagnosis.diagnosis,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Diagnostiqué le ${diagnosis.date.format(formatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun LatestMeasures(
    weightEntries: List<WeightEntry>,
    hba1cEntries: List<HBA1CEntry>
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val primaryColor = MaterialTheme.colorScheme.primary

    if (weightEntries.isEmpty() && hba1cEntries.isEmpty()) {
        return
    }

    Column {
        Text(
            text = "Dernières mesures",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
            color = MaterialTheme.colorScheme.surfaceTint
        )

        Spacer(Modifier.height(8.dp))

        weightEntries.maxByOrNull { it.date }?.let { weight ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SvgIcon(
                        resId = (R.drawable.weight_icon_vector),
                        modifier = Modifier.size(40.dp),
                        color = primaryColor
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = String.format("%.2f kg", weight.weightKg),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Pesée réalisée le ${weight.date.format(formatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        hba1cEntries.maxByOrNull { it.date }?.let { hba1c ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SvgIcon(
                        resId = (R.drawable.hba1c_icon_vector),
                        modifier = Modifier.size(40.dp),
                        color = primaryColor
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = String.format("%.1f%%", hba1c.value),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "HBA1C mesurée le ${hba1c.date.format(formatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
