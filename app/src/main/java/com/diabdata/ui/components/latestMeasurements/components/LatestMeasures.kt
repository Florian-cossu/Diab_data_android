package com.diabdata.ui.components.latestMeasurements.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.WeightEntry
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dateUtils.formatLocalDate
import com.diabdata.ui.components.ColoredIconCircle
import com.diabdata.ui.components.layout.SvgIcon
import com.diabdata.utils.ui.getItemShape
import java.time.LocalDate
import java.util.Locale
import com.diabdata.shared.R as shared

data class MeasureCardData(
    val textColor: Color,
    val titleText: String,
    val dateText: String,
    val addableType: AddableType,
    val trendIcon: Int? = null
)

@Composable
fun LatestMeasuresContent(
    weightEntries: List<WeightEntry>,
    hba1cEntries: List<HBA1CEntry>
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()
    val oneYearAgo = today.minusYears(1)

    if (weightEntries.isEmpty() && hba1cEntries.isEmpty()) return

    val cards = buildList {
        weightEntries.maxByOrNull { it.date }?.let { latest ->
            add(
                MeasureCardData(
                    textColor = primaryColor,
                    titleText = String.format(Locale.getDefault(), "%.2f kg", latest.value),
                    dateText = stringResource(
                        shared.string.weight_on_date_text,
                        formatLocalDate(latest.date)
                    ),
                    addableType = AddableType.WEIGHT,
                    trendIcon = computeTrendIcon(
                        entries = weightEntries,
                        oneYearAgo = oneYearAgo,
                        valueExtractor = { it.value }
                    )
                )
            )
        }

        hba1cEntries.maxByOrNull { it.date }?.let { latest ->
            add(
                MeasureCardData(
                    textColor = when {
                        latest.value in 7.5..8.5 -> colorResource(R.color.archived_primary)
                        latest.value >= 8.6 -> MaterialTheme.colorScheme.error
                        else -> primaryColor
                    },
                    titleText = String.format(Locale.getDefault(), "%.1f%%", latest.value),
                    dateText = stringResource(
                        shared.string.hba1c_on_date_text,
                        formatLocalDate(latest.date)
                    ),
                    addableType = AddableType.HBA1C,
                    trendIcon = computeTrendIcon(
                        entries = hba1cEntries,
                        oneYearAgo = oneYearAgo,
                        valueExtractor = { it.value }
                    )
                )
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(shared.string.home_section_latest_measures),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.surfaceTint
        )
        Spacer(Modifier.height(8.dp))

        cards.forEachIndexed { index, card ->
            Surface(
                shape = getItemShape(index, cards.size),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColoredIconCircle(
                        iconRes = card.addableType.iconRes,
                        baseColor = if (card.textColor != primaryColor) card.textColor else card.addableType.baseColor,
                        size = 40.dp,
                        iconSize = 25.dp
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.titleText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = card.textColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = card.dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    card.trendIcon?.let { iconRes ->
                        SvgIcon(
                            resId = iconRes,
                            modifier = Modifier.size(15.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (index != cards.size - 1) Spacer(Modifier.height(3.dp))
        }
    }
}

@Composable
fun LatestMeasures(viewModel: DataViewModel) {
    val weightEntries by viewModel.recentWeights.collectAsState()
    val hba1cEntries by viewModel.recentHba1c.collectAsState()
    LatestMeasuresContent(weightEntries, hba1cEntries)
}

@Preview(showBackground = true)
@Composable
fun PreviewLatestMeasures() {
    val sampleWeights = listOf(
        WeightEntry(
            date = LocalDate.now().minusDays(3),
            value = 88.4f,
            id = 1,
            createdAt = LocalDate.of(2015, 3, 1),
            isArchived = false,
            updatedAt = LocalDate.of(2015, 4, 2),
        ),
        WeightEntry(
            date = LocalDate.now().minusDays(10),
            value = 83.7f,
            id = 2,
            createdAt = LocalDate.of(2015, 3, 1),
            isArchived = false,
            updatedAt = LocalDate.of(2015, 4, 2),
        )
    )
    val sampleHba1c = listOf(
        HBA1CEntry(
            date = LocalDate.now().minusMonths(2),
            value = 6.8f,
            id = 3,
            createdAt = LocalDate.of(2015, 3, 1),
            isArchived = false,
            updatedAt = LocalDate.of(2015, 4, 2),
        ),
        HBA1CEntry(
            date = LocalDate.now().minusMonths(6),
            value = 7.2f,
            id = 4,
            createdAt = LocalDate.of(2015, 3, 1),
            isArchived = false,
            updatedAt = LocalDate.of(2015, 4, 2),
        )
    )

    MaterialTheme {
        LatestMeasuresContent(
            weightEntries = sampleWeights,
            hba1cEntries = sampleHba1c
        )
    }
}

private fun <T> computeTrendIcon(
    entries: List<T>,
    oneYearAgo: LocalDate,
    valueExtractor: (T) -> Number,
    dateExtractor: (T) -> LocalDate = {
        when (it) {
            is WeightEntry -> it.date
            is HBA1CEntry -> it.date
            else -> LocalDate.MIN
        }
    }
): Int? {
    val lastYearEntries = entries.filter { dateExtractor(it).isAfter(oneYearAgo) }
    if (lastYearEntries.size < 2) return null

    val sorted = lastYearEntries.sortedBy { dateExtractor(it) }
    val firstValue = valueExtractor(sorted.first()).toFloat()
    val lastValue = valueExtractor(sorted.last()).toFloat()

    return when {
        lastValue > firstValue -> shared.drawable.trending_up_icon_vector
        lastValue < firstValue -> shared.drawable.trending_down_icon_vector
        else -> shared.drawable.trending_flat_icon_vector
    }
}
