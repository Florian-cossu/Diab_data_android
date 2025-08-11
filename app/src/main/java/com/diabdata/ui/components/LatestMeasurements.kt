package com.diabdata.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.WeightEntry
import com.diabdata.ui.utils.SvgIcon
import java.time.format.DateTimeFormatter

@SuppressLint("DefaultLocale")
@Composable
fun LatestMeasurements(
    weightEntries: List<WeightEntry>,
    hba1cEntries: List<HBA1CEntry>
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val latestWeight = weightEntries.maxByOrNull { it.date }
    val latestHba1c = hba1cEntries.maxByOrNull { it.date }
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dernières mesures",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
            color = MaterialTheme.colorScheme.surfaceTint
        )

        latestWeight?.let { weight ->
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
                        name = "weight_icon_vector",
                        modifier = Modifier.size(40.dp),
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = String.format("%.2f kg", weight.weightKg),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = primaryColor,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Le ${weight.date.format(formatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        latestHba1c?.let { hba1c ->
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
                        name = "hba1c_icon_vector",
                        modifier = Modifier.size(40.dp),
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = String.format("%.1f %%", hba1c.value),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = primaryColor,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Le ${hba1c.date.format(formatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}