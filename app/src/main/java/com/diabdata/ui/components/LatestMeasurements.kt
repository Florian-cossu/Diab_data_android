package com.diabdata.ui.components

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        val sources = listOf(
            MeasureSource(
                entries = weightEntries,
                icon = R.drawable.weight_icon_vector,
                formatTitle = { w: WeightEntry -> String.format("%.2f kg", w.weightKg) },
                formatDate = { w: WeightEntry -> "Pesée réalisée le ${w.date.format(formatter)}" }
            ),
            MeasureSource(
                entries = hba1cEntries,
                icon = R.drawable.hba1c_icon_vector,
                formatTitle = { h: HBA1CEntry -> String.format("%.1f%%", h.value) },
                formatDate = { h: HBA1CEntry -> "HBA1C mesurée le ${h.date.format(formatter)}" }
            )
        )


        ImportantDatesList(diagnosisEntries)
        Spacer(modifier = Modifier.height(8.dp))
        LatestMeasures(sources)
    }
}

@Composable
fun ImportantDatesList(diagnosisEntries: List<DiagnosisDate>) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val primaryColor = MaterialTheme.colorScheme.primary


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Text(
            text = "Dates importantes",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
            color = MaterialTheme.colorScheme.surfaceTint
        )

        Spacer(Modifier.height(8.dp))

        diagnosisEntries.forEachIndexed { index, diagnosis ->
            Surface(
                shape = getItemShape(index, diagnosisEntries.size),
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
                        resId = R.drawable.diagnosis_icon_vector,
                        modifier = Modifier.size(25.dp),
                        color = primaryColor
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = diagnosis.diagnosis,
                            style = MaterialTheme.typography.titleMedium.copy(
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
            if (index != diagnosisEntries.size - 1) {
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}

data class MeasureCardData(
    val titleText: String,
    val dateText: String,
    @DrawableRes val icon: Int
)

data class MeasureSource<T>(
    val entries: List<T>,
    val icon: Int,
    val formatTitle: (T) -> String,
    val formatDate: (T) -> String
)

@SuppressLint("DefaultLocale")
@Composable
fun LatestMeasures(
    sources: List<MeasureSource<*>>
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    val cards = sources.mapNotNull { source ->
        source.entries
            .mapNotNull { entry ->
                when (entry) {
                    is WeightEntry -> entry to entry.date
                    is HBA1CEntry -> entry to entry.date
                    else -> null
                }
            }
            .maxByOrNull { it.second }
            ?.first
            ?.let { latest ->
                @Suppress("UNCHECKED_CAST")
                val typedSource = source as MeasureSource<Any>
                MeasureCardData(
                    titleText = typedSource.formatTitle(latest),
                    dateText = typedSource.formatDate(latest),
                    icon = typedSource.icon
                )
            }
    }

    if (cards.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Dernières mesures",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
            color = MaterialTheme.colorScheme.surfaceTint
        )
        Spacer(Modifier.height(8.dp))

        cards.forEachIndexed { index, card ->
            Surface(
                shape = getItemShape(index, cards.size),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SvgIcon(
                        resId = card.icon,
                        modifier = Modifier.size(25.dp),
                        color = primaryColor
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = card.titleText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = card.dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (index != cards.size - 1) {
                Spacer(Modifier.height(3.dp))
            }
        }
    }
}

fun getItemShape(index: Int, size: Int): Shape {
    if (size == 1) {
        return RoundedCornerShape(16.dp)
    }
    return when (index) {
        0 -> RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 3.dp,
            bottomEnd = 3.dp
        )

        size - 1 -> RoundedCornerShape(
            topStart = 3.dp,
            topEnd = 3.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )

        else -> RoundedCornerShape(3.dp)
    }
}
