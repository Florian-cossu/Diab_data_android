package com.diabdata.data

import com.diabdata.models.Appointment
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry

data class ExportData(
    val weights: List<WeightEntry>,
    val hba1c: List<HBA1CEntry>,
    val appointments: List<Appointment>,
    val treatments: List<Treatment>,
    val diagnosisDates: List<DiagnosisDate>
)