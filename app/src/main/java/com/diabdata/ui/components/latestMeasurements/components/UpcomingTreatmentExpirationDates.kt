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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.models.Treatment
import com.diabdata.models.TreatmentType
import com.diabdata.ui.components.ColoredIconCircle
import com.diabdata.utils.SvgIcon
import com.diabdata.utils.formatLocalDate
import com.diabdata.utils.getItemShape
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class TreatmentCardData(
    val titleText: String,
    val dateText: String,
    val icon: Int,
    val isExpiringSoon: Boolean,
    val untilDateIcon: Int,
    val type: String,
    val remainingText: String
)

@Composable
fun UpcomingTreatmentExpirationDatesContent(
    treatments: List<Treatment>
) {
    if (treatments.isEmpty()) return

    val context = LocalContext.current
    val today = LocalDate.now()
    val soonThreshold = today.plusDays(30)

    fun getUntilIconIdMap(isExpiringSoon: Boolean): Int =
        if (isExpiringSoon) R.drawable.warning_icon_vector else R.drawable.hourglass_icon_vector

    val cards = treatments.map { treatment ->
        val daysUntil = ChronoUnit.DAYS.between(today, treatment.expirationDate).toInt()
        val yearsUntil = ChronoUnit.YEARS.between(today, treatment.expirationDate).toInt()
        val totalMonths = ChronoUnit.MONTHS.between(today, treatment.expirationDate).toInt()
        val remainingMonths = totalMonths - yearsUntil * 12

        val remainingText = when {
            daysUntil == 0 -> context.getString(R.string.today)
            daysUntil in 1..29 -> pluralStringResource(R.plurals.in_days, daysUntil, daysUntil)
            yearsUntil == 0 && remainingMonths > 0 -> pluralStringResource(
                R.plurals.in_months,
                remainingMonths,
                remainingMonths
            )

            yearsUntil > 0 && remainingMonths == 0 -> pluralStringResource(
                R.plurals.in_years,
                yearsUntil,
                yearsUntil
            )

            yearsUntil > 0 && remainingMonths > 0 -> pluralStringResource(
                R.plurals.in_years_and_months,
                1,
                yearsUntil,
                remainingMonths
            )

            else -> context.getString(R.string.today)
        }

        val isExpiringSoon = daysUntil in 0..29
        val untilDateIcon = getUntilIconIdMap(isExpiringSoon)

        TreatmentCardData(
            titleText = treatment.name.ifBlank { treatment.type.displayName(context) },
            dateText = stringResource(
                R.string.expires_on_text,
                formatLocalDate(treatment.expirationDate)
            ),
            icon = treatment.type.iconRes,
            isExpiringSoon = treatment.expirationDate.isBefore(soonThreshold),
            type = treatment.type.displayName(context),
            remainingText = remainingText,
            untilDateIcon = untilDateIcon,
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.upcoming_expiration_dates_card_section_heading),
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
                        iconRes = card.icon,
                        baseColor = if (card.isExpiringSoon) MaterialTheme.colorScheme.error else AddableType.TREATMENT.baseColor,
                        size = 40.dp,
                        iconSize = 26.dp
                    )

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.titleText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (card.isExpiringSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = card.type,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = card.dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SvgIcon(
                            resId = card.untilDateIcon,
                            modifier = Modifier.size(15.dp),
                            color = if (card.isExpiringSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = card.remainingText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (card.isExpiringSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (index != cards.size - 1) Spacer(modifier = Modifier.height(3.dp))
        }
    }
}

@Composable
fun UpcomingTreatmentExpirationDates(viewModel: DataViewModel) {
    val treatments by viewModel.upcomingExpirationDates.collectAsState()
    UpcomingTreatmentExpirationDatesContent(treatments)
}

@Preview(showBackground = true)
@Composable
fun PreviewUpcomingTreatmentExpirationDates() {
    val sampleTreatments = listOf(
        Treatment(
            id = 1,
            expirationDate = LocalDate.now().plusMonths(2),
            name = "Levemir Penfill",
            createdAt = LocalDate.now().minusYears(8),
            updatedAt = LocalDate.now().minusYears(8),
            isArchived = false,
            type = TreatmentType.SLOW_ACTING_INSULIN_CARTRIDGE
        ),
        Treatment(
            id = 2,
            expirationDate = LocalDate.now().plusDays(23),
            name = "Novorapid Penfill",
            createdAt = LocalDate.now().minusYears(8),
            updatedAt = LocalDate.now().minusYears(8),
            isArchived = false,
            type = TreatmentType.FAST_ACTING_INSULIN_CARTRIDGE
        )
    )

    MaterialTheme {
        UpcomingTreatmentExpirationDatesContent(sampleTreatments)
    }
}
