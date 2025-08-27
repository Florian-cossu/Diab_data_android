package com.diabdata.ui.components.latestMeasurements.components

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.WeightEntry
import com.diabdata.utils.SvgIcon
import com.diabdata.utils.getItemShape
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class MeasureCardData(
    val titleText: String,
    val dateText: String,
    @get:DrawableRes val icon: Int,
    val trendIcon: Int? = null
)

@SuppressLint("DefaultLocale")
@Composable
fun LatestMeasures(
    viewModel: DataViewModel
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()
    val oneYearAgo = today.minusYears(1)

    val weightEntries by viewModel.recentWeights.collectAsState()
    val hba1cEntries by viewModel.recentHba1c.collectAsState()

    if (weightEntries.isEmpty() && hba1cEntries.isEmpty()) return

    val cards = buildList {
        // ---- Weight ----
        weightEntries.maxByOrNull { it.date }?.let { latest ->
            add(
                MeasureCardData(
                    titleText = String.format("%.2f kg", latest.value),
                    dateText = stringResource(
                        R.string.weight_on_date_text,
                        latest.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    ),
                    icon = R.drawable.weight_icon_vector,
                    trendIcon = computeTrendIcon(
                        entries = weightEntries,
                        oneYearAgo = oneYearAgo,
                        valueExtractor = { it.value })
                )
            )
        }

        // ---- HbA1c ----
        hba1cEntries.maxByOrNull { it.date }?.let { latest ->
            add(
                MeasureCardData(
                    titleText = String.format("%.1f%%", latest.value),
                    dateText = stringResource(
                        R.string.hba1c_on_date_text,
                        latest.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    ),
                    icon = R.drawable.hba1c_icon_vector,
                    trendIcon = computeTrendIcon(
                        entries = hba1cEntries,
                        oneYearAgo = oneYearAgo,
                        valueExtractor = { it.value })
                )
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.latest_measures_card_section_heading),
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
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SvgIcon(
                        resId = card.icon, modifier = Modifier.size(26.dp), color = primaryColor
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = card.titleText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = primaryColor, fontWeight = FontWeight.Bold
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
            if (index != cards.size - 1) {
                Spacer(Modifier.height(3.dp))
            }
        }
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
    val firstValue = valueExtractor(sorted.first()).toDouble()
    val lastValue = valueExtractor(sorted.last()).toDouble()

    return when {
        lastValue > firstValue -> R.drawable.trending_up_icon_vector
        lastValue < firstValue -> R.drawable.trending_down_icon_vector
        else -> R.drawable.trending_flat_icon_vector
    }
}
