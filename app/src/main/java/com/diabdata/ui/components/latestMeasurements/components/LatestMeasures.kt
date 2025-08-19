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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.R
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.WeightEntry
import com.diabdata.ui.components.latestMeasurements.MeasureSource
import com.diabdata.utils.SvgIcon
import com.diabdata.utils.getItemShape
import java.time.LocalDate

data class MeasureCardData(
    val titleText: String,
    val dateText: String,
    @DrawableRes val icon: Int,
    val trendIcon: Int? = null
)

@SuppressLint("DefaultLocale")
@Composable
fun LatestMeasures(
    sources: List<MeasureSource<*>>
) {
    if (sources.isEmpty()) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()
    val oneYearAgo = today.minusYears(1)

    val cards = sources.mapNotNull { source ->
        val latestEntry = source.entries.maxByOrNull {
            when (it) {
                is WeightEntry -> it.date
                is HBA1CEntry -> it.date
                else -> LocalDate.MIN
            }
        } ?: return@mapNotNull null

        // Entrées de l'année passée pour la tendance
        val lastYearEntries = source.entries.filter { entry ->
            when (entry) {
                is WeightEntry -> entry.date.isAfter(oneYearAgo)
                is HBA1CEntry -> entry.date.isAfter(oneYearAgo)
                else -> false
            }
        }

        val trendIcon = if (lastYearEntries.size >= 2) {
            val sortedEntries = lastYearEntries.sortedBy {
                when (it) {
                    is WeightEntry -> it.date
                    is HBA1CEntry -> it.date
                    else -> LocalDate.MIN
                }
            }
            val firstValue = when (val first = sortedEntries.first()) {
                is WeightEntry -> first.weightKg
                is HBA1CEntry -> first.value
                else -> 0.0
            }
            val lastValue = when (val last = sortedEntries.last()) {
                is WeightEntry -> last.weightKg
                is HBA1CEntry -> last.value
                else -> 0.0
            }

            when {
                lastValue.toDouble() > firstValue.toDouble() -> R.drawable.trending_up_icon_vector
                lastValue.toDouble() < firstValue.toDouble() -> R.drawable.trending_down_icon_vector
                else -> R.drawable.trending_flat_icon_vector
            }
        } else null

        @Suppress("UNCHECKED_CAST")
        val typedSource = source as MeasureSource<Any>
        MeasureCardData(
            titleText = typedSource.formatTitle(latestEntry),
            dateText = typedSource.formatDate(latestEntry),
            icon = typedSource.icon,
            trendIcon = trendIcon
        )
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
                        resId = card.icon,
                        modifier = Modifier.size(26.dp),
                        color = primaryColor
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
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
                    // Icône de tendance si dispo
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