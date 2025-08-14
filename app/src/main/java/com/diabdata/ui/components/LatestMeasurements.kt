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
import com.diabdata.models.Appointment
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.WeightEntry
import com.diabdata.utils.SvgIcon
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@SuppressLint("DefaultLocale")
@Composable
fun LatestMeasurements(
    weightEntries: List<WeightEntry> = emptyList(),
    hba1cEntries: List<HBA1CEntry> = emptyList(),
    diagnosisEntries: List<DiagnosisDate> = emptyList(),
    appointmentEntries: List<Appointment> = emptyList()
) {
    Column(
        modifier = Modifier
            .padding(20.dp)
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
        Spacer(modifier = Modifier.height(8.dp))
        UpcomingAppointmentsList(appointmentEntries)
    }
}

@Composable
fun ImportantDatesList(diagnosisEntries: List<DiagnosisDate>) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val primaryColor = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()

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
            val years = ChronoUnit.YEARS.between(diagnosis.date, today)
            val monthsTotal = ChronoUnit.MONTHS.between(diagnosis.date, today)
            val remainingMonths = monthsTotal - years * 12

            val elapsedText = when {
                years == 0L && remainingMonths == 0L -> "Aujourd’hui"
                years == 0L -> "$remainingMonths mois"
                remainingMonths == 0L -> "$years an${if (years > 1) "s" else ""}"
                else -> "$years an${if (years > 1) "s" else ""} et $remainingMonths mois"
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
                            text = "Diagnostiqué le ${diagnosis.date.format(formatter)}",
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

@Composable
fun UpcomingAppointmentsList(appointments: List<Appointment>) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val primaryColor = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()

    val upcomingAppointments = appointments.filter { it.date.isAfter(today) }
        .sortedBy { it.date } // on trie du plus proche au plus lointain

    if (upcomingAppointments.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Text(
            text = "Prochains rendez-vous",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
            color = MaterialTheme.colorScheme.surfaceTint
        )

        Spacer(Modifier.height(8.dp))

        upcomingAppointments.forEachIndexed { index, appointment ->
            val daysUntil = ChronoUnit.DAYS.between(today, appointment.date)
            val monthsUntil = ChronoUnit.MONTHS.between(today, appointment.date)
            val yearsUntil = ChronoUnit.YEARS.between(today, appointment.date)

            val remainingText = when {
                daysUntil == 0L -> "Aujourd’hui"
                daysUntil in 1..6 -> "Dans $daysUntil jour${if (daysUntil > 1) "s" else ""}"
                yearsUntil == 0L -> "Dans $monthsUntil mois"
                else -> "Dans $yearsUntil an${if (yearsUntil > 1) "s" else ""}"
            }

            Surface(
                shape = getItemShape(index, upcomingAppointments.size),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconResId = when (appointment.type.name) {
                        "APPOINTMENT" -> R.drawable.stethoscope_icon_vector
                        "ANNUAL_CHECKUP" -> R.drawable.recurring_event_icon_vector
                        else -> R.drawable.event_icon_vector
                    }

                    SvgIcon(
                        resId = iconResId,
                        modifier = Modifier.size(26.dp),
                        color = primaryColor
                    )

                    Spacer(Modifier.width(16.dp))

                    // Texte principal
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${appointment.doctor} - (${appointment.type.displayName})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Le ${appointment.date.format(formatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SvgIcon(
                            resId = R.drawable.hourglass_icon_vector,
                            modifier = Modifier.size(15.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        // Temps restant à droite
                        Text(
                            text = remainingText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (index != upcomingAppointments.size - 1) {
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}

data class MeasureCardData(
    val titleText: String,
    val dateText: String,
    @DrawableRes val icon: Int,
    val trendIcon: Int? = null
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
    val today = LocalDate.now()
    val oneYearAgo = today.minusYears(1)

    val cards = sources.mapNotNull { source ->
        // Dernière entrée toutes périodes confondues
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
