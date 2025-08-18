package com.diabdata.ui.components.latestMeasurements.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.R
import com.diabdata.models.DiagnosisDate
import com.diabdata.utils.SvgIcon
import com.diabdata.utils.getItemShape
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun ImportantDatesList(diagnosisEntries: List<DiagnosisDate>) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val primaryColor = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()

    val context = LocalContext.current

    if (diagnosisEntries.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Text(
            text = stringResource(R.string.important_dates_card_section_heading),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
            color = MaterialTheme.colorScheme.surfaceTint
        )

        Spacer(Modifier.height(8.dp))

        diagnosisEntries.forEachIndexed { index, diagnosis ->
            val years = ChronoUnit.YEARS.between(diagnosis.date, today)
            val monthsTotal = ChronoUnit.MONTHS.between(diagnosis.date, today)
            val remainingMonths = monthsTotal - years * 12

            val elapsedText = when {
                years == 0L && remainingMonths == 0L -> context.getString(R.string.today)
                years == 0L -> context.resources.getQuantityString(
                    R.plurals.months,
                    remainingMonths.toInt(),
                    remainingMonths
                )

                remainingMonths == 0L -> context.resources.getQuantityString(
                    R.plurals.years,
                    years.toInt(),
                    years
                )

                else -> context.resources.getQuantityString(
                    R.plurals.years_and_months,
                    years.toInt(),
                    years, remainingMonths
                )
            }

            Surface(
                shape = getItemShape(index, diagnosisEntries.size),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icône à gauche
                    SvgIcon(
                        resId = R.drawable.diagnosis_icon_vector,
                        modifier = Modifier.size(26.dp),
                        color = primaryColor
                    )
                    Spacer(Modifier.width(16.dp))

                    // Texte principal
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = diagnosis.diagnosis,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = context.resources.getString(
                                R.string.diagnosed_on_text,
                                diagnosis.date.format(formatter)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Temps écoulé à droite
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SvgIcon(
                            resId = R.drawable.event_icon_vector,
                            modifier = Modifier.size(15.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = elapsedText,
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