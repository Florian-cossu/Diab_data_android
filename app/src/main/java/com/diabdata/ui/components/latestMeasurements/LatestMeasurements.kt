package com.diabdata.ui.components.latestMeasurements

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.models.Appointment
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry
import com.diabdata.ui.components.latestMeasurements.components.ImportantDatesList
import com.diabdata.ui.components.latestMeasurements.components.LatestMeasures
import com.diabdata.ui.components.latestMeasurements.components.UpcomingAppointmentsList
import com.diabdata.ui.components.latestMeasurements.components.UpcomingTreatmentExpirationDates
import java.time.format.DateTimeFormatter

@SuppressLint("DefaultLocale")
@Composable
fun LatestMeasurements(
    weightEntries: List<WeightEntry> = emptyList(),
    hba1cEntries: List<HBA1CEntry> = emptyList(),
    diagnosisEntries: List<DiagnosisDate> = emptyList(),
    appointmentEntries: List<Appointment> = emptyList(),
    treatmentEntries: List<Treatment> = emptyList()
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
        Spacer(modifier = Modifier.height(8.dp))
        UpcomingTreatmentExpirationDates(treatments = treatmentEntries)
    }
}

data class MeasureSource<T>(
    val entries: List<T>,
    val icon: Int,
    val formatTitle: (T) -> String,
    val formatDate: (T) -> String
)